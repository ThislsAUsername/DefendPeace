package Engine.GameEvents;

import Terrain.MapMaster;
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
  public void performEvent(MapMaster gameMap)
  {
    target.resupply();
  }
  
  @Override // there's no known way for this to fail after the GameAction is constructed
  public boolean shouldPreempt(MapMaster gameMap )
  {
    return false;
  }
}
