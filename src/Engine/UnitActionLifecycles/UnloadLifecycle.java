package Engine.UnitActionLifecycles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitContext;

public abstract class UnloadLifecycle
{
  public static class UnloadFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        if( actor.heldUnits.size() > 0 )
        {
          ArrayList<GameAction> unloadActions = new ArrayList<GameAction>();

          // TODO: Consider using ignoreResident for dropoff points as well
          for( Unit cargo : actor.heldUnits )
          {
            ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, actor, moveLocation, cargo);
            for( XYCoord loc : dropoffLocations )
            {
              unloadActions.add(new UnloadAction(actor, movePath, cargo, loc));
            }
          }

          if( !unloadActions.isEmpty() )
          {
            return new GameActionSet(unloadActions);
          }
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "UNLOAD";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return UNLOAD;
    }
  } // ~Factory

  public static class UnloadAction extends GameAction
  {
    private Unit actor;
    private GamePath movePath;
    private XYCoord moveLoc;
    private Map<Unit, XYCoord> myDropoffs;
    private XYCoord firstDropLoc;

    public UnloadAction(GameMap gameMap, Unit actor, GamePath path, Unit passenger, int dropX, int dropY)
    {
      this(actor, path, passenger, new XYCoord(dropX, dropY));
    }

    public UnloadAction(Unit transport, GamePath movePath, final Unit passenger, final XYCoord dropLocation)
    {
      this(transport, movePath, new HashMap<Unit, XYCoord>(){
        private static final long serialVersionUID = 1L;
        {
          this.put(passenger, dropLocation);
        }
      });
    }

    public UnloadAction(Unit transport, GamePath path, Map<Unit, XYCoord> dropoffs)
    {
      actor = transport;
      movePath = path;
      myDropoffs = dropoffs;

      // Grab the move location and the first drop location to support getMoveLocation and getTargetLocation.
      if( (null != movePath) && (movePath.getPathLength() > 0) )
      {
        moveLoc = movePath.getEndCoord();
      }
      if( !myDropoffs.isEmpty() )
      {
        for( XYCoord coord : myDropoffs.values() )
        {
          firstDropLoc = coord;
          break;
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // UNLOAD actions consist of
      //   MOVE (transport)
      //   UNLOAD
      //   [UNLOAD]*
      GameEventQueue unloadEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver;
      isValid &= null != myDropoffs && !myDropoffs.isEmpty();
      isValid &= movePath.getPathLength() > 0;
      isValid &= null != gameMap;
      if( isValid )
      {
        isValid &= !actor.heldUnits.isEmpty();
        for( Unit cargo : myDropoffs.keySet() ) // Make sure the cargo can go where we want to put it.
        {
          isValid &= new UnitContext(cargo).calculateMoveType().canTraverse(gameMap.getEnvironment(myDropoffs.get(cargo)));
        }
        for( XYCoord coord : myDropoffs.values() ) // Make sure nobody's there already.
        {
          Unit res = gameMap.getLocation(coord).getResident();
          isValid &= (null == res) || (res == actor); // Except the transport, because it must have moved anyway.
        }
      }

      // Generate events.
      if( isValid )
      {
        // Attempt to move the transport to the target location.
        if( Utils.enqueueMoveEvent(gameMap, actor, movePath, unloadEvents) )
        {
          // Debark the passengers. Unload all passengers you can, regardless of order.
          for( Unit unit : myDropoffs.keySet() )
          {
            XYCoord dropXY = myDropoffs.get(unit);
            if( gameMap.isLocationEmpty(actor, dropXY) )
            {
              unloadEvents.add(new UnloadEvent(actor, unit, myDropoffs.get(unit)));
            }
          }
        }
      }
      return unloadEvents;
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveLoc;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return firstDropLoc;
    }

    @Override
    public String toString()
    {
      return String.format("[Unload from %s]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.UNLOAD;
    }
  } // ~UnloadAction

  public static class UnloadEvent implements GameEvent
  {
    private final Unit transport;
    private final Unit cargo;
    private final XYCoord dropLoc;

    public UnloadEvent(Unit transport, Unit cargo, int dropX, int dropY)
    {
      this(transport, cargo, new XYCoord(dropX, dropY));
    }

    public UnloadEvent(Unit transport, Unit cargo, XYCoord dropLoc)
    {
      this.transport = transport;
      this.cargo = cargo;
      this.dropLoc = dropLoc;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return mapView.buildUnloadAnimation();
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return listener.receiveUnloadEvent(this);
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      if( transport.heldUnits != null && transport.heldUnits.contains(cargo)
          && gameMap.getLocation(dropLoc).getResident() == null )
      {
        transport.heldUnits.remove(cargo);
        gameMap.moveUnit(cargo, dropLoc.xCoord, dropLoc.yCoord);
        cargo.isTurnOver = true;
        transport.CO.myView.revealFog(cargo);
      }
      else
      {
        System.out.println("WARNING! Failed to unload unit due to preconditions not being met:");
        if( transport.heldUnits == null )
          System.out.println("          Transport unit is empty.");
        if( !transport.heldUnits.contains(cargo) )
          System.out.println("          Unit to debark is not on transport.");
        if( gameMap.getLocation(dropLoc).getResident() != null )
          System.out.println("          Unload location is not empty.");
      }
    }

    @Override
    public XYCoord getStartPoint()
    {
      return new XYCoord(transport.x, transport.y);
    }

    @Override
    public XYCoord getEndPoint()
    {
      return dropLoc;
    }
  } // ~Event

}
