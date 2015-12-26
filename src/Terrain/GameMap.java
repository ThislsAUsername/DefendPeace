package Terrain;

public class GameMap {

	private Location[][] map;
	public final int mapWidth;
	public final int mapHeight;

	//TODO: We need a way to load/use multiple maps; either:
	//  pass in a file name as a parameter here, or subclass
	//  GameMap for each map you want, or have a static data
	//  element with tile information for each available map
	public GameMap()
	{
		// for now, just make a 15x10 map of almost all plains.
		mapWidth = 15;
		mapHeight = 10;
		map = new Location[mapWidth][mapHeight];
		for(int w = 0; w < mapWidth; ++w)
		{
			for(int h = 0; h < mapHeight; ++h)
			{
				// Make a ring of water around the edges, and the rest plains to start.
				if(w == 0 || h == 0 || (w == mapWidth - 1) || (h == mapHeight - 1) )
				{
					map[w][h] = new Location(Environment.getTile(Environment.Terrains.WATER, Environment.Weathers.CLEAR));
				}
				else
				{
					map[w][h] = new Location(Environment.getTile(Environment.Terrains.PLAIN, Environment.Weathers.CLEAR));
				}
			}
		}
		
		// Throw down an HQ and a factory for each.
		map[1][8] = new Location(Environment.getTile(Environment.Terrains.HQ, Environment.Weathers.CLEAR));
		map[1][7] = new Location(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));
		map[2][7] = new Location(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));

		map[13][1] = new Location(Environment.getTile(Environment.Terrains.HQ, Environment.Weathers.CLEAR));
		map[13][2] = new Location(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));
		map[12][2] = new Location(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));

		// Add some cities and forests
		map[1][4] = new Location(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[2][5] = new Location(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[13][5] = new Location(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[12][4] = new Location(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));

		map[3][5] = new Location(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		map[6][5] = new Location(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		map[11][4] = new Location(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		map[8][4] = new Location(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));

		// Coupla shoals and reefs
		map[1][1] = new Location(Environment.getTile(Environment.Terrains.SHOAL, Environment.Weathers.CLEAR));
		map[13][8] = new Location(Environment.getTile(Environment.Terrains.SHOAL, Environment.Weathers.CLEAR));

		map[5][0] = new Location(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		map[9][0] = new Location(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		map[10][9] = new Location(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		map[6][9] = new Location(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		
		// Mountains
		map[3][2] = new Location(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));
		map[4][3] = new Location(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));
		map[10][6] = new Location(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));
		map[11][7] = new Location(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));

		// Finally, add a road.
		map[4][6] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[5][6] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[6][6] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][6] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][5] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][4] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][3] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][3] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[8][3] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[9][3] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[10][3] = new Location(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
	}

	public Environment getEnvironment(int w, int h)
	{
		if(w < 0 || w >= mapWidth || h < 0 || h >= mapHeight)
		{
			System.out.println("Warning! Attempting to retrieve an invalid tile! (" + w + ", " + h + ")");
			return null;
		}
		return map[w][h].getEnvironment();
	}

	public Location getLocation(int w, int h)
	{
		if(w < 0 || w >= mapWidth || h < 0 || h >= mapHeight)
		{
			System.out.println("Warning! Attempting to retrieve an invalid tile! (" + w + ", " + h + ")");
			return null;
		}
		return map[w][h];
	}
}
