package Terrain;

import Terrain.Tile.Weathers; // Eclipse was whining that it couldn't resolve this, and now it's whining that it's not used. *sigh*

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
				map[w][h] = Tile.getTile(Tile.Terrains.PLAIN, Tile.Weathers.CLEAR);
			}
		}

		map[1][4] = Tile.getTile(Tile.Terrains.CITY, Tile.Weathers.CLEAR);
		map[13][4] = Tile.getTile(Tile.Terrains.CITY, Tile.Weathers.CLEAR);
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
