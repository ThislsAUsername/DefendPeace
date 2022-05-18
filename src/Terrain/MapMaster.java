package Terrain;

import java.util.Map;
import java.util.Map.Entry;

import Engine.Army;
import Engine.XYCoord;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModelScheme;

public class MapMaster extends GameMap
{
  private static final long serialVersionUID = 1L;
  private MapLocation[][] map;

  private boolean initOK = false;

  public MapMaster(Army[] propertyOwners, MapInfo mapInfo)
  {
    super(mapInfo.getWidth(), mapInfo.getHeight());
    initOK = true;
    map = new MapLocation[mapWidth][mapHeight];

    // Build the map locations based on the MapInfo data.
    for( int y = 0; y < mapHeight; ++y )
    {
      for( int x = 0; x < mapWidth; ++x )
      {
        TerrainType terrain = mapInfo.terrain[x][y];
        // Create this MapLocation using the MapInfo terrain.
        map[x][y] = new MapLocation(Environment.getTile(terrain, Environment.Weathers.CLEAR), new XYCoord(x, y));
      }
    }

    // Print a warning if the number of Commanders we have does not match the number the map expects.
    if( propertyOwners.length != mapInfo.COProperties.length )
    {
      System.out.println("Warning! Wrong number of COs specified for map " + mapInfo.mapName);
      initOK = false;
    }
    if( (mapInfo.mapUnits.size() > 0) && (mapInfo.mapUnits.size() != propertyOwners.length ) )
    {
      System.out.println("Warning! Wrong number of unit arrays specified for map " + mapInfo.mapName);
      System.out.println(String.format("         Expected zero or %s; received %s", propertyOwners.length, mapInfo.mapUnits.size()));
      initOK = false;
    }

    // Assign properties according to MapInfo's direction.
    for( int co = 0; co < mapInfo.COProperties.length && co < propertyOwners.length; ++co )
    {
      boolean hasHQ = false, hasLab = false, hasProperty = false;
      // Loop through all locations assigned to this CO by mapInfo.
      for( int i = 0; i < mapInfo.COProperties[co].length; ++i )
      {
        // If the location can be owned, make the assignment.
        XYCoord coord = mapInfo.COProperties[co][i];
        int x = coord.xCoord;
        int y = coord.yCoord;
        MapLocation location = map[x][y];
        if( location.isCaptureable() )
        {
          hasProperty = true;
          final TerrainType terrainType = map[x][y].getEnvironment().terrainType;
          if( terrainType == TerrainType.HEADQUARTERS )
          {
            propertyOwners[co].HQLocations.add(new XYCoord(x, y));
            hasHQ = true;
          }
          else if( terrainType == TerrainType.LAB )
          {
            hasLab = true;
          }
          location.setOwner(propertyOwners[co].cos[0]);
        }
        else
        {
          System.out.println("Warning! CO specified as owner of an uncapturable location in map " + mapInfo.mapName);
        }
      }

      if( !mapInfo.mapUnits.isEmpty() )
      {
        Map<XYCoord, String> unitSet = mapInfo.mapUnits.get(co);
        for( Entry<XYCoord, String> unitEntry : unitSet.entrySet() )
        {
          UnitModel model = UnitModelScheme.getModelFromString(unitEntry.getValue(), propertyOwners[co].cos[0].unitModels);
          if( model != null )
          {
            Unit unit = new Unit(propertyOwners[co].cos[0], model);
            addNewUnit(unit, unitEntry.getKey().xCoord, unitEntry.getKey().yCoord);
            propertyOwners[co].cos[0].units.add(unit);
          }
          else
          {
            System.out.println("Warning! Invalid unit " + unitEntry.getValue() + " specified in map " + mapInfo.mapName);
          }
        }
      }

      if( hasHQ )
        continue; // If we have an HQ already, we know what our most strategic assets are

      // If we don't have an HQ and do have Labs, then Labs are our HQs
      if( hasLab )
        for( XYCoord coord : propertyOwners[co].cos[0].ownedProperties )
        {
          if( getEnvironment(coord).terrainType == TerrainType.LAB )
          {
            propertyOwners[co].HQLocations.add(coord);
          }
        }
      // If we own some property, call that good enough for our purposes
      else if( hasProperty )
        propertyOwners[co].HQLocations.addAll(propertyOwners[co].cos[0].ownedProperties);
      // If we don't have property, use our first unit's starting location
      else if( !propertyOwners[co].cos[0].units.isEmpty() )
        propertyOwners[co].HQLocations.add(new XYCoord(propertyOwners[co].cos[0].units.get(0)));
      // If we don't even have units, we've already lost
      else
        propertyOwners[co].isDefeated = true;

    } // ~property assignment loop
  } // ~constructor

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

  @Override
  public Unit getResident(XYCoord coord)
  {
    return getResident(coord.xCoord, coord.yCoord);
  }
  @Override
  public Unit getResident(int w, int h)
  {
    if( !isLocationValid(w, h) )
    {
      return null;
    }
    return map[w][h].getResident();
  }

  /** Returns the MapLocation at the specified location, or null if that MapLocation does not exist. */
  @Override
  public MapLocation getLocation(XYCoord location)
  {
    if (null != location)
      return getLocation(location.xCoord, location.yCoord);
    return null;
  }

  /** Returns the MapLocation at the specified location, or null if that MapLocation does not exist. */
  @Override
  public MapLocation getLocation(int w, int h)
  {
    if( !isLocationValid(w, h) )
    {
      return null;
    }
    return map[w][h];
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

  /** Returns true if no unit (excluding 'unit') is in the specified MapLocation. */
  @Override
  public boolean isLocationEmpty(Unit unit, XYCoord coords)
  {
    return isLocationEmpty(unit, coords.xCoord, coords.yCoord);
  }

  /** Returns true if no unit (excluding 'unit') is in the specified MapLocation. */
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
    addNewUnit(unit, x, y, false);
  }
  public void addNewUnit(Unit unit, int x, int y, boolean force)
  {
    Unit resident = getLocation(x, y).getResident();
    if( resident != null && !force )
    {
      System.out.println("Error! Attempting to add a unit to an occupied MapLocation!");
      return;
    }

    if( resident != null ) removeUnit(resident);

    getLocation(x, y).setResident(unit);
    unit.x = x;
    unit.y = y;
  }

  public void moveUnit(Unit unit, int x, int y)
  {
    moveUnit(unit, x, y, false);
  }

  public void moveUnit(Unit unit, int x, int y, boolean force)
  {
    if( unit.x == x && unit.y == y )
    {
      // We are not actually moving. Just return.
      // This will happen e.g. if we attack an enemy that is already adjacent.
      return;
    }

    if( !isLocationEmpty(unit, x, y) )
    {
      if( force ) // Force is set; the user *must* know what he's doing.
      {
        removeUnit(getLocation(x, y).getResident());
      }
      else
      {
        System.out.println("ERROR! Attempting to move unit to an occupied MapLocation!");
        return;
      }
    }

    // Update the map
    MapLocation priorLoc = getLocation(unit.x, unit.y);
    if( null != priorLoc && priorLoc.getResident() == unit )
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
