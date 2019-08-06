package Terrain;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.Path;
import Engine.Path.PathNode;
import Engine.Utils;
import Engine.XYCoord;
import Units.Unit;

public class MapWindow extends GameMap
{
  private static final long serialVersionUID = 1L;
  MapMaster master;
  Commander viewer; // can be null
  boolean isFogEnabled;
  private boolean[][] isFogged;
  private Commander[][] lastOwnerSeen;
  private ArrayList<Unit> confirmedVisibles;

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
    confirmedVisibles = new ArrayList<Unit>();

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
  @Override
  public boolean isLocationValid(XYCoord coords)
  {
    return master.isLocationValid(coords);
  }

  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  @Override
  public boolean isLocationValid(int x, int y)
  {
    return master.isLocationValid(x, y);
  }

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  @Override
  public Environment getEnvironment(XYCoord coord)
  {
    return master.getEnvironment(coord);
  }
  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  @Override
  public Environment getEnvironment(int w, int h)
  {
    return master.getEnvironment(w, h);
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
  public Location getLocation(int x, int y)
  {
    XYCoord coord = new XYCoord(x, y);
    Location masterLoc = master.getLocation(coord);
    Location returnLoc = masterLoc;
    if( isLocationFogged(coord) || // If we can't see anything...
        (isLocationEmpty(coord) && !master.isLocationEmpty(coord)) ) // ...or what's there is hidden
    {
      returnLoc = new Location(returnLoc.getEnvironment(), coord);
      returnLoc.setHighlight(masterLoc.isHighlightSet());
      returnLoc.setOwner( lastOwnerSeen[x][y] );
    }
    return returnLoc;
  }

  @Override
  public void clearAllHighlights()
  {
    master.clearAllHighlights();
  }

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  @Override
  public boolean isLocationEmpty(XYCoord coords)
  {
    return isLocationEmpty(null, coords.xCoord, coords.yCoord);
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
    Unit resident = master.getLocation(x, y).getResident();
    // if there's nothing there, yeah...
    if (resident == null)
      return true;
    // say it's not there if we dunno it's there
    if (resident.model.hidden && !confirmedVisibles.contains(resident)) 
      return true;
    // otherwise, consult the fog map and master map
    return isLocationFogged(x, y) || master.isLocationEmpty(unit, x, y);
  }

  /**
   * Returns true if the location lies outside the GameMap.
   */
  @Override
  public boolean isLocationFogged(XYCoord coord)
  {
    return isLocationFogged(coord.xCoord, coord.yCoord);
  }
  @Override
  public boolean isLocationFogged(int x, int y)
  {
    return isFogEnabled && ((x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) ? true : isFogged[x][y]);
  }

  @Override
  public void resetFog()
  {
    // assume everything is fogged
    confirmedVisibles.clear();
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
          for( XYCoord coord : Utils.findVisibleLocations(this, unit, false) )
          {
            revealFog(coord, false);
          }
          // We need to do a second pass with piercing vision so we can know whether to reveal the units
          for( XYCoord coord : Utils.findVisibleLocations(this, unit, true) )
          {
            revealFog(coord, true);
          }
        }
        for( XYCoord xyc : co.ownedProperties )
        {
          revealFog(xyc, true); // Properties can see themselves and anything on them
          Location loc = master.getLocation(xyc);
          for( XYCoord coord : Utils.findVisibleLocations(this, loc.getCoordinates(), Environment.PROPERTY_VISION_RANGE) )
          {
            revealFog(coord, false);
          }
        }
      }
    }
  }

  @Override
  public void revealFog(Unit scout)
  {
    if (null == viewer)
      return;
    if( !viewer.isEnemy(scout.CO) )
    {
      for( XYCoord coord : Utils.findVisibleLocations(this, scout, false) )
      {
        revealFog(coord, true);
      }
      // We need to do a second pass with piercing vision so we can know whether to reveal the units
      for( XYCoord coord : Utils.findVisibleLocations(this, scout, true) )
      {
        revealFog(coord, true);
      }
    }
  }

  @Override
  public void revealFog(Unit scout, Path movepath)
  {
    if (null == viewer)
      return;
    if( !viewer.isEnemy(scout.CO) )
    {
      for( PathNode node : movepath.getWaypoints() )
      {
        for( XYCoord coord : Utils.findVisibleLocations(this, scout, node.x, node.y, false) )
        {
          revealFog(coord, false);
        }
        // We need to do a second pass with piercing vision so we can know whether to reveal the units
        for( XYCoord coord : Utils.findVisibleLocations(this, scout, node.x, node.y, true) )
        {
          revealFog(coord, true);
        }
      }
    }
  }
  
  public void revealFog(XYCoord coord, boolean piercing)
  {
    Location loc = master.getLocation(coord);
    isFogged[coord.xCoord][coord.yCoord] = false;
    lastOwnerSeen[coord.xCoord][coord.yCoord] = loc.getOwner();
    if (piercing && loc.getResident() != null)
      confirmedVisibles.add(loc.getResident());
  }
}
