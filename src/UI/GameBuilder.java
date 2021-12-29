package UI;

import java.util.ArrayList;
import java.util.Arrays;

import CommandingOfficers.Commander;
import Engine.Army;
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

    // Create all of the armies.
    Commander[] propertyOwners = new Commander[mapInfo.getNumCos()];
    for(int i = 0; i < mapInfo.getNumCos(); ++i)
    {
      propertyOwners[i] = coInfos[i].makeCommander(scenario.rules);
    }

    // Build the CO list and the new map and create the game instance.
    MapMaster map = new MapMaster( propertyOwners, mapInfo );

    // Build the armies out of the COs
    Army[] armies = new Army[mapInfo.getNumCos()];
    for(int i = 0; i < mapInfo.getNumCos(); ++i)
    {
      armies[i] = new Army();
      // TODO: Add logic to add cart/persistent tags tag partners
      armies[i].cos = new Commander[] { propertyOwners[i] };
      armies[i].team = coInfos[i].currentTeam;
      armies[i].setAIController(coInfos[i].getCurrentAI().create(armies[i]));
    }
    
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
    }

    return newGame;
  }
}
