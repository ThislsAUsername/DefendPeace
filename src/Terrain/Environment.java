package Terrain;

import java.util.ArrayList;

import Terrain.Types.Airport;
import Terrain.Types.BaseTerrain;
import Terrain.Types.Bridge;
import Terrain.Types.City;
import Terrain.Types.Dunes;
import Terrain.Types.Factory;
import Terrain.Types.Forest;
import Terrain.Types.Grass;
import Terrain.Types.Headquarters;
import Terrain.Types.Lab;
import Terrain.Types.Mountain;
import Terrain.Types.Reef;
import Terrain.Types.Road;
import Terrain.Types.Sea;
import Terrain.Types.Seaport;
import Terrain.Types.Shoal;

/**
 * Environment is a flyweight class - each Terrain/Weather combination is instantiated only once.
 * Subsequent calls to retrieve that tile will receive the same copy.
 */
public class Environment
{
  
  private static ArrayList<BaseTerrain> terrainTypes;

  public enum Weathers
  {
    CLEAR, RAIN, SNOW, SANDSTORM
  };

  public final BaseTerrain terrainType;
  public final Weathers weatherType;

  // ArrayList can dynamically expand to accomodate arbitrary terrains at runtime
  // Thankfully, there's no plans to expand our list of weathers
  private static ArrayList<Environment[]> tileInstances= new ArrayList<Environment[]>();

  /**
   * Private constructor so that Tile can manage all of its flyweights.
   */
  private Environment(BaseTerrain tileType, Weathers weather)
  {
    terrainType = tileType;
    weatherType = weather;
  }

  /**
   * Returns the Tile flyweight matching the input parameters, creating it first if needed.
   * @return
   */
  public static Environment getTile(BaseTerrain terrain, Weathers weather)
  {
    // If we don't have a slot for that terrain yet, expand the array to allow it
    if(tileInstances.size() <= terrain.index)
    { // start iteration from current size to avoid excess elements
      for (int i = tileInstances.size(); i <= terrain.index; i++)
        tileInstances.add(i, new Environment[Weathers.values().length]);
    }
    // If we don't have the desired Tile built already, create it.
    if( tileInstances.get(terrain.index)[weather.ordinal()] == null )
    {
      System.out.println("Creating new Tile " + weather + ", " + terrain);
      tileInstances.get(terrain.index)[weather.ordinal()] = new Environment(terrain, weather);
    }

    return tileInstances.get(terrain.index)[weather.ordinal()];
  }
  
  public static ArrayList<BaseTerrain> getTerrainTypes()
  {
    if( null == terrainTypes )
    {
      terrainTypes = new ArrayList<BaseTerrain>();
      // init base terrains first; their indexes should exist before the others' do
      Grass.getInstance();
      Sea.getInstance();
      Shoal.getInstance();
      
      Airport.getInstance();
      Bridge.getInstance();
      City.getInstance();
      Dunes.getInstance();
      Factory.getInstance();
      Forest.getInstance();
      Headquarters.getInstance();
      Lab.getInstance();
      Mountain.getInstance();
      Reef.getInstance();
      Road.getInstance();
      Seaport.getInstance();
    }
    return terrainTypes;
  }
}
