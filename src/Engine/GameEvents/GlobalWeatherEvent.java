package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.Environment.Weathers;
import Terrain.MapLocation;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * This event changes the weather of the whole map.
 */
public class GlobalWeatherEvent implements GameEvent
{
  private Weathers weather;
  private int duration;

  /** Changes the whole map to the given weather. */
  public GlobalWeatherEvent(Weathers newWeather)
  {
    this(newWeather, 1);
  }

  /** Changes the weather for the given number of rounds. */
  public GlobalWeatherEvent(Weathers newWeather, int forecastDuration)
  {
    weather = newWeather;
    duration = forecastDuration; 
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveWeatherChangeEvent(weather, duration);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for( int y = 0; y < gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        MapLocation loc = gameMap.getLocation(x, y);
        loc.setForecast(weather, (gameMap.game.armies.length * duration) - 1);
      }
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
