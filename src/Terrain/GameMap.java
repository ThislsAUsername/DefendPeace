package Terrain;

import Units.InfantryModel;
import Units.Unit;
import Units.UnitModel;

public class GameMap {

	private Location[][] map;
	public final int mapWidth;
	public final int mapHeight;
	public CommandingOfficers.Commander[] commanders;

	//TODO: We need a way to load/use multiple maps; either:
	//  pass in a file name as a parameter here, or subclass
	//  GameMap for each map you want, or have a static data
	//  element with tile information for each available map
	public GameMap(CommandingOfficers.Commander[] COs)
	{
		commanders = COs;
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
					map[w][h] = new Location(Environment.getTile(Environment.Terrains.OCEAN, Environment.Weathers.CLEAR));
				}
				else
				{
					map[w][h] = new Location(Environment.getTile(Environment.Terrains.GRASS, Environment.Weathers.CLEAR));
				}
			}
		}
		// Throw down an HQ and a factory for each.
		map[1][8].setEnvironment(Environment.getTile(Environment.Terrains.HQ, Environment.Weathers.CLEAR));
		map[1][7].setEnvironment(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));
		map[2][7].setEnvironment(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));
		map[1][8].setOwner(commanders[0]);
		map[1][7].setOwner(commanders[0]);
//		map[2][7].setOwner(commanders[0]);

		map[13][1].setEnvironment(Environment.getTile(Environment.Terrains.HQ, Environment.Weathers.CLEAR));
		map[13][2].setEnvironment(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));
		map[12][2].setEnvironment(Environment.getTile(Environment.Terrains.FACTORY, Environment.Weathers.CLEAR));
		map[13][1].setOwner(commanders[1]);
		map[13][2].setOwner(commanders[1]);
//		map[12][2].setOwner(commanders[1]);

		// Add some cities and forests
		map[1][4].setEnvironment(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[2][5].setEnvironment(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[13][5].setEnvironment(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[12][4].setEnvironment(Environment.getTile(Environment.Terrains.CITY, Environment.Weathers.CLEAR));
		map[1][4].setOwner(commanders[0]);
//		map[2][5].setOwner(commanders[0]);
		map[13][5].setOwner(commanders[1]);
//		map[12][4].setOwner(commanders[1]);

		map[3][5].setEnvironment(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		map[6][5].setEnvironment(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		Unit n = new Unit(commanders[0], commanders[0].unitModels[UnitModel.UnitEnum.INFANTRY.ordinal()]);
		n.x = 6;
		n.y = 5;
		n.isTurnOver = false;
		map[6][5].setResident(n);
		commanders[0].units.add(n);
		map[11][4].setEnvironment(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		map[8][4].setEnvironment(Environment.getTile(Environment.Terrains.FOREST, Environment.Weathers.CLEAR));
		Unit n2 = new Unit(commanders[1], commanders[1].unitModels[UnitModel.UnitEnum.INFANTRY.ordinal()]);
		n2.x = 8;
		n2.y = 4;
		n2.isTurnOver = false;
		map[8][4].setResident(n2);
		commanders[1].units.add(n2);

		// Coupla shoals and reefs
		map[1][1].setEnvironment(Environment.getTile(Environment.Terrains.SHOAL, Environment.Weathers.CLEAR));
		map[13][8].setEnvironment(Environment.getTile(Environment.Terrains.SHOAL, Environment.Weathers.CLEAR));

		map[5][0].setEnvironment(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		map[9][0].setEnvironment(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		map[10][9].setEnvironment(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		map[6][9].setEnvironment(Environment.getTile(Environment.Terrains.REEF, Environment.Weathers.CLEAR));
		
		// Mountains
		map[3][2].setEnvironment(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));
		map[4][3].setEnvironment(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));
		map[10][6].setEnvironment(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));
		map[11][7].setEnvironment(Environment.getTile(Environment.Terrains.MOUNTAIN, Environment.Weathers.CLEAR));

		// Finally, add a road.
		map[4][6].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[5][6].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[6][6].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][6].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][5].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][4].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][3].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[7][3].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[8][3].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[9][3].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
		map[10][3].setEnvironment(Environment.getTile(Environment.Terrains.ROAD, Environment.Weathers.CLEAR));
	}
	
	/**
	 * Returns true if (x,y) lies within the GameMap, false else.
	 */
	public boolean isLocationValid(int x, int y)
	{
	  return !(x < 0 || x >= mapWidth || y < 0 || y >= mapHeight);
	}

	/** Returns the Environment of the specified tile, or null if that location does not exist. */
	public Environment getEnvironment(int w, int h)
	{
		if(!isLocationValid(w, h))
		{
			return null;
		}
		return map[w][h].getEnvironment();
	}

	/** Returns the Location at the specified location, or null if that Location does not exist. */
	public Location getLocation(int w, int h)
	{
		if(!isLocationValid(w, h))
		{
			return null;
		}
		return map[w][h];
	}

	public void clearAllHighlights()
	{
		for(int w = 0; w < mapWidth; ++w)
		{
			for(int h = 0; h < mapHeight; ++h)
			{
				map[w][h].setHighlight(false);
			}
		}
	}
	
	/** Returns true if no unit is at the specified x and y coordinate, false else */
	public boolean isLocationEmpty(int x, int y)
	{
	  return isLocationEmpty(null, x, y);
	}
	
	/** Returns true if no unit (excluding 'unit') is in the specified Location. */
	public boolean isLocationEmpty(Unit unit, int x, int y)
	{
	  boolean empty = true;
	  if(isLocationValid(x, y))
	  {
	    if(getLocation(x, y).getResident() != null && getLocation(x, y).getResident() != unit)
	    {
	      empty = false;
	    }
	  }
	  return empty;
	}

  public void addNewUnit(Unit unit, int x, int y)
  {
    if( getLocation(x, y).getResident() != null )
    {
      System.out.println("Error! Attempting to add a unit to an occupied Location!");
      return;
    }

    getLocation(x, y).setResident(unit);
    unit.x = x;
    unit.y = y;
  }

  public void moveUnit(Unit unit, int x, int y)
  {
    if( !isLocationEmpty(unit, x, y) )
    {
      System.out.println("ERROR! Attempting to move unit to an occupied Location!");
      return;
    }
    
    // Update the map
    Location priorLoc = getLocation(unit.x, unit.y);
    if(null != priorLoc)
    {
      priorLoc.setResident(null);
    }
    getLocation(x, y).setResident(unit);

    // Update the Unit
    unit.x = x;
    unit.y = y;
  }

  /** Removes the Unit from the map, if the map agrees with the Unit on its location. */ 
  public void removeUnit(Unit u)
  {
    if( isLocationValid(u.x, u.y) )
    {
      if(getLocation(u.x, u.y).getResident() != u)
      {
        System.out.println("WARNING! Trying to remove a Unit that isn't where he claims to be.");
      }
      else
      {
        getLocation(u.x, u.y).setResident(null);
      }
    }
  }
}
