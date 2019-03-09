package Units.MoveTypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;

import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class MoveType implements Serializable
{
  // A 2-layer map. Map Weathers to a mapping of Terrains-to-cost.
  protected transient EnumMap<Weathers, MoveCostByTerrain> moveCosts;

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
    Integer cost = 99;
    MoveCostByTerrain mcbw = moveCosts.get(weather);
    if( null != mcbw )
    {
      cost = mcbw.get(terrain);
      if( null == cost )
      {
        cost = 99;
      }
    }
    return cost.intValue();
  }

  /** Returns the cost to traverse the given tile, accounting for its current terrain and weather types. */
  public int getMoveCost(Environment tile)
  {
    return getMoveCost(tile.weatherType, tile.terrainType);
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
    /** We don't actually serialize this map, but declaring this prevents a warning. */
    private static final long serialVersionUID = 2956827533548597328L;

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

    /**
     * Private method, same signature as in Serializable interface
     *
     * @param stream
     * @throws IOException
     */
    public void writeObject(ObjectOutputStream stream) throws IOException
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        stream.writeInt(get(terrain));
      }
    }

    /**
     * Private method, same signature as in Serializable interface
     *
     * @param stream
     * @throws IOException
     */
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        put(terrain, stream.readInt());
      }
    }
  } //~MoveCostByTerrain
  
  /**
   * Private method, same signature as in Serializable interface
   * Manifests as a flattened 2D array of integers; for each weather, for each terrain, what is my move cost? 
   *
   * @param stream
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      for( Weathers w : Weathers.values() )
      {
        moveCosts.get(w).writeObject(stream);
      }
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void readObject(ObjectInputStream stream)
          throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      
      moveCosts = new EnumMap<Weathers, MoveCostByTerrain>(Weathers.class);
      for( Weathers w : Weathers.values() )
      {
        MoveCostByTerrain noMoving = new MoveCostByTerrain(99);
        noMoving.readObject(stream); // initialize the values from our serialized file
        moveCosts.put(w, noMoving);
      }
  }
}
