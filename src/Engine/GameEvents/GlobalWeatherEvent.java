package Engine.GameEvents;

import Engine.Army;
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
  public boolean canCancelFog = false; // If temporary fog is active and the weather doesn't create fog, cancel it.

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
  public void performEvent(MapMaster map)
  {
    for( int y = 0; y < map.mapHeight; ++y )
    {
      for( int x = 0; x < map.mapWidth; ++x )
      {
        MapLocation loc = map.getLocation(x, y);
        loc.setForecast(weather, (map.game.armies.length * duration) - 1);
      }
    }
    for( Army a : map.game.armies )
      a.myView.revealFog(); // In case this removed some vision penalties.

    if( map.game.rules.fogMode.fogDefaultsOn )
      return;
    // Beyond here is logic that turns on/off global fog in non-fog games.
    if( weather.startsFog )
    {
      map.game.setFog(duration);
      for( Army a : map.game.armies )
      {
        if( !a.isEnemy(map.game.activeArmy) )
          continue; // This is probably technically laxer than it should be (allows allies to watch your turn with full vision in DoR fog), but like... it seems more fun to see things than not.
        a.myView.resetFog();
      }
    }
    else if( canCancelFog )
    {
      map.game.setFog(-1); // 0 would make it active for this turn.
      for( Army a : map.game.armies )
        a.myView.resetFog();
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
