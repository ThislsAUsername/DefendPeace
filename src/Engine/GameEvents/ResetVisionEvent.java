package Engine.GameEvents;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * Resets everyone's vision.
 */
public class ResetVisionEvent implements GameEvent
{
  final MapMaster map;

  public ResetVisionEvent(MapMaster map)
  {
    this.map = map;
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
  public void performEvent(MapMaster map)
  {
    for( Army army : map.game.armies )
    {
      army.myView.resetFog();
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return null;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return null;
  }
}
