package Terrain;

import CommandingOfficers.Commander;
import Engine.Path;
import Engine.Path.PathNode;
import Engine.Utils;
import Engine.XYCoord;
import Units.Unit;

public class MapWindow extends GameMap
{
  MapMaster master;
  Commander viewer; // can be null
  boolean isFogEnabled;
  private boolean[][] isFogged;
  private Commander[][] lastOwnerSeen;

  public MapWindow(MapMaster pMaster, Commander pViewer)
  {
    this( pMaster, pViewer, false);
  }

  public MapWindow(MapMaster pMaster, Commander pViewer, boolean fog)
  {
    super(pMaster.mapWidth, pMaster.mapHeight);
    master = pMaster;
    viewer = pViewer;
    commanders = master.commanders;
    isFogEnabled = fog;
    isFogged = new boolean[mapWidth][mapHeight];

    // We start with knowledge of what properties everyone starts with.
    lastOwnerSeen = new Commander[mapWidth][mapHeight];
    for( int w = 0; w < pMaster.mapWidth; ++w )
    {
      for( int h = 0; h < pMaster.mapHeight; ++h )
      {
        lastOwnerSeen[w][h] = pMaster.getLocation(w, h).getOwner();
      }
    }
  }

  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  public boolean isLocationValid(XYCoord coords)
  {
    return master.isLocationValid(coords);
  }

  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  public boolean isLocationValid(int x, int y)
  {
    return master.isLocationValid(x, y);
  }

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  public Environment getEnvironment(XYCoord coord)
  {
    return master.getEnvironment(coord);
  }
  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  public Environment getEnvironment(int w, int h)
  {
    return master.getEnvironment(w, h);
  }

  /** Returns the Location at the specified location, or null if that Location does not exist. */
  public Location getLocation(XYCoord location)
  {
    if (null != location)
      return getLocation(location.xCoord, location.yCoord);
    return null;
  }

  /** Returns the Location at the specified location, or null if that Location does not exist. */
  public Location getLocation(int x, int y)
  {
    XYCoord coord = new XYCoord(x, y);
    Location masterLoc = master.getLocation(coord);
    Location returnLoc = masterLoc;
    if( isLocationFogged(coord) )
    {
      returnLoc = new Location(returnLoc.getEnvironment(), coord);
      returnLoc.setHighlight(masterLoc.isHighlightSet());
      returnLoc.setOwner( lastOwnerSeen[x][y] );
    }
    return returnLoc;
  }

  public void clearAllHighlights()
  {
    master.clearAllHighlights();
  }

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  public boolean isLocationEmpty(XYCoord coords)
  {
    return isLocationFogged(coords) || master.isLocationEmpty(null, coords);
  }

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  public boolean isLocationEmpty(int x, int y)
  {
    return isLocationFogged(x, y) || master.isLocationEmpty(null, x, y);
  }

  /** Returns true if no unit (excluding 'unit') is in the specified Location. */
  public boolean isLocationEmpty(Unit unit, XYCoord coords)
  {
    return isLocationFogged(coords) || master.isLocationEmpty(unit, coords.xCoord, coords.yCoord);
  }

  /** Returns true if no unit (excluding 'unit') is in the specified Location. */
  public boolean isLocationEmpty(Unit unit, int x, int y)
  {
    return isLocationFogged(x, y) || master.isLocationEmpty(unit, x, y);
  }

  /**
   * Returns true if the location lies outside the GameMap.
   */
  public boolean isLocationFogged(XYCoord coord)
  {
    return isLocationFogged(coord.xCoord, coord.yCoord);
  }
  public boolean isLocationFogged(int x, int y)
  {
    return isFogEnabled && ((x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) ? true : isFogged[x][y]);
  }

  public void resetFog()
  {
    // assume everything is fogged
    for( int y = 0; y < mapHeight; ++y )
    {
      for( int x = 0; x < mapWidth; ++x )
      {
        isFogged[x][y] = true;
      }
    }
    // then reveal what we should see
    if (null == viewer)
      return;
    for( Commander co : commanders )
    {
      if( !viewer.isEnemy(co) )
      {
        for( Unit unit : co.units )
        {
          for( XYCoord coord : Utils.findVisibleLocations(this, unit) )
          {
            isFogged[coord.xCoord][coord.yCoord] = false;
            lastOwnerSeen[coord.xCoord][coord.yCoord] = master.getLocation(coord).getOwner();
          }
        }
        for( XYCoord xyc : co.ownedProperties )
        {
          Location loc = master.getLocation(xyc);
          for( XYCoord coord : Utils.findVisibleLocations(this, loc.getCoordinates(), Environment.PROPERTY_VISION_RANGE) )
          {
            isFogged[coord.xCoord][coord.yCoord] = false;
            lastOwnerSeen[coord.xCoord][coord.yCoord] = master.getLocation(coord).getOwner();
          }
        }
      }
    }
  }

  public void revealFog(Unit scout)
  {
    if (null == viewer)
      return;
    if( !viewer.isEnemy(scout.CO) )
    {
      for( XYCoord coord : Utils.findVisibleLocations(this, scout, scout.x, scout.y) )
      {
        isFogged[coord.xCoord][coord.yCoord] = false;
        lastOwnerSeen[coord.xCoord][coord.yCoord] = master.getLocation(coord).getOwner();
      }
    }
  }

  public void revealFog(Unit scout, Path movepath)
  {
    if (null == viewer)
      return;
    if( !viewer.isEnemy(scout.CO) )
    {
      for( PathNode node : movepath.getWaypoints() )
      {
        for( XYCoord coord : Utils.findVisibleLocations(this, scout, node.x, node.y) )
        {
          isFogged[coord.xCoord][coord.yCoord] = false;
          lastOwnerSeen[coord.xCoord][coord.yCoord] = master.getLocation(coord).getOwner();
        }
      }
    }
  }
}
