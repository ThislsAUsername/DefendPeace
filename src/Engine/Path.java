package Engine;

import java.util.ArrayList;

/**
 * Path stores a list of waypoints with associated times to reach them. Once all 
 *   waypoints are stored, call start() to begin the movement, and  from the current 
 *   time, and getCurrentPosition() at  
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
		System.out.println("Added waypoint. Now at " + waypoints.size());
	}
	
	public void start()
	{
		System.out.println("Starting path with " + waypoints.size() + " waypoints");
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
	 * Puts the Path back in a pre-start state to allow it to run again.
	 */
	public void clear()
	{
		timeStarted = -1;
		lastWaypointPassed = -1;
		lastWaypointTime = 0;
		atEnd = false;
		waypoints.clear();
	}
	
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
				System.out.println("At End");
				pathX = waypoints.get(waypoints.size() - 1).x;
				pathY = waypoints.get(waypoints.size() - 1).y;
			}
			else
			{
				System.out.println("Calculating position");
				// We are still en route and not yet at the end. Find our actual location.
				PathNode lastPt = waypoints.get(lastWaypointPassed);
				PathNode nextPt = waypoints.get(lastWaypointPassed + 1);
				double xdiff = nextPt.x - lastPt.x;
				double ydiff = nextPt.y - lastPt.y;
				double progress = waypointTime / nextPt.timeMS;
				
				pathX = lastPt.x + (xdiff * progress);
				pathY = lastPt.y + (ydiff * progress);
				
				System.out.println("waypoint: " + nextPt);
				System.out.println("Progress: " + progress);
				System.out.println(" pathX  : " + pathX);
				System.out.println(" pathY  : " + pathY);
			}
		}

		return new XYCoord(pathX, pathY);
	}

	public int getPathLength()
	{
		return waypoints.size();
	}

	public XYCoord getEnd()
	{
		XYCoord ret;
		if(waypoints.size() > 0)
		{
			PathNode p = waypoints.get(waypoints.size() - 1);
			ret = new XYCoord(p.x, p.y);
		}
		else
		{
			ret = new XYCoord(-1, -1);
		}

		return ret;
	}

	private static class PathNode
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
}
