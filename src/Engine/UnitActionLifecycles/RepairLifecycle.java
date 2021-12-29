package Engine.UnitActionLifecycles;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public abstract class RepairLifecycle
{
  public static class RepairFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        ArrayList<GameAction> repairOptions = new ArrayList<GameAction>();
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, 1, 1);

        // For each location, see if there is a friendly unit to repair.
        for( XYCoord loc : locations )
        {
          // If there's a friendly unit there who isn't us, we can repair them.
          Unit other = map.getLocation(loc).getResident();
          if( other != null && !actor.CO.isEnemy(other.CO) && other != actor
              && (!other.isFullySupplied() || other.isHurt()) )
          {
            repairOptions.add(new RepairUnitAction(actor, movePath, other));
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
    public String name()
    {
      return "REPAIR";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return REPAIR_UNIT;
    }
  }

  public static class RepairUnitAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    private XYCoord repairCoord;
    Unit benefactor;
    Unit beneficiary;

    public RepairUnitAction(Unit actor, GamePath path, Unit target)
    {
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
      //   RESUPPLY
      GameEventQueue repairEvents = new GameEventQueue();

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
        if( Utils.enqueueMoveEvent(gameMap, benefactor, movePath, repairEvents) )
        {
          // No surprises in the fog.
          repairEvents.add(new HealUnitEvent(beneficiary, 1, benefactor.CO.army)); // As this is a unit action, there's no usecase to vary this yet
          repairEvents.add(new ResupplyEvent(benefactor, beneficiary));
        }
      }
      return repairEvents;
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
      return String.format("[Move %s to %s and heal %s]", benefactor.toStringWithLocation(), moveCoord,
          beneficiary.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.REPAIR_UNIT;
    }
  } // ~RepairUnitAction

  // No event, as HealUnitEvent and ResupplyEvents are held in common with non-unit activities
}
