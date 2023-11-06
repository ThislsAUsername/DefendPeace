package Units.MoveTypes;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;

import Engine.Army;
import Engine.XYCoord;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.Environment.Weathers;
import Terrain.TerrainType;
import Units.Unit;

public class MoveType implements Serializable
{
  private static final long serialVersionUID = 1L;

  public final static Integer IMPASSABLE = 99;

  // A 2-layer map. Map Weathers to a mapping of Terrains-to-cost.
  protected EnumMap<Weathers, MoveCostByTerrain> moveCosts;

  /** Default constructor to prohibit movement. This will make it obvious fairly
      quickly if a subclass fails to initialize properly.                         */
  public MoveType()
  {
    moveCosts = new EnumMap<Weathers, MoveCostByTerrain>(Weathers.class);
    for( Weathers w : Weathers.values() )
    {
      MoveCostByTerrain noMoving = new MoveCostByTerrain(99);
      moveCosts.put(w, noMoving);
    }
  }

  public MoveType clone()
  {
    return new MoveType(this);
  }

  protected MoveType(MoveType other)
  {
    // Initialize
    this();

    // Copy all values from the other object.
    for( Weathers w : moveCosts.keySet() )
    {
      for( TerrainType t : moveCosts.get(w).keySet() )
      {
        moveCosts.get(w).put(t, other.getMoveCost(w, t));
      }
    }
  }

  /** Returns the cost to traverse terrain type 'terrain' while experiencing weather 'weather'. */
  public int getMoveCost(Weathers weather, TerrainType terrain)
  {
    Integer cost = IMPASSABLE;
    MoveCostByTerrain mcbw = moveCosts.get(weather);
    if( null != mcbw )
    {
      cost = mcbw.get(terrain);
      if( null == cost )
      {
        cost = IMPASSABLE;
      }
    }
    return cost.intValue();
  }

  public int getTransitionCost(GameMap map, XYCoord from, XYCoord to,
                               Army team, boolean canTravelThroughEnemies)
  {
    // if we're past the edges of the map
    if( !map.isLocationValid(to) )
      return MoveType.IMPASSABLE;

    // note to self: extend this if we ever support moving to non-adjacent tiles
    int cost = getMoveCost(map.getEnvironment(to.xCoord, to.yCoord));

    if( !canTravelThroughEnemies )
    {
      final Unit blocker = map.getLocation(to).getResident();
      // if there is an enemy unit in that space
      if( blocker != null && blocker.CO.isEnemy(team) )
        cost = MoveType.IMPASSABLE;
    }

    return cost;
  }

  /** Returns the cost to traverse the given tile, accounting for its current terrain and weather types. */
  public int getMoveCost(Environment tile)
  {
    return getMoveCost(tile.weatherType, tile.terrainType);
  }

  /** Returns whether the unit can hang out in the specified environment. */
  public boolean canStandOn(Environment tile)
  {
    if( tile.terrainType == TerrainType.TELETILE )
      return false; // Standing on empty space is pretty hard
    return getMoveCost(tile) < IMPASSABLE;
  }
  /** Returns whether the unit can hang out on the specified tile. */
  public boolean canStandOn(GameMap map, XYCoord end, Unit mover, boolean includeOccupiedDestinations)
  {
    final MapLocation loc = map.getLocation(end);
    if(!canStandOn(loc.getEnvironment()))
      return false;

    Unit obstacle = loc.getResident();
    if( obstacle == null || obstacle == mover)
      return true;
    return includeOccupiedDestinations;
  }

  /** Sets the cost to move through terrain during weather. */
  public void setMoveCost(Weathers weather, TerrainType terrain, int cost)
  {
    if( cost > IMPASSABLE )
      cost = IMPASSABLE;
    moveCosts.get(weather).put(terrain, cost);
  }

  /** Set the move cost for this terrain for all weather conditions. Useful for marking a terrain as impassable. */
  public void setMoveCost(TerrainType terrain, int cost)
  {
    for( Weathers w : Weathers.values() )
    {
      moveCosts.get(w).setMoveCost(terrain, cost);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  /** Convenience class to allow easy manipulation of move costs. */
  protected class MoveCostByTerrain extends HashMap<TerrainType, Integer>
  {
    private static final long serialVersionUID = 1L;

    public MoveCostByTerrain(int moveCost)
    {
      setAllMovementCosts(moveCost);
    }

    /** Copy-constructor. Adopt all values from the provided map. */
    public MoveCostByTerrain(MoveCostByTerrain other)
    {
      super(other);
    }

    /** Set cost to traverse a specific terrain type. */
    public void setMoveCost(TerrainType t, int c)
    {
      put(t, c);
    }

    /** Helper function to set all movement costs to the same value. */
    public void setAllMovementCosts(int moveCost)
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        setMoveCost(terrain, moveCost);
      }
    }

    /** Set all ground tile types to the given move cost. */
    public void setAllLandCosts(int moveCost)
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        if( terrain.isLand() )
          setMoveCost(terrain, moveCost);
      }
    }

    /** Set all ground tile types to the given move cost. */
    public void setAllSeaCosts(int moveCost)
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        if( terrain.isWater() )
          setMoveCost(terrain, moveCost);
      }
    }
  } //~MoveCostByTerrain
}
