package Engine.GameEvents;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class ResupplyEvent implements GameEvent
{
  private Unit target;

  public ResupplyEvent(Unit aTarget)
  {
    target = aTarget;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildResupplyAnimation(target);
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveResupplyEvent( this );
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    target.resupply();
  }
}
