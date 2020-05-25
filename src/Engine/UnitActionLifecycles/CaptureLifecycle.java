package Engine.UnitActionLifecycles;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public abstract class CaptureLifecycle
{
  public static class CaptureFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        if( actor.CO.isEnemy(map.getLocation(moveLocation).getOwner()) && map.getLocation(moveLocation).isCaptureable() )
        {
          return new GameActionSet(new CaptureAction(map, actor, movePath), false);
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "CAPTURE";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return CAPTURE;
    }
  } // ~CaptureFactory

  public static class CaptureAction extends GameAction
  {
    private Unit actor = null;
    private Path movePath;
    private XYCoord movePathEnd;
    private TerrainType propertyType;

    public CaptureAction(GameMap gameMap, Unit unit, Path path)
    {
      actor = unit;
      movePath = path;
      if( (null != path) && path.getPathLength() > 0 )
      {
        movePathEnd = path.getEndCoord();
      }
      if( (null != gameMap) && gameMap.isLocationValid(movePathEnd))
      {
        propertyType = gameMap.getLocation(movePathEnd).getEnvironment().terrainType;
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      // CAPTURE actions consist of
      //   MOVE
      //   CAPTURE
      //   [DEFEAT]
      GameEventQueue captureEvents = new GameEventQueue();

      // Validate input
      Location captureLocation = null;
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver; // Valid unit
      isValid &= null != map; // Valid map
      isValid &= (null != movePath) && (movePath.getPathLength() > 0); // Valid path
      if( isValid )
      {
        movePathEnd = movePath.getEndCoord();
        captureLocation = map.getLocation(movePathEnd);
        isValid &= captureLocation.isCaptureable(); // Valid location
        isValid &= actor.CO.isEnemy(captureLocation.getOwner()); // Valid CO
      }

      // Generate events
      if( isValid )
      {
        // Move to the target location.
        if( Utils.enqueueMoveEvent(map, actor, movePath, captureEvents))
        {
          // Attempt to capture.
          CaptureEvent capture = new CaptureEvent(actor, map.getLocation(movePathEnd));
          captureEvents.add(capture);

          if( capture.willCapture() ) // If this will succeed, check if the CO will lose as a result.
          {
            // Check if capturing this property will cause someone's defeat.
            if( (propertyType == TerrainType.HEADQUARTERS) && (null != captureLocation.getOwner()) )
            {
              // Someone is losing their big, comfy chair.
              CommanderDefeatEvent defeat = new CommanderDefeatEvent(captureLocation.getOwner());
              defeat.setPropertyBeneficiary(actor.CO);
              captureEvents.add(defeat);
            }
          }
        }
      }
      return captureEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return movePathEnd;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return movePathEnd;
    }

    @Override
    public String toString()
    {
      return String.format("[Capture %s at %s with %s]", propertyType, movePathEnd, actor.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.CAPTURE;
    }
  } // ~CaptureAction
  
  public static class CaptureEvent implements GameEvent
  {
  private Unit unit = null;
  private Location location = null;
  final int captureAmount;
  final int priorCaptureAmount;

  public CaptureEvent( Unit u, Location loc )
  {
    unit = u;
    location = loc;
    XYCoord unitXY = new XYCoord(u.x, u.y);
    if( null != location && location.isCaptureable() && unit.CO.isEnemy(location.getOwner()) )
    {
      priorCaptureAmount = (unitXY.equals(location.getCoordinates()) ? unit.getCaptureProgress() : 0);
      captureAmount = unit.getHP(); // TODO: Apply CO buffs.
    }
    else
    {
      System.out.println("WARNING! Attempting to capture an invalid location!");
      priorCaptureAmount = 0;
      captureAmount = 0;
    }
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildCaptureAnimation();
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveCaptureEvent( unit, location );
  }

  public boolean willCapture()
  {
    int finalCapAmt = priorCaptureAmount + captureAmount;
    return finalCapAmt >= 20;
  }

  /**
   * NOTE: CaptureEvent expects the unit to already be on the location
   * it is attempting to capture. This should always be true (at least
   * until we change this by introducing new, game-breaking mechanics).
   */
  @Override
  public void performEvent(MapMaster gameMap)
  {
    // Only attempt to do the action if it is valid to do so.
    if( location.isCaptureable() &&
        (location.getResident() == unit) &&
        (unit.CO.isEnemy(location.getOwner())) )
    {
      unit.capture(gameMap.getLocation(unit.x, unit.y));
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return (null != location) ? location.getCoordinates() : null;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return (null != location) ? location.getCoordinates() : null;
  }
  } // ~CaptureEvent
}
