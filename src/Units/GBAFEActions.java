package Units;

import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.UnitActionLifecycles.TransformLifecycle.TransformEvent;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;

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
          transformEvents.add(new HealUnitEvent(actor, 10, null)); // "Free" fullheal included, for tactical spice
          transformEvents.add(new TransformEvent(actor, type.destinationType));
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
  } // ~TransformAction

}
