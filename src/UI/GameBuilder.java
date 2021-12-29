package UI;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GameScenario.TagMode;
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
  public TagMode tagMode = TagMode.OFF;
  public boolean isSecurityEnabled = false;

  GameBuilder(MapInfo info)
  {
    mapInfo = info;
  }

  public GameInstance createGame(PlayerSetupInfo[] coInfos)
  {
    GameScenario scenario = new GameScenario(unitModelScheme, incomePerCity, startingFunds, isFowEnabled, tagMode);

    // Add any CO-specific units into our set of UnitModels
    for(int i = 0; i < mapInfo.getNumCos(); ++i)
    {
      coInfos[i].getCurrentCO().injectUnits(unitModelScheme.getGameReadyModels());
    }

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
