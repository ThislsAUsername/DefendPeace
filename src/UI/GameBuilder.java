package UI;

import java.util.ArrayList;
import java.util.Arrays;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GameScenario.FogMode;
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
  public FogMode fogMode = FogMode.OFF_TRILOGY;
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

  public GameInstance createGame(PlayerSetupInfo[] playerInfos)
  {
    GameScenario scenario = new GameScenario(unitModelScheme, incomePerCity, startingFunds, fogMode, tagMode);

    // Add any CO-specific units into our set of UnitModels
    for( PlayerSetupInfo player : playerInfos )
    {
      for( CommanderInfo info : player.getCurrentCOList() )
      {
        info.injectUnits(unitModelScheme.getGameReadyModels());
      }
    }

    // Create all of the armies.
    Army[] armies = new Army[mapInfo.getNumPlayers()];
    for(int i = 0; i < mapInfo.getNumPlayers(); ++i)
    {
      armies[i] = new Army(scenario);
      armies[i].cos = playerInfos[i].makeCommanders(scenario.rules);
      armies[i].team = playerInfos[i].currentTeam;
      armies[i].setAIController(playerInfos[i].getCurrentAI().create(armies[i]));
    }

    // Build the CO list and the new map and create the game instance.
    MapMaster map = new MapMaster( armies, mapInfo );

    // If doing team merge... merge the teams
    if( TagMode.Team_Merge == tagMode )
    {
      // The final number of armies should probably be fewer than the number of armies in the map, so we'll just copy them over
      ArrayList<Army> finalArmies = new ArrayList<>();
      for(Army army : armies)
      {
        // See if we've seen this army's team yet
        Army match = null;
        for (Army finalArmy : finalArmies)
          if(finalArmy.team == army.team)
            match = finalArmy;

        // If not, just add it to the final list 
        if(match == null)
        {
          finalArmies.add(army);
        }
        else // else, merge with the match we found
        {
          // credit: https://www.baeldung.com/java-concatenate-arrays
          // Clone the final army's commander list into a larger space, then copy the new Commanders into it
          Commander[] newCoList = Arrays.copyOf(match.cos, match.cos.length + army.cos.length);
          System.arraycopy(army.cos, 0, newCoList, match.cos.length, army.cos.length);
          match.cos = newCoList;
          match.HQLocations.addAll(army.HQLocations);
        }
      }
      armies = finalArmies.toArray(new Army[0]);
    }

    // Hook COs back up to their army
    for(Army army : armies)
    {
      for (Commander co : army.cos)
        co.army = army;
    }

    GameInstance newGame = null;
    if( map.initOK() )
    {
      newGame = new GameInstance(scenario, armies, map, defaultWeather, isSecurityEnabled);
      unitModelScheme.registerStateTrackers(newGame);
    }

    return newGame;
  }
}
