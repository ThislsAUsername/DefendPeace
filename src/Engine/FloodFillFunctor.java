package Engine;

import Terrain.GameMap;
import Units.Unit;
import Units.MoveTypes.MoveType;

/**
 * Provides a stateless interface for determining traversability
 * Used in {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath()} and {@link #findPossibleDestinations(XYCoord, Unit, GameMap, boolean) findPossibleDestinations()}
 */
public interface FloodFillFunctor
{
  /**
   * Determines whether the Location (x, y), can be traveled through.
   * @param ignoreUnits If set, this function will ignore enemy-unit presence.
   */
  int getRemainingFillPower(GameMap map, int initialFillPower, XYCoord from, XYCoord to);
  /**
   * Determines whether the Location (x, y), is a valid destination.
   */
  boolean canEnd(GameMap map, XYCoord end);


  /** Acts as the default way of calculating unit mobility */
  public static class BasicMoveFillFunctor implements FloodFillFunctor
  {
    public final Unit unit;
    public final boolean includeOccupiedDestinations;
    public final boolean canTravelThroughEnemies;

    public BasicMoveFillFunctor(Unit mover, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      unit = mover;
      this.includeOccupiedDestinations = includeOccupied;
      this.canTravelThroughEnemies = canTravelThroughEnemies;
    }

    public int getRemainingFillPower(GameMap map, int initialFillPower, XYCoord from, XYCoord to)
    {
      int cost = findMoveCost(from, to, map);

      // if we're past the edges of the map
      if( !map.isLocationValid(to) )
        cost = MoveType.IMPASSABLE;

      // if there is an enemy unit in that space
      if( !canTravelThroughEnemies
          && (map.getLocation(to).getResident() != null)
          && unit.CO.isEnemy(map.getLocation(to).getResident().CO) )
        cost = MoveType.IMPASSABLE;

      return initialFillPower - cost;
    }

    public boolean canEnd(GameMap map, XYCoord end)
    {
      Unit obstacle = map.getLocation(end).getResident();
      if( obstacle == null || obstacle == unit)
        return true;
      return includeOccupiedDestinations;
    }

    public int findMoveCost(XYCoord from, XYCoord to, GameMap map)
    {
      return unit.model.propulsion.getMoveCost(map.getEnvironment(to.xCoord, to.yCoord));
    }
  } // ~BasicMoveFillFunctor

}
