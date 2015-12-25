package Terrain;

public class Tile {
	public enum Terrains{PLAIN, FOREST, ROAD, CITY, FACTORY, HQ, SHOAL, WATER, REEF};
	public enum Weathers{CLEAR, RAIN, SNOW, SANDSTORM};

	public final Terrains terrainType;
	public final Weathers weatherType;

	private static Tile[][] tileInstances = new Tile[Terrains.values().length][Weathers.values().length];

	private Tile(Terrains tileType, Weathers weather)
	{
		terrainType = tileType;
		weatherType = weather;
	}

	public static Tile getTile(Terrains terrain, Weathers weather)
	{
		if(tileInstances[terrain.ordinal()][weather.ordinal()] == null)
		{
			System.out.println("Creating new Tile " + weather + ", " + terrain);
			tileInstances[terrain.ordinal()][weather.ordinal()] = new Tile(terrain, weather);
		}

		return tileInstances[terrain.ordinal()][weather.ordinal()];
	}
}
