package Units.MoveTypes;

import java.util.EnumMap;

import Terrain.Environment;
import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class MoveType
{
  // A 2-layer map. Map Weathers to a mapping of Terrains-to-cost.
  protected EnumMap<Weathers, MoveCostByTerrain> moveCosts;

  /** Default constructor to prohibit movement. This will make it obvious fairly
      quickly if a subclass fails to initialize properly.                         */
  public MoveType()
  {
    moveCosts = new EnumMap<Weathers, MoveCostByTerrain>(Weathers.class);
    for( Weathers w : Weathers.values() )
    {
      MoveCostByTerrain noMoving = new MoveCostByTerrain(Terrains.class, 99);
      moveCosts.put(w, noMoving);
    }
  }

  /** Returns the cost to traverse terrain type 'terrain' while experiencing weather 'weather'. */
  public int getMoveCost(Weathers weather, Terrains terrain)
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

  /** Returns the cost to traverse the given tile, accounting for its current terain and weather types. */
  public int getMoveCost(Environment tile)
  {
    return getMoveCost(tile.weatherType, tile.terrainType);
  }

  /** Sets the cost to move through terrain during weather. */
  protected void setMoveCost(Weathers weather, Terrains terrain, int cost)
  {
    moveCosts.get(weather).put(terrain, cost);
  }

  /** Set the move cost for this terrain for all weather conditions. Useful for marking a terrain as impassable. */
  protected void setMoveCost(Terrains terrain, int cost)
  {
    for( Weathers w : Weathers.values() )
    {
      moveCosts.get(w).setMoveCost(terrain, cost);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  /** Convenience class to allow easy manipulation of move costs. */
  protected class MoveCostByTerrain extends EnumMap<Terrains, Integer>
  {
    /** We don't actually serialize this map, but declaring this prevents a warning. */
    private static final long serialVersionUID = 2956827533548597328L;

    public MoveCostByTerrain(Class<Terrains> enumClass, int moveCost)
    {
      super(enumClass);
      setAllMovementCosts(moveCost);
    }

    /** Copy-constructor. Adopt all values from the provided map. */
    public MoveCostByTerrain(MoveCostByTerrain other)
    {
      super(other);
    }

    /** Set cost to traverse a specific terrain type. */
    public void setMoveCost(Terrains t, int c)
    {
      put(t, c);
    }

    /** Helper function to set all movement costs to the same value. */
    public void setAllMovementCosts(int moveCost)
    {
      for( Terrains tn : Terrains.values() )
      {
        put(tn, moveCost);
      }
    }

    /** Set all ground tile types to the given move cost. */
    public void setAllLandCosts(int moveCost)
    {
      put(Terrains.AIRPORT, moveCost);
      put(Terrains.BRIDGE, moveCost);  // Note that bridges are both land and sea.
      put(Terrains.CITY, moveCost);
      put(Terrains.DUNES, moveCost);
      put(Terrains.FACTORY, moveCost);
      put(Terrains.FOREST, moveCost);
      put(Terrains.GRASS, moveCost);
      put(Terrains.HQ, moveCost);
      put(Terrains.LAB, moveCost);
      put(Terrains.MOUNTAIN, moveCost);
      put(Terrains.ROAD, moveCost);
      put(Terrains.SEAPORT, moveCost); // Note that seaports are both land and sea.
      put(Terrains.SHOAL, moveCost); // Note that shoals are both land and sea.
    }

    /** Set all ground tile types to the given move cost. */
    public void setAllSeaCosts(int moveCost)
    {
      put(Terrains.BRIDGE, moveCost); // Note that bridges are both land and sea.
      put(Terrains.REEF, moveCost);
      put(Terrains.SEA, moveCost);
      put(Terrains.SEAPORT, moveCost); // Note that seaports are both land and sea.
      put(Terrains.SHOAL, moveCost); // Note that shoals are both land and sea.
    }
  }
}
