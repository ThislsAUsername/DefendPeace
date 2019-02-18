package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.Location;
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
  private int turns;

  /** For when you just want to redraw the map */
  public MapChangeEvent()
  {
    this(null, null, 0);
  }

  /** For changing terrain type */
  public MapChangeEvent(XYCoord location, Environment newTile)
  {
    this(location, newTile, 0);
  }

  /** For changing weather */
  public MapChangeEvent(XYCoord location, Environment newTile, int duration)
  {
    where = location;
    myEnvironment = newTile;
    turns = duration;
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
    Location loc = gameMap.getLocation(where);
    if( null != loc )
    {
      loc.setEnvironment(myEnvironment);
      if (turns > 0)
        loc.setForecast(myEnvironment.weatherType, (gameMap.commanders.length * turns) - 1);
    }
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
