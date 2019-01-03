package Engine.GameEvents;

import Terrain.Location;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class CaptureEvent implements GameEvent
{
  private Unit unit = null;
  private Location location = null;
  final int captureAmount;
  final int priorCaptureAmount;

  public CaptureEvent( Unit u, Location loc )
  {
    unit = u;
    location = loc;
    if( null != location && location.isCaptureable() && unit.CO.isEnemy(location.getOwner()) )
    {
      priorCaptureAmount = unit.getCaptureProgress();
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
}
