package Engine.UnitActionLifecycles;

import java.util.ArrayList;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.PathCalcParams;
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

public abstract class LaunchLifecycle
{
  public static class LaunchFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( moveLocation.equals(actor.x, actor.y) )
      {
        if( actor.heldUnits.size() > 0 )
        {
          ArrayList<GameAction> launchActions = new ArrayList<GameAction>();

          for( Unit cargo : actor.heldUnits )
          {
            if( cargo.isTurnOver )
              continue; // No acting on the same turn as you load

            // For the benefit of our planning functions, consider the cargo to be on the launcher's space...
            cargo.x = actor.x;
            cargo.y = actor.y;

            PathCalcParams pcp = new PathCalcParams(cargo, map);
            pcp.includeOccupiedSpaces = true;
            ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();
            // Acting in place after a launch is a cool concept, but a little weird in execution
            // Also, allowing action in place would allow *launching*, and recursive launching isn't something I wanna bite off
            destinations.remove(moveLocation);

            // Build a launch action for each possible action the cargo can do after launch
            for( Utils.SearchNode coord : destinations )
            {
              GamePath cargoMovePath = coord.getMyPath();
              ArrayList<GameActionSet> cargoActions = cargo.getPossibleActions(map, cargoMovePath, ignoreResident);

              for( GameActionSet actionSet : cargoActions )
                for( GameAction action : actionSet.getGameActions() )
                  launchActions.add(new LaunchAction(actor, cargo, action));
            }

            // ...and reset it when we're done calculating
            cargo.x = -1;
            cargo.y = -1;
          }

          if( !launchActions.isEmpty() )
          {
            return new GameActionSet(launchActions);
          }
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "LAUNCH";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return LAUNCH;
    }
  } // ~Factory

  public static class LaunchAction extends GameAction
  {
    private Unit launcher, launchee;
    private GameAction cargoAction;

    public LaunchAction(Unit actor, Unit passenger, GameAction passengerAction)
    {
      launcher = actor;
      launchee = passenger;
      cargoAction = passengerAction;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // LAUNCH actions consist of
      //   LAUNCH
      //   Launchable's action events
      //   [LOAD]*
      GameEventQueue launchEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != launchee && !launchee.isTurnOver;
      isValid &= null != launcher && !launcher.isTurnOver && launcher.heldUnits.contains(launchee);
      isValid &= null != cargoAction;
      isValid &= null != gameMap;

      // Generate events.
      if( isValid )
      {
        launchEvents.add(new LaunchEvent(launcher, launchee));
        GameEventQueue cargoEvents = cargoAction.getEvents(gameMap);
        launchEvents.addAll(cargoEvents);

        XYCoord firstEndPoint = cargoEvents.peekFirst().getEndPoint();
        if (null != firstEndPoint && firstEndPoint.equals(launcher.x, launcher.y))
          // If the first action ends on top of the launcher, assume it's a move action and re-load the unit to avoid weird states
          // This could theoretically happen due to ambush when there are no valid destinations on the path
          launchEvents.add(new LoadLifecycle.LoadEvent(launchee, launcher));
      }
      return launchEvents;
    }

    @Override
    public Unit getActor()
    {
      return launcher;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return new XYCoord(launcher.x, launcher.y);
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return new XYCoord(launcher.x, launcher.y);
    }

    @Override
    public String toString()
    {
      return String.format("[Launch %s from %s, then execute {%s}]",
          launchee, launcher.toStringWithLocation(),
          cargoAction.toString());
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.LAUNCH;
    }
  } // ~Action

  public static class LaunchEvent implements GameEvent
  {
    private final Unit transport;
    private final Unit cargo;

    public LaunchEvent(Unit transport, Unit cargo)
    {
      this.transport = transport;
      this.cargo = cargo;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return mapView.buildUnloadAnimation();
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return null;
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      if( transport.heldUnits != null && transport.heldUnits.contains(cargo) )
      {
        transport.heldUnits.remove(cargo);
        cargo.x = transport.x;
        cargo.y = transport.y;
      }
      else
      {
        System.out.println("WARNING! Failed to unload unit due to preconditions not being met:");
        if( transport.heldUnits == null )
          System.out.println("          Transport unit is empty.");
        if( !transport.heldUnits.contains(cargo) )
          System.out.println("          Unit to debark is not on transport.");
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
      return new XYCoord(transport.x, transport.y);
    }
  } // ~Event

}
