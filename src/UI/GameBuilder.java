package UI;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Terrain.Environment.Weathers;
import Terrain.MapInfo;
import Terrain.MapMaster;

/**
 * Represents all of the information needed to create a GameInstance.
 */
public class GameBuilder
{
  public MapInfo mapInfo;
  public ArrayList<Commander> commanders;
  public boolean isFowEnabled = false;
  public int startingFunds = 0;
  public int incomePerCity = 1000;
  public Weathers defaultWeather = Weathers.CLEAR;

  GameBuilder(MapInfo info)
  {
    mapInfo = info;
    commanders = new ArrayList<Commander>();
  }

  public void addCO(Commander co)
  {
    commanders.add(co);
  }

  public GameInstance createGame()
  {
    GameInstance newGame = null;

    // Build the CO list and the new map and create the game instance.
    Commander[] cos = commanders.toArray(new Commander[commanders.size()]);
    MapMaster map = new MapMaster( cos, mapInfo );
    if( map.initOK() )
    {
      newGame = new GameInstance(map, isFowEnabled, defaultWeather, startingFunds);

      for(Commander co: commanders) co.incomePerCity = incomePerCity;
    }

    return newGame;
  }
}
