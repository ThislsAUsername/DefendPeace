package Terrain;

/**
 * Tile is a flyweight class - each Terrain/Weather combination is instantiated only once.
 * Subsequent calls to retrieve that tile will receive the same copy.
 */
public class Environment {
	public enum Terrains{PLAIN, FOREST, MOUNTAIN, DUNES, ROAD, CITY, FACTORY, HQ, SHOAL, WATER, REEF};
	public enum Weathers{CLEAR, RAIN, SNOW, SANDSTORM};

	public final Terrains terrainType;
	public final Weathers weatherType;

	private static Environment[][] tileInstances = new Environment[Terrains.values().length][Weathers.values().length];

	/**
	 * Private constructor so that Tile can manage all of its flyweights.
	 */
	private Environment(Terrains tileType, Weathers weather)
	{
		terrainType = tileType;
		weatherType = weather;
	}

	/**
	 * Returns the Tile flyweight matching the input parameters, creating it first if needed.
	 * @return
	 */
	public static Environment getTile(Terrains terrain, Weathers weather)
	{
		// If we don't have the desired Tile built already, create it.
		if(tileInstances[terrain.ordinal()][weather.ordinal()] == null)
		{
			System.out.println("Creating new Tile " + weather + ", " + terrain);
			tileInstances[terrain.ordinal()][weather.ordinal()] = new Environment(terrain, weather);
		}

		return tileInstances[terrain.ordinal()][weather.ordinal()];
	}
	
	public int getDefLevel() {
		// If there's a better way to do this, I'm all ears
		switch(terrainType){
		case PLAIN:
			return 1;
		case FOREST:
			return 2;
		case MOUNTAIN:
			return 4;
		case ROAD:
			return 0;
		case CITY:
			return 3;
		case FACTORY:
			return 4;
		case HQ:
			return 4;
		case SHOAL:
			return 0;
		case WATER:
			return 1;
		case REEF:
			return 2;
		default:
			System.out.println("Error in getDefLevel! Terrain type is invalid! Returning -1 for great shenanigans.");
			return -1;
		}
	}
}
