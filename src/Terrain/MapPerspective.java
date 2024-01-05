package Terrain;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GamePath;
import Engine.GamePath.PathNode;
import Engine.Utils;
import Engine.XYCoord;
import Units.Unit;
import Units.UnitContext;

public class MapPerspective extends GameMap
{
  private static final long serialVersionUID = 1L;
  MapMaster master;
  public final Army viewer; // can be null
  private boolean[][] isFogged;
  private Commander[][] lastOwnerSeen;
  private ArrayList<Unit> confirmedVisibles;

  public MapPerspective(MapMaster pMaster, Army pViewer)
  {
    super(pMaster.mapWidth, pMaster.mapHeight);
    master = pMaster;
    viewer = pViewer;
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

  @Override
  public Unit getResident(XYCoord coord)
  {
    return getResident(coord.x, coord.y);
  }
  @Override
  public Unit getResident(int w, int h)
  {
    if( !isLocationValid(w, h) )
    {
      return null;
    }
    return getLocation(w, h).getResident();
  }

  /** Returns the MapLocation at the specified location, or null if that MapLocation does not exist. */
  @Override
  public MapLocation getLocation(XYCoord location)
  {
    if (null != location)
      return getLocation(location.x, location.y);
    return null;
  }

  /** Returns the MapLocation at the specified location, or null if that MapLocation does not exist. */
  @Override
  public MapLocation getLocation(int x, int y)
  {
    XYCoord coord = new XYCoord(x, y);
    MapLocation masterLoc = master.getLocation(coord);
    MapLocation returnLoc = masterLoc;
    if( isLocationFogged(coord) || // If we can't see anything...
        (isLocationEmpty(coord) && !master.isLocationEmpty(coord)) ) // ...or what's there is hidden
    {
      returnLoc = new MapLocation(returnLoc.getEnvironment(), coord);
      returnLoc.setOwner( lastOwnerSeen[x][y] );
    }
    return returnLoc;
  }

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  @Override
  public boolean isLocationEmpty(XYCoord coords)
  {
    return isLocationEmpty(null, coords.x, coords.y);
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
    return isLocationEmpty(unit, coords.x, coords.y);
  }

  /** Returns true if no unit (excluding 'unit') is in the specified MapLocation. */
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
    return isLocationFogged(coord.x, coord.y);
  }
  @Override
  public boolean isLocationFogged(int x, int y)
  {
    return ((x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) ? true : isFogged[x][y]);
  }
  public boolean isFogOn()
  {
    return master.game.isFogEnabled();
  }
  public boolean isFogDoR()
  {
    return master.game.rules.fogMode.dorMode;
  }

  /**
   * @return Whether the input unit is visible whether or not it's cloaked
   */
  public boolean isConfirmedVisible(Unit unit)
  {
    return confirmedVisibles.contains(unit);
  }

  @Override
  public void resetFog()
  {
    // Assume everything is fogged...
    confirmedVisibles.clear();
    boolean defaultState = isFogOn();
    for( int y = 0; y < mapHeight; ++y )
    {
      for( int x = 0; x < mapWidth; ++x )
      {
        isFogged[x][y] = defaultState;
      }
    }
    // then reveal what we should see
    if (null == viewer)
      return;
    for( Army army : master.game.armies )
    {
      if( viewer.isEnemy(army) )
        continue;
      for( Commander co : army.cos )
      {
        for( Unit unit : co.units )
        {
          UnitContext uc = new UnitContext(this, unit);
          uc.calculateVision();
          revealFog(uc, uc.coord);
        }
        for( XYCoord xyc : co.ownedProperties )
        {
          revealFog(xyc, true); // Properties can see themselves and anything on them
          if( isFogDoR() ) // Trilogy fog does not enable cities to have area vision
            for( XYCoord coord : Utils.findLocationsInRange(this, xyc, Environment.PROPERTY_VISION_RANGE) )
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
    if( !isFogDoR() )
    {
      resetFog();
      return;
    }
    if( !viewer.isEnemy(scout.CO.army) )
    {
      UnitContext uc = new UnitContext(this, scout);
      uc.calculateVision();
      revealFog(uc, uc.coord);
    }
  }

  @Override
  public void revealFog(Unit scout, GamePath movepath)
  {
    if (null == viewer)
      return;
    if( !isFogDoR() )
    {
      resetFog();
      return;
    }
    if( !viewer.isEnemy(scout.CO.army) )
    {
      UnitContext uc = new UnitContext(this, scout);
      for( PathNode node : movepath.getWaypoints() )
      {
        uc.setCoord(node.GetCoordinates());
        uc.calculateVision();
        revealFog(uc, uc.coord);
      }
    }
  }

  /**
   * Assumes the scout context has pre-calculated its vision.
   */
  protected void revealFog(UnitContext scout, XYCoord seeFrom)
  {
    int piercingRange = scout.visionPierces ? scout.visionRange : 1;
    for( XYCoord coord : Utils.findLocationsInRange(this, seeFrom, 0, piercingRange) )
    {
      revealFog(coord, true);
    }
    if( !scout.visionPierces )
      for( XYCoord coord : Utils.findLocationsInRange(this, seeFrom, piercingRange, scout.visionRange) )
      {
        revealFog(coord, false);
      }
  }

  public void revealFog(XYCoord coord, boolean piercing)
  {
    MapLocation loc = master.getLocation(coord);
    lastOwnerSeen[coord.x][coord.y] = loc.getOwner();
    Unit resident = loc.getResident();

    Environment env = loc.getEnvironment();
    boolean shouldSee = piercing || !env.terrainType.isCover();
    if( null != resident )
    {
      if( piercing )
        confirmedVisibles.add(resident);
      else if( !resident.model.hidden ) // Non-invisible aircraft reveal cover that can't repair them.
        if( resident.model.isAirUnit() && !env.terrainType.healsAir() )
          shouldSee = true;
    }
    if( shouldSee )
      isFogged[coord.x][coord.y] = false;
  }
}
