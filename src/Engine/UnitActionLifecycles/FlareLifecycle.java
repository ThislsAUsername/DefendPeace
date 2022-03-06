package Engine.UnitActionLifecycles;

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
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public abstract class FlareLifecycle
{
  public static class FlareFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public int minRange, maxRange, radius;

    public FlareFactory(int minRange, int maxRange, int radius)
    {
      this.minRange = minRange;
      this.maxRange = maxRange;
      this.radius = radius;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        if( actor.ammo > 0 && moveLocation.equals(actor.x, actor.y) )
        {
          ArrayList<GameAction> targetOptions = new ArrayList<GameAction>();

          ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, minRange, maxRange);

          for( XYCoord loc : locations )
            targetOptions.add(new FlareAction(this, actor, movePath, loc));

          // Only add this action set if we actually have a target
          if( !targetOptions.isEmpty() )
          {
            // Bundle our attack options into an action set
            GameActionSet actions = new GameActionSet(targetOptions);
            actions.useFreeSelect = true;
            return actions;
          }
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "FLARE";
    }
  }

  public static class FlareAction extends GameAction
  {
    private FlareFactory type;
    private GamePath movePath;
    private XYCoord moveCoord = null;
    private XYCoord launchLocation = null;
    private Unit actor;

    public FlareAction(FlareFactory pType, Unit actor, GamePath path, int targetX, int targetY)
    {
      this(pType, actor, path, new XYCoord(targetX, targetY));
    }

    public FlareAction(FlareFactory pType, Unit actor, GamePath path, XYCoord atkLoc)
    {
      type = pType;
      movePath = path;
      this.actor = actor;
      launchLocation = atkLoc;
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = path.getEndCoord();
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue flareEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= actor != null && !actor.isTurnOver;
      isValid &= (null != gameMap) && (gameMap.isLocationValid(launchLocation)) && gameMap.isLocationValid(moveCoord);
      isValid &= (movePath != null) && (movePath.getPathLength() > 0);

      int range = Math.abs(moveCoord.xCoord - launchLocation.xCoord) + Math.abs(moveCoord.yCoord - launchLocation.yCoord);
      isValid &= type.minRange <= range && range <= type.maxRange;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, actor, movePath, flareEvents) )
        {
          // No surprises in the fog. Resolve defoggination.
          GameEvent event = new FlareEvent(actor, launchLocation, type.radius);
          flareEvents.add(event);
        }
      }
      return flareEvents;
    }

    @Override
    public String toString()
    {
      return String.format("[Launch a flare to %s with %s after moving to %s]", launchLocation, actor.toStringWithLocation(),
          moveCoord);
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveCoord;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return launchLocation;
    }
  }

  public static class FlareEvent implements GameEvent
  {
    private final Unit launcher;
    private final XYCoord target;
    private final int radius;

    public FlareEvent(Unit unit, XYCoord pTarget, int pRadius)
    {
      launcher = unit;
      target = pTarget;
      radius = pRadius;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      // TODO: Create listener hook
      return null;
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      launcher.ammo -= 1;
      ArrayList<XYCoord> tiles = Utils.findLocationsInRange(gameMap, target, 0, radius);
      for( XYCoord xyc : tiles )
        launcher.CO.army.myView.revealFog(xyc, true);
    }

    @Override
    public XYCoord getStartPoint()
    {
      return new XYCoord(launcher.x, launcher.y);
    }

    @Override
    public XYCoord getEndPoint()
    {
      return target;
    }
  } // ~FlareEvent

}
