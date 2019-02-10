package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * This event changes the environment (terrain type, weather, or both) of a map tile.
 */
public class MapChangeEvent implements GameEvent
{
  private XYCoord where;
  private Environment myEnvironment;
  public MapChangeEvent(XYCoord location, Environment newTile)
  {
    where = location;
    myEnvironment = newTile;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveMapChangeEvent(this);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    gameMap.getLocation(where).setEnvironment(myEnvironment);
  }

  @Override
  public XYCoord getStartPoint()
  {
    return where;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return where;
  }
}
