package Engine;

import java.util.ArrayList;

import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel;

/**
 * Path stores a list of waypoints.
 */
public class GamePath
{

  private ArrayList<PathNode> waypoints;

  public GamePath()
  {
    waypoints = new ArrayList<PathNode>();
  }

  public void addWaypoint(int x, int y)
  {
    waypoints.add(new PathNode(x, y));
  }

  public ArrayList<PathNode> getWaypoints()
  {
    return waypoints;
  }

  /**
   * Remove any stored waypoints, returning this object to its just-initialized state.
   */
  public void clear()
  {
    waypoints.clear();
  }

  public int getPathLength()
  {
    return waypoints.size();
  }

  /**
   * @return the amount of fuel it would cost to travel this path with the given unit type 
  **/
  public int getFuelCost(UnitModel unit, GameMap map)
  {
    int cost = 0;
    boolean includeOccupied = true, canTravelThroughEnemies = true;
    FloodFillFunctor fff = unit.propulsion.getUnitMoveFunctor(null, includeOccupied, canTravelThroughEnemies);
    // We iterate from 1 because the first waypoint is the unit's initial position.
    for (int i = 1; i < waypoints.size(); i++)
    {
      XYCoord from = waypoints.get(i-1).GetCoordinates();
      XYCoord to   = waypoints.get( i ).GetCoordinates();
      cost += fff.getTransitionCost(map, from, to);
    }
    return cost;
  }

  public PathNode getWaypoint(int wpt)
  {
    PathNode p = null;
    if( waypoints.size() > wpt )
    {
      p = waypoints.get(wpt);
    }
    else
    {
      System.out.println("WARNING! Attempting to get an invalid PathNode.");
    }
    return p;
  }

  public PathNode getEnd()
  {
    PathNode p = null;
    if( !waypoints.isEmpty() )
    {
      p = getWaypoint(waypoints.size() - 1);
    }

    return p;
  }

  public XYCoord getEndCoord()
  {
    PathNode p = getEnd();

    if( null == p )
      return null;

    return new XYCoord(p.x, p.y);
  }

  public static class PathNode
  {
    public int x;
    public int y;

    public PathNode(int x, int y)
    {
      this.x = x;
      this.y = y;
    }
    public XYCoord GetCoordinates()
    {
      return new XYCoord(x, y);
    }
    @Override
    public String toString()
    {
      return String.format("(%s, %s)", x, y);
    }
  }

  /**
   * The waypoint at the indicated index, and any that come after, are removed from the path.
   */
  public void snip(int newLastPoint)
  {
    while (newLastPoint < waypoints.size())
    {
      waypoints.remove(waypoints.size() - 1);
    }
  }

  public void snipCollision(GameMap map, Unit unit)
  {
    boolean includeOccupiedSpaces = false;
    FloodFillFunctor fff = unit.getMoveFunctor(includeOccupiedSpaces);

    // Snip if we can't actually traverse, iterate starting at the fist place we move
    for( int i = 1; i < waypoints.size(); ++i)
    {
      XYCoord from = waypoints.get(i-1).GetCoordinates();
      XYCoord to   = waypoints.get( i ).GetCoordinates();
      if( unit.getMovePower(map) < fff.getTransitionCost(map, from, to) )
      {
        snip(i);
        break;
      }
    }

    // Snip if we can't arrive, iterate backwards
    // Don't check zero since we don't want to invalidate the path
    for( int i = waypoints.size()-1; i > 0; --i )
    {
      XYCoord to = waypoints.get(i).GetCoordinates();
      if( fff.canEnd(map, to) )
        break;
      snip(i);
    }
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer("[");
    for( PathNode xyc : waypoints )
    {
      sb.append(xyc);
      if( xyc != getEnd() ) sb.append(", ");
    }
    sb.append("]");
    return sb.toString();
  }
}
