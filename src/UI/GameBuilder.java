package UI;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.GameScenario;
import Terrain.Environment.Weathers;
import Terrain.MapInfo;
import Terrain.MapMaster;
import Units.UnitModelScheme;

/**
 * Represents all of the information needed to create a GameInstance.
 */
public class GameBuilder
{
  public MapInfo mapInfo;
  public boolean isFowEnabled = false;
  public int startingFunds = 0;
  public int incomePerCity = 1000;
  public Weathers defaultWeather = Weathers.CLEAR;
  public UnitModelScheme unitModelScheme = null;
  public boolean isSecurityEnabled = false;

  GameBuilder(MapInfo info)
  {
    mapInfo = info;
  }

  public GameInstance createGame(PlayerSetupInfo[] coInfos)
  {
    GameScenario scenario = new GameScenario(unitModelScheme, incomePerCity, startingFunds, isFowEnabled);

    // Create all of the commanders.
    Commander[] cos = new Commander[mapInfo.getNumCos()];
    for(int i = 0; i < mapInfo.getNumCos(); ++i)
    {
      cos[i] = coInfos[i].makeCommander(scenario.rules);
    }

    // Build the CO list and the new map and create the game instance.
    MapMaster map = new MapMaster( cos, mapInfo );
    GameInstance newGame = null;
    if( map.initOK() )
    {
      newGame = new GameInstance(map, defaultWeather, scenario, isSecurityEnabled);
    }

    return newGame;
  }
}
