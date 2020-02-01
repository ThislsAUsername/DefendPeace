package Engine;

import java.util.ArrayList;

import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel;

/**
 * Path stores a list of waypoints with associated times to reach them. Once all 
 *   waypoints are stored, call start() to begin the movement. Subsequent calls
 *   to get getCurrentPosition() will return the calculated intermediate point.
 *
 *   NOTE: Path assumes that the first waypoint passed is the starting location.
 */
public class Path
{

  private ArrayList<PathNode> waypoints;

  private long timeStarted = -1;

  private int lastWaypointPassed = -1;
  private long lastWaypointTime = 0;

  private boolean atEnd = false;

  private double moveSpeedMsPerTile;

  // TODO: Consider the merits of removing movespeed as a parameter, and allowing the MapArtist to
  //   animate at whatever speed is appropriate. Or make speed node-specific again and have mapArtist
  //   set the speed for each PathNode (e.g. go slower through trees than on roads, etc).
  public Path(double moveSpeedMsPerTile)
  {
    waypoints = new ArrayList<PathNode>();
    this.moveSpeedMsPerTile = moveSpeedMsPerTile;
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
    lastWaypointPassed = -1;
    lastWaypointTime = 0;
    atEnd = false;
    timeStarted = -1;
  }

  public void start()
  {
    //System.out.println("Starting path with " + waypoints.size() + " waypoints");
    timeStarted = System.currentTimeMillis();
    lastWaypointPassed = 0;
    lastWaypointTime = timeStarted;
    atEnd = false;

    if( waypoints.size() == 1 )
    {
      // Only 1 waypoint in this path. By virtue of our assumption that we are at 
      // the first point when start() is called, we are now also at the end.
      atEnd = true;
    }

    if( waypoints.size() == 0 )
    {
      System.out.println("WARNING! Path.start() called when Path has no waypoints!");
      timeStarted = -1;
      lastWaypointPassed = -1;
      lastWaypointTime = -1;
    }
  }

  /**
   * If path has not been started:
   *   return the location of the first waypoint.
   * If path has been started:
   *   return the position along the route, as calculated from the time start() was called.
   * If path has been completed:
   *   return the location of the last waypoint.
   */
  public XYCoord getPosition()
  {
    double pathX = -1;
    double pathY = -1;

    if( atEnd ) // At end of path.
    {
      pathX = waypoints.get(waypoints.size() - 1).x;
      pathY = waypoints.get(waypoints.size() - 1).y;
    }
    else if( -1 == timeStarted ) // Not yet started Path.
    {
      if( waypoints.size() > 0 )
      {
        pathX = waypoints.get(0).x;
        pathY = waypoints.get(0).y;
      }
      else
      {
        System.out.println("WARNING! Path.getPathLocation called before any waypoints were added!");
      }
    }
    else
    // En route.
    {
      long currentTime = System.currentTimeMillis();
      long waypointTime = currentTime - lastWaypointTime;
      double nextWaypointTimeMs = moveSpeedMsPerTile;

      // Make sure nextWaypoint is correct, accounting for if we just passed a point.
      while (!atEnd && waypointTime > nextWaypointTimeMs)
      {
        lastWaypointPassed += 1;
        lastWaypointTime = currentTime;
        waypointTime -= nextWaypointTimeMs;

        // If the new waypoint is the last one in the Path, raise a flag.
        if( lastWaypointPassed == (waypoints.size() - 1) )
        {
          atEnd = true;
        }
      }

      // Now we know our nextWaypoint; figure out where we are along the way.
      if( atEnd )
      {
        pathX = waypoints.get(waypoints.size() - 1).x;
        pathY = waypoints.get(waypoints.size() - 1).y;
      }
      else
      {
        // We are still en route and not yet at the end. Find our actual location.
        PathNode lastPt = waypoints.get(lastWaypointPassed);
        PathNode nextPt = waypoints.get(lastWaypointPassed + 1);
        double xdiff = nextPt.x - lastPt.x;
        double ydiff = nextPt.y - lastPt.y;
        double progress = waypointTime / nextWaypointTimeMs;

        pathX = lastPt.x + (xdiff * progress);
        pathY = lastPt.y + (ydiff * progress);
      }
    }

    return new XYCoord((int) pathX, (int) pathY);
  }

  public int getPathLength()
  {
    return waypoints.size();
  }

  /**
   * @return the amount of fuel it would cost to travel this path with the given unit type 
  **/
  public int getFuelCost(UnitModel model, GameMap map)
  {
    int cost = 0;
    // We iterate from 1 because the first waypoint is the unit's initial position.
    for (int i = 1; i < waypoints.size(); i++)
    {
      PathNode loc = waypoints.get(i);
      cost += model.propulsion.getMoveCost(map.getEnvironment(loc.x, loc.y));
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
    for( int i = 0; i < waypoints.size(); i++)
    {
      PathNode point = waypoints.get(i);
      Unit obstacle = map.getLocation(point.x, point.y).getResident();
      if( null != obstacle && unit.CO.isEnemy(obstacle.CO) )
      {
        snip(i);
        break;
      }
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
