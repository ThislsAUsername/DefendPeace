package Units.MoveTypes;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;

import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class MoveType implements Serializable
{
  private static final long serialVersionUID = 1L;

  protected final Integer IMPASSABLE = 99;

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

  public MoveType( MoveType other )
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

  /** Returns the cost to traverse the given tile, accounting for its current terrain and weather types. */
  public int getMoveCost(Environment tile)
  {
    return getMoveCost(tile.weatherType, tile.terrainType);
  }

  /** Returns whether the unit can travel in the specified environment. */
  public boolean canTraverse( Environment tile )
  {
    return getMoveCost(tile) < IMPASSABLE;
  }

  /** Sets the cost to move through terrain during weather. */
  public void setMoveCost(Weathers weather, TerrainType terrain, int cost)
  {
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
