package Engine;

import java.util.ArrayList;

/**
 * Path stores a list of waypoints with associated times to reach them. Once all 
 *   waypoints are stored, call start() to begin the movement. Subsequent calls
 *   to get getCurrentPosition() will return the calculated intermediate point.
 *
 *   NOTE: Path assumes that the first waypoint passed is the starting location.
 */
public class Path {

	private ArrayList<PathNode> waypoints;
	
	private long timeStarted = -1;
	
	private int lastWaypointPassed = -1;
	private long lastWaypointTime = 0;
	
	private boolean atEnd = false;
	
	public Path()
	{
		waypoints = new ArrayList<PathNode>();
	}
	
	public void addWaypoint(int x, int y, double timeMS)
	{
		waypoints.add(new PathNode(x, y, timeMS));
	}

	public ArrayList<PathNode> getWaypoints()
	{
		return waypoints;
	}
	
	public void start()
	{
		//System.out.println("Starting path with " + waypoints.size() + " waypoints");
		timeStarted = System.currentTimeMillis();
		lastWaypointPassed = 0;
		lastWaypointTime = timeStarted;
		atEnd = false;
		
		if(waypoints.size() == 1)
		{
			// Only 1 waypoint in this path. By virtue of our assumption that we are at 
			// the first point when start() is called, we are now also at the end.
			atEnd = true;
		}
		
		if(waypoints.size() == 0)
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
		
		if(atEnd) // At end of path.
		{
			pathX = waypoints.get(waypoints.size() - 1).x;
			pathY = waypoints.get(waypoints.size() - 1).y;
		}
		else if(-1 == timeStarted) // Not yet started Path.
		{
			if(waypoints.size() > 0)
			{
				pathX = waypoints.get(0).x;
				pathY = waypoints.get(0).y;
			}
			else
			{
				System.out.println("WARNING! Path.getPathLocation called before any waypoints were added!");
			}
		}
		else // En route.
		{
			long currentTime = System.currentTimeMillis();
			long waypointTime = currentTime - lastWaypointTime;	
			PathNode nextWaypoint = waypoints.get(lastWaypointPassed+1);
			
			// Make sure nextWaypoint is correct, accounting for if we just passed a point.
			while(!atEnd && waypointTime > nextWaypoint.timeMS)
			{
				lastWaypointPassed += 1;
				lastWaypointTime = currentTime;
				waypointTime -= nextWaypoint.timeMS;
				
				// If the new waypoint is the last one in the Path, raise a flag.
				if(lastWaypointPassed == (waypoints.size() - 1))
				{
					atEnd = true;
				}
				else // Otherwise, just update nextWaypoint.
				{
					nextWaypoint = waypoints.get(lastWaypointPassed+1);
				}
			}
			
			// Now we know our nextWaypoint; figure out where we are along the way.
			if(atEnd)
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
				double progress = waypointTime / nextPt.timeMS;
				
				pathX = lastPt.x + (xdiff * progress);
				pathY = lastPt.y + (ydiff * progress);
			}
		}

		return new XYCoord(pathX, pathY);
	}

	public int getPathLength()
	{
		return waypoints.size();
	}

	public PathNode getWaypoint(int wpt)
	{
		PathNode p = null;
		if(waypoints.size() > wpt)
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

	public static class PathNode
	{
		public int x;
		public int y;
		public double timeMS;
		
		public PathNode(int x, int y, double timeMS)
		{
			this.x = x;
			this.y = y;
			this.timeMS = timeMS;
		}
	}

	/**
	 * The waypoint at the indicated index, and any that come after, are removed from the path.
	 */
	public void snip(int newLastPoint)
	{
		while(newLastPoint < waypoints.size())
		{
			waypoints.remove(waypoints.size()-1);
		}
	}
}
