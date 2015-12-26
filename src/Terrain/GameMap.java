package Terrain;

public class GameMap {

	private Tile[][] map;
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
		map = new Tile[mapWidth][mapHeight];
		for(int w = 0; w < mapWidth; ++w)
		{
			for(int h = 0; h < mapHeight; ++h)
			{
				// Make a ring of water around the edges, and the rest plains to start.
				if(w == 0 || h == 0 || (w == mapWidth - 1) || (h == mapHeight - 1) )
				{
					map[w][h] = Tile.getTile(Tile.Terrains.WATER, Tile.Weathers.CLEAR);
				}
				else
				{
					map[w][h] = Tile.getTile(Tile.Terrains.PLAIN, Tile.Weathers.CLEAR);
				}
			}
		}

		// Throw down an HQ and a factory for each.
		map[1][8] = Tile.getTile(Tile.Terrains.HQ, Tile.Weathers.CLEAR);
		map[1][7] = Tile.getTile(Tile.Terrains.FACTORY, Tile.Weathers.CLEAR);
		map[2][7] = Tile.getTile(Tile.Terrains.FACTORY, Tile.Weathers.CLEAR);

		map[13][1] = Tile.getTile(Tile.Terrains.HQ, Tile.Weathers.CLEAR);
		map[13][2] = Tile.getTile(Tile.Terrains.FACTORY, Tile.Weathers.CLEAR);
		map[12][2] = Tile.getTile(Tile.Terrains.FACTORY, Tile.Weathers.CLEAR);

		// Add some cities and forests
		map[1][4] = Tile.getTile(Tile.Terrains.CITY, Tile.Weathers.CLEAR);
		map[2][5] = Tile.getTile(Tile.Terrains.CITY, Tile.Weathers.CLEAR);
		map[13][5] = Tile.getTile(Tile.Terrains.CITY, Tile.Weathers.CLEAR);
		map[12][4] = Tile.getTile(Tile.Terrains.CITY, Tile.Weathers.CLEAR);

		map[3][5] = Tile.getTile(Tile.Terrains.FOREST, Tile.Weathers.CLEAR);
		map[6][5] = Tile.getTile(Tile.Terrains.FOREST, Tile.Weathers.CLEAR);
		map[11][4] = Tile.getTile(Tile.Terrains.FOREST, Tile.Weathers.CLEAR);
		map[8][4] = Tile.getTile(Tile.Terrains.FOREST, Tile.Weathers.CLEAR);

		// Coupla shoals and reefs
		map[1][1] = Tile.getTile(Tile.Terrains.SHOAL, Tile.Weathers.CLEAR);
		map[13][8] = Tile.getTile(Tile.Terrains.SHOAL, Tile.Weathers.CLEAR);

		map[5][0] = Tile.getTile(Tile.Terrains.REEF, Tile.Weathers.CLEAR);
		map[9][0] = Tile.getTile(Tile.Terrains.REEF, Tile.Weathers.CLEAR);
		map[10][9] = Tile.getTile(Tile.Terrains.REEF, Tile.Weathers.CLEAR);
		map[6][9] = Tile.getTile(Tile.Terrains.REEF, Tile.Weathers.CLEAR);
		
		// Mountains
		map[3][2] = Tile.getTile(Tile.Terrains.MOUNTAIN, Tile.Weathers.CLEAR);
		map[4][3] = Tile.getTile(Tile.Terrains.MOUNTAIN, Tile.Weathers.CLEAR);
		map[10][6] = Tile.getTile(Tile.Terrains.MOUNTAIN, Tile.Weathers.CLEAR);
		map[11][7] = Tile.getTile(Tile.Terrains.MOUNTAIN, Tile.Weathers.CLEAR);

		// Finally, add a road.
		map[4][6] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[5][6] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[6][6] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[7][6] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[7][5] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[7][4] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[7][3] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[7][3] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[8][3] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[9][3] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
		map[10][3] = Tile.getTile(Tile.Terrains.ROAD, Tile.Weathers.CLEAR);
	}

	public Tile getTile(int w, int h)
	{
		if(w < 0 || w >= mapWidth || h < 0 || h >= mapHeight)
		{
			System.out.println("Warning! Attempting to retrieve an invalid tile! (" + w + ", " + h + ")");
			return null;
		}
		return map[w][h];
	}
}
