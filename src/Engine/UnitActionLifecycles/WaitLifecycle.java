package Engine.UnitActionLifecycles;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public abstract class WaitLifecycle
{
  public static class WaitFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new WaitAction(actor, movePath), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return "WAIT";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return WAIT;
    }
  } //~Factory

  public static class WaitAction extends GameAction
  {
    private final GamePath movePath;
    private final XYCoord waitLoc;
    private final Unit actor;

    public WaitAction(Unit unit, GamePath path)
    {
      actor = unit;
      movePath = path;
      if( (null != path) && (path.getPathLength() > 0) )
      {
        // Store the destination for later.
        waitLoc = movePath.getEndCoord();
      }
      else
        waitLoc = null;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // WAIT actions consist of
      //   MOVE
      GameEventQueue waitEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);

      if( isValid ) // Check fuel.
      {
        GamePath.PathNode endpoint = movePath.getEnd();
        int fuelBurn = movePath.getFuelCost(actor.model, gameMap);
        boolean includeOccupiedSpaces = true; // To allow validation for LOAD/JOIN actions.
        isValid = fuelBurn <= actor.fuel && fuelBurn <= actor.getMovePower(gameMap)
            && actor.getMoveFunctor(includeOccupiedSpaces).canEnd(gameMap, endpoint.GetCoordinates());
      }

      // Generate events.
      if( isValid )
      {
        Utils.enqueueMoveEvent(gameMap, actor, movePath, waitEvents);
      }
      return waitEvents;
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return waitLoc;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return waitLoc;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s]", actor.toStringWithLocation(), waitLoc);
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.WAIT;
    }
  } // ~WaitAction

  // No event, as MoveEvent is common to many unit action lifecycles

}
