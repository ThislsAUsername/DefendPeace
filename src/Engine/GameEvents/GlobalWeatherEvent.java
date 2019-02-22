package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.Environment.Weathers;
import Terrain.Location;
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

  /** Changes the whole map to the given weather for the rest of this turn. */
  public GlobalWeatherEvent(Weathers newWeather)
  {
    this(newWeather, 0);
  }

  /** Changes the weather for the given number of turns, not counting the current turn. */
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
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveWeatherChangeEvent(weather, duration);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for( int y = 0; y < gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        Location loc = gameMap.getLocation(x, y);
        loc.setForecast(weather, (gameMap.commanders.length * duration) - 1);
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
