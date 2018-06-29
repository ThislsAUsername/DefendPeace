package Terrain;

import java.util.HashMap;
import java.util.Map;

import Terrain.Types.Airport;
import Terrain.Types.TerrainType;
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
  
  public static final TerrainType GRASS = Grass.getInstance();
  public static final TerrainType SEA = Sea.getInstance();
  public static final TerrainType SHOAL = Shoal.getInstance();
  public static final TerrainType AIRPORT = Airport.getInstance();
  public static final TerrainType BRIDGE = Bridge.getInstance();
  public static final TerrainType CITY = City.getInstance();
  public static final TerrainType DUNES = Dunes.getInstance();
  public static final TerrainType FACTORY = Factory.getInstance();
  public static final TerrainType FOREST = Forest.getInstance();
  public static final TerrainType HEADQUARTERS = Headquarters.getInstance();
  public static final TerrainType LAB = Lab.getInstance();
  public static final TerrainType MOUNTAIN = Mountain.getInstance();
  public static final TerrainType REEF = Reef.getInstance();
  public static final TerrainType ROAD = Road.getInstance();
  public static final TerrainType SEAPORT = Seaport.getInstance();

  private static TerrainType[] Terrains = {
    GRASS, SEA, SHOAL, AIRPORT, BRIDGE, CITY, DUNES, FACTORY, FOREST, HEADQUARTERS, LAB, MOUNTAIN, REEF, ROAD, SEAPORT
  };

  public enum Weathers
  {
    CLEAR, RAIN, SNOW, SANDSTORM
  };

  public final TerrainType terrainType;
  public final Weathers weatherType;

  // Maintain a list of all tile types types. Each type will be added the first time it is used.
  private static Map<TerrainType, Environment[]> tileInstances= new HashMap<TerrainType, Environment[]>();

  /**
   * Private constructor so that Tile can manage all of its flyweights.
   */
  private Environment(TerrainType tileType, Weathers weather)
  {
    terrainType = tileType;
    weatherType = weather;
  }

  /**
   * Returns the Tile flyweight matching the input parameters, creating it first if needed.
   * @return
   */
  public static Environment getTile(TerrainType terrain, Weathers weather)
  {
    // Add a new Environment array for this terrain type if it's missing.
    if( !tileInstances.containsKey(terrain) )
    {
      tileInstances.put(terrain, new Environment[Weathers.values().length]);
    }

    // If we don't have the desired Tile built already, create it.
    if( tileInstances.get(terrain)[weather.ordinal()] == null )
    {
      System.out.println("Creating new Tile " + weather + ", " + terrain);
      tileInstances.get(terrain)[weather.ordinal()] = new Environment(terrain, weather);
    }

    return tileInstances.get(terrain)[weather.ordinal()];
  }
  
  public static TerrainType[] getTerrainTypes()
  {
    return Terrains;
  }
}
