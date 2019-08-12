package Terrain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Environment is a flyweight class - each Terrain/Weather combination is instantiated only once.
 * Subsequent calls to retrieve that tile will receive the same copy.
 */
public class Environment implements Serializable
{
  private static final long serialVersionUID = 1L;
  public static final int PROPERTY_VISION_RANGE = 2;

  public enum Weathers
  {
    CLEAR, SNOW, RAIN, SANDSTORM
  };

  public final TerrainType terrainType;
  public final Weathers weatherType;

  // Maintain a list of all tile types. Each type will be added the first time it is used.
  private static Map<TerrainType, Environment[]> tileInstances = new HashMap<TerrainType, Environment[]>();

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
}
