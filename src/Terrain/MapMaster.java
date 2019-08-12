package Terrain;

import java.util.Map;
import java.util.Map.Entry;

import Engine.XYCoord;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class MapMaster extends GameMap
{
  private static final long serialVersionUID = 1L;
  private Location[][] map;
  public CommandingOfficers.Commander[] commanders;

  private boolean initOK = false;

  public MapMaster(CommandingOfficers.Commander[] COs, MapInfo mapInfo)
  {
    super(mapInfo.getWidth(), mapInfo.getHeight());
    initOK = true;
    commanders = COs;
    map = new Location[mapWidth][mapHeight];

    // Build the map locations based on the MapInfo data.
    for( int y = 0; y < mapHeight; ++y )
    {
      for( int x = 0; x < mapWidth; ++x )
      {
        TerrainType terrain = mapInfo.terrain[x][y];
        // Create this Location using the MapInfo terrain.
        map[x][y] = new Location(Environment.getTile(terrain, Environment.Weathers.CLEAR), new XYCoord(x, y));
      }
    }

    // Print a warning if the number of Commanders we have does not match the number the map expects.
    if( COs.length != mapInfo.COProperties.length )
    {
      System.out.println("Warning! Wrong number of COs specified for map " + mapInfo.mapName);
      initOK = false;
    }
    if( (mapInfo.mapUnits.size() > 0) && (mapInfo.mapUnits.size() != COs.length ) )
    {
      System.out.println("Warning! Wrong number of unit arrays specified for map " + mapInfo.mapName);
      System.out.println(String.format("         Expected zero or %s; received %s", COs.length, mapInfo.mapUnits.size()));
      initOK = false;
    }

    // Assign properties according to MapInfo's direction.
    for( int co = 0; co < mapInfo.COProperties.length && co < COs.length; ++co )
    {
      // Loop through all locations assigned to this CO by mapInfo.
      for( int i = 0; i < mapInfo.COProperties[co].length; ++i )
      {
        // If the location can be owned, make the assignment.
        XYCoord coord = mapInfo.COProperties[co][i];
        int x = coord.xCoord;
        int y = coord.yCoord;
        Location location = map[x][y];
        if( location.isCaptureable() )
        {
          // Check if this location holds an HQ.
          if( map[x][y].getEnvironment().terrainType == TerrainType.HEADQUARTERS )
          {
            // If the CO has no HQ yet, assign this one.
            if( COs[co].HQLocation == null )
            {
              System.out.println("Assigning HQ at " + x + ", " + y + " to " + COs[co]);
              COs[co].HQLocation = new XYCoord(x, y);
            }
            // If the CO does have an HQ, turn this location into a city.
            else
            {
              location.setEnvironment(Environment.getTile(TerrainType.CITY, location.getEnvironment().weatherType));
            }
          }
          location.setOwner(COs[co]);
        }
        else
        {
          System.out.println("Warning! CO specified as owner of an uncapturable location in map " + mapInfo.mapName);
        }
      }

      if( !mapInfo.mapUnits.isEmpty() )
      {
        Map<XYCoord, UnitEnum> unitSet = mapInfo.mapUnits.get(co);
        for( Entry<XYCoord, UnitEnum> unitEntry : unitSet.entrySet() )
        {
          Unit unit = new Unit(commanders[co], commanders[co].getUnitModel(unitEntry.getValue()));
          addNewUnit(unit, unitEntry.getKey().xCoord, unitEntry.getKey().yCoord);
          commanders[co].units.add(unit);
        }
      }

      // Warn if the CO still doesn't have a valid HQ.
      if( COs[co].HQLocation == null )
      {
        System.out.println("Warning! CO " + co + " does not have any HQ assigned!");
        initOK = false;
        break;
      }
    }
  }

  /**
   * Used to check if the GameMap is ready to be played after constructing.
   */
  public boolean initOK()
  {
    return initOK;
  }

  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  @Override
  public boolean isLocationValid(XYCoord coords)
  {
    return (coords != null) && isLocationValid(coords.xCoord, coords.yCoord);
  }

  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  @Override
  public boolean isLocationValid(int x, int y)
  {
    return !(x < 0 || x >= mapWidth || y < 0 || y >= mapHeight);
  }

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  @Override
  public Environment getEnvironment(XYCoord coord)
  {
    return getEnvironment(coord.xCoord, coord.yCoord);
  }
  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  @Override
  public Environment getEnvironment(int w, int h)
  {
    if( !isLocationValid(w, h) )
    {
      return null;
    }
    return map[w][h].getEnvironment();
  }

  /** Returns the Location at the specified location, or null if that Location does not exist. */
  @Override
  public Location getLocation(XYCoord location)
  {
    if (null != location)
      return getLocation(location.xCoord, location.yCoord);
    return null;
  }

  /** Returns the Location at the specified location, or null if that Location does not exist. */
  @Override
  public Location getLocation(int w, int h)
  {
    if( !isLocationValid(w, h) )
    {
      return null;
    }
    return map[w][h];
  }

  @Override
  public void clearAllHighlights()
  {
    for( int w = 0; w < mapWidth; ++w )
    {
      for( int h = 0; h < mapHeight; ++h )
      {
        map[w][h].setHighlight(false);
      }
    }
  }

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  @Override
  public boolean isLocationEmpty(XYCoord coords)
  {
    return isLocationEmpty(null, coords);
  }

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  @Override
  public boolean isLocationEmpty(int x, int y)
  {
    return isLocationEmpty(null, x, y);
  }

  /** Returns true if no unit (excluding 'unit') is in the specified Location. */
  @Override
  public boolean isLocationEmpty(Unit unit, XYCoord coords)
  {
    return isLocationEmpty(unit, coords.xCoord, coords.yCoord);
  }

  /** Returns true if no unit (excluding 'unit') is in the specified Location. */
  @Override
  public boolean isLocationEmpty(Unit unit, int x, int y)
  {
    boolean empty = true;
    if( isLocationValid(x, y) )
    {
      if( getLocation(x, y).getResident() != null && getLocation(x, y).getResident() != unit )
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
    if( unit.x == x && unit.y == y )
    {
      // We are not actually moving. Just return.
      // This will happen e.g. if we attack an enemy that is already adjacent.
      return;
    }

    if( !isLocationEmpty(unit, x, y) )
    {
      System.out.println("ERROR! Attempting to move unit to an occupied Location!");
      return;
    }

    // Update the map
    Location priorLoc = getLocation(unit.x, unit.y);
    if( null != priorLoc )
    {
      priorLoc.setResident(null);
    }
    getLocation(x, y).setResident(unit);

    // Reset capture progress, since we moved.
    if( unit.getCaptureProgress() > 0 )
    {
      unit.stopCapturing();
    }

    // Update the Unit location.
    unit.x = x;
    unit.y = y;
  }

  /** Removes the Unit from the map, if the map agrees with the Unit on its location. */
  public void removeUnit(Unit u)
  {
    if( isLocationValid(u.x, u.y) )
    {
      if( getLocation(u.x, u.y).getResident() != u )
      {
        System.out.println("WARNING! Trying to remove a Unit that isn't where he claims to be.");
      }
      else
      {
        // Get the unit off the map.
        getLocation(u.x, u.y).setResident(null);

        // Tell the unit he's off the map.
        u.x = -1;
        u.y = -1;

        // Reset capture progress if needed.
        if( u.getCaptureProgress() > 0 )
        {
          u.stopCapturing();
        }
      }
    }
  }
  
  /**
   * Returns true if the location lies outside the GameMap.
   * False otherwise
   */
  @Override
  public boolean isLocationFogged(XYCoord coord)
  {
    return isLocationFogged(coord.xCoord, coord.yCoord);
  }
  @Override
  public boolean isLocationFogged(int x, int y)
  {
    return (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) ? true : false;
  }
}
