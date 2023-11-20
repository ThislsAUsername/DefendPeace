package Units;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.UnitActionLifecycles.TransformLifecycle.TransformEvent;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class GBAFEActions
{
  public static final int PROMOTION_COST = 5000;
  public static class PromotionFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final UnitModel destinationType;
    public final String name;

    public PromotionFactory(UnitModel type)
    {
      destinationType = type;
      name = "~" + type.name + " ("+PROMOTION_COST+")";
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        boolean validPromo = true;
        validPromo &= actor.CO.army.money >= PROMOTION_COST;
        MapLocation destInfo = map.getLocation(moveLocation);
        validPromo &= !actor.CO.isEnemy(destInfo.getOwner());
        if( validPromo )
          return new GameActionSet(new PromotionAction(actor, movePath, this), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }
  }

  /** Effectively a WAIT that costs money, and the unit ends up as a different unit at the end of it. */
  public static class PromotionAction extends WaitLifecycle.WaitAction
  {
    private PromotionFactory type;
    Unit actor;

    public PromotionAction(Unit unit, GamePath path, PromotionFactory pType)
    {
      super(unit, path);
      type = pType;
      actor = unit;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue transformEvents = super.getEvents(gameMap);

      if( transformEvents.size() > 0 ) // if we successfully made a move action
      {
        GameEvent moveEvent = transformEvents.peek();
        if( moveEvent.getEndPoint().equals(getMoveLocation()) ) // make sure we shouldn't be pre-empted
        {
          transformEvents.add(new ModifyFundsEvent(actor.CO.army, -1 * PROMOTION_COST));
          transformEvents.add(new TransformEvent(actor, type.destinationType));
          transformEvents.add(new HealUnitEvent(actor, 10, null)); // "Free" fullheal included, for tactical spice
          transformEvents.add(new ResupplyEvent(null, actor));     //   and also resupply, since we use this for ballistae
        }
      }
      return transformEvents;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and promote to %s]", actor.toStringWithLocation(), getMoveLocation(),
          type.destinationType);
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~PromotionAction

  public static abstract class SupportActionFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final String name;
    public final int rangeMin, rangeMax;

    public SupportActionFactory(String name)
    {
      this(name, 1, 1);
    }
    public SupportActionFactory(String name, int rangeMin, int rangeMax)
    {
      this.name     = name;
      this.rangeMin = rangeMin;
      this.rangeMax = rangeMax;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        ArrayList<GameAction> repairOptions = new ArrayList<GameAction>();
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, rangeMin, rangeMax);

        // For each location, see if there is a friendly unit to repair.
        for( XYCoord loc : locations )
        {
          // If there's a friendly unit there who isn't us, we can repair them.
          Unit other = map.getLocation(loc).getResident();
          if( other != null
              && !actor.CO.isEnemy(other.CO) && other != actor
              && canSupport(map, actor, movePath, other)
              )
          {
            repairOptions.add(getSupport(map, actor, movePath, other));
          }
        }

        // Only add this action set if we actually have a target
        if( !repairOptions.isEmpty() )
        {
          // Bundle our attack options into an action set
          return new GameActionSet(repairOptions);
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }

    public abstract boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other);
    public abstract GameAction getSupport(GameMap map, Unit actor, GamePath movePath, Unit other);
  }

  /**
   * Repair, but:<p>
   * It's free<p>
   * It has variable heal quantity<p>
   * It has variable range<p>
   * It doesn't take target HP into account because it'd be very annoying to play with<p>
   * It doesn't work on boats (I have stupid plans for boats)<p>
   */
  public static class HealStaffFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final int quantity;

    public HealStaffFactory(String name, int healHP, int range)
    {
      super(name, 1, range);
      quantity = healHP;
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return !other.model.isAny(UnitModel.SHIP) && (!other.isFullySupplied() || other.isHurt());
    }
    @Override
    public GameAction getSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return new HealStaffAction(this, actor, movePath, other);
    }
  }

  public static class HealStaffAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    private XYCoord repairCoord;
    Unit benefactor;
    Unit beneficiary;
    HealStaffFactory type;

    public HealStaffAction(HealStaffFactory type, Unit actor, GamePath path, Unit target)
    {
      this.type = type;
      benefactor = actor;
      beneficiary = target;
      movePath = path;
      if( benefactor != null && null != beneficiary )
      {
        startCoord = new XYCoord(actor.x, actor.y);
        repairCoord = new XYCoord(target.x, target.y);
      }
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = movePath.getEndCoord();
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Repair actions consist of
      //   MOVE
      //   HEAL
      GameEventQueue healEvents = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != repairCoord) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(repairCoord) )
      {
        isValid &= benefactor != null && !benefactor.isTurnOver;
        isValid &= isValid && null != beneficiary && !benefactor.CO.isEnemy(beneficiary.CO);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, benefactor, movePath, healEvents) )
        {
          // No surprises in the fog.
          healEvents.add(new HealUnitEvent(beneficiary, type.quantity, null));
        }
      }
      return healEvents;
    }

    @Override
    public Unit getActor()
    {
      return benefactor;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveCoord;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return repairCoord;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and use %s to heal %s]", benefactor.toStringWithLocation(), moveCoord,
          type.name, beneficiary.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~HealStaffAction


  /**
   * Uses hacker wizard powers to cheat the turn system<p>
   */
  public static class ReactivateUnitFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;

    public ReactivateUnitFactory(String name)
    {
      super(name);
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return other.isTurnOver;
    }
    @Override
    public GameAction getSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return new ReactivateUnitAction(this, actor, movePath, other);
    }
  }

  public static class ReactivateUnitAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    private XYCoord repairCoord;
    Unit benefactor;
    Unit beneficiary;
    ReactivateUnitFactory type;

    public ReactivateUnitAction(ReactivateUnitFactory type, Unit actor, GamePath path, Unit target)
    {
      this.type = type;
      benefactor = actor;
      beneficiary = target;
      movePath = path;
      if( benefactor != null && null != beneficiary )
      {
        startCoord = new XYCoord(actor.x, actor.y);
        repairCoord = new XYCoord(target.x, target.y);
      }
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = movePath.getEndCoord();
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   REACTIVATE
      GameEventQueue healEvents = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != repairCoord) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(repairCoord) )
      {
        isValid &= benefactor != null && !benefactor.isTurnOver;
        isValid &= isValid && null != beneficiary && !benefactor.CO.isEnemy(beneficiary.CO);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, benefactor, movePath, healEvents) )
        {
          // No surprises in the fog.
          healEvents.add(new ReactivateUnitEvent(beneficiary));
        }
      }
      return healEvents;
    }

    @Override
    public Unit getActor()
    {
      return benefactor;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveCoord;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return repairCoord;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and use %s to heal %s]", benefactor.toStringWithLocation(), moveCoord,
          type.name, beneficiary.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~ReactivateUnitAction

public static class ReactivateUnitEvent implements GameEvent
{
  private Unit unit;

  public ReactivateUnitEvent(Unit aTarget)
  {
    unit = aTarget;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return null;
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( unit.isStunned )
      unit.isStunned = false;
    else
      unit.isTurnOver = false;
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }
}
}
