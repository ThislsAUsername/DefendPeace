package Engine.GameEvents;

import java.util.function.Consumer;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class LambdaEvent implements GameEvent
{
  private final Consumer<MapMaster> lambda;

  public LambdaEvent( Consumer<MapMaster> lambda )
  {
    this.lambda = lambda;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    // TODO?
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    // TODO?
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    lambda.accept(gameMap);
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
