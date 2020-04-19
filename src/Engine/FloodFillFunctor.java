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
  int getRemainingFillPower(GameMap map, int initialFillPower, XYCoord from, XYCoord to, boolean ignoreUnits);
  /**
   * Determines whether the Location (x, y), is a valid destination.
   */
  boolean canEnd(GameMap map, XYCoord end);


  /** Acts as the default way of calculating unit mobility */
  public static class BasicMoveFillFunctor implements FloodFillFunctor
  {
    private final Unit unit;

    public BasicMoveFillFunctor(Unit mover)
    {
      unit = mover;
    }

    public int getRemainingFillPower(GameMap map, int initialFillPower, XYCoord from, XYCoord to, boolean ignoreUnits)
    {
      // if we're past the edges of the map
      if( !map.isLocationValid(to) )
        return -1;

      // if there is an enemy unit in that space
      if( !ignoreUnits && (map.getLocation(to).getResident() != null)
          && unit.CO.isEnemy(map.getLocation(to).getResident().CO) )
        return -1;

      // if this unit can't traverse that terrain.
      int cost = findMoveCost(from, to, map);
      if( cost == MoveType.IMPASSABLE )
        return -1;

      return initialFillPower - cost;
    }

    public boolean canEnd(GameMap map, XYCoord end)
    {
      Unit obstacle = map.getLocation(end).getResident();
      return (obstacle == null || obstacle == unit);
    }

    public int findMoveCost(XYCoord from, XYCoord to, GameMap map)
    {
      return unit.model.propulsion.getMoveCost(map.getEnvironment(to.xCoord, to.yCoord));
    }
  } // ~BasicMoveFillFunctor

}
