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
  int getTransitionCost(GameMap map, XYCoord from, XYCoord to);
  /**
   * Determines whether the Location (x, y), is a valid destination.
   */
  boolean canEnd(GameMap map, XYCoord end);


  /** Acts as the default way of calculating unit mobility */
  public static class BasicMoveFillFunctor implements FloodFillFunctor
  {
    public final Unit unit;
    public final MoveType propulsion;
    public final boolean includeOccupiedDestinations;
    public final boolean canTravelThroughEnemies;

    public BasicMoveFillFunctor(Unit mover, MoveType propulsion, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      unit = mover;
      this.propulsion = propulsion;
      this.includeOccupiedDestinations = includeOccupied;
      this.canTravelThroughEnemies = (null == unit)? true : canTravelThroughEnemies;
    }

    public int getTransitionCost(GameMap map, XYCoord from, XYCoord to)
    {
      // if we're past the edges of the map
      if( !map.isLocationValid(to) )
        return MoveType.IMPASSABLE;

      int cost = findMoveCost(from, to, map);

      // if there is an enemy unit in that space
      if( !canTravelThroughEnemies
          && (map.getLocation(to).getResident() != null)
          && unit.CO.isEnemy(map.getLocation(to).getResident().CO) )
        cost = MoveType.IMPASSABLE;

      return cost;
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
      return propulsion.getMoveCost(map.getEnvironment(to.xCoord, to.yCoord));
    }
  } // ~BasicMoveFillFunctor

}
