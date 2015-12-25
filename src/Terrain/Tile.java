package Terrain;

/**
 * Tile is a flyweight class - each Terrain/Weather combination is instantiated only once.
 * Subsequent calls to retrieve that tile will receive the same copy.
 */
public class Tile {
	public enum Terrains{PLAIN, FOREST, ROAD, CITY, FACTORY, HQ, SHOAL, WATER, REEF};
	public enum Weathers{CLEAR, RAIN, SNOW, SANDSTORM};

	public final Terrains terrainType;
	public final Weathers weatherType;

	private static Tile[][] tileInstances = new Tile[Terrains.values().length][Weathers.values().length];

	/**
	 * Private constructor so that Tile can manage all of its flyweights.
	 */
	private Tile(Terrains tileType, Weathers weather)
	{
		terrainType = tileType;
		weatherType = weather;
	}

	/**
	 * Returns the Tile flyweight matching the input parameters, creating it first if needed.
	 * @return
	 */
	public static Tile getTile(Terrains terrain, Weathers weather)
	{
		// If we don't have the desired Tile built already, create it.
		if(tileInstances[terrain.ordinal()][weather.ordinal()] == null)
		{
			System.out.println("Creating new Tile " + weather + ", " + terrain);
			tileInstances[terrain.ordinal()][weather.ordinal()] = new Tile(terrain, weather);
		}

		return tileInstances[terrain.ordinal()][weather.ordinal()];
	}
}
