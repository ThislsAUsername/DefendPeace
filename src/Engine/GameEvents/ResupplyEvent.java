package Engine.GameEvents;

import Engine.XYCoord;
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

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(target.x, target.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(target.x, target.y);
  }
}
