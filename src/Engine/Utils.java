package Engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;

import Terrain.GameMap;
import UI.MapView;
import Units.Unit;

public class Utils {
	
	/**
	 * Sets the highlight for myGame.gameMap.getLocation(x, y) to true if unit can act on Location (x, y) from (xLoc, yLoc), and false otherwise.
	 */
	public static void findActionableLocations(Unit unit, GameAction.ActionType action, int xLoc, int yLoc, GameMap map)
	{
		switch (action)
		{
		case ATTACK:
			// Set highlight for locations that contain an enemy that 'unit' can shoot from (xLoc, yLoc).
			for (int i = 0; i < map.mapWidth; i++)
			{
				for (int j = 0; j < map.mapHeight; j++)
				{
					Unit target = map.getLocation(i, j).getResident();
					if (target != null && target.CO != unit.CO)
					{
						if (unit.getDamage(target, xLoc, yLoc) > 0)
						{
							map.getLocation(i, j).setHighlight(true);
						}
						else
						{
							map.getLocation(i, j).setHighlight(false);
						}
					}
				}
			}
			break;
		case UNLOAD:
			// Set highlight for valid drop locations that can also support the passenger.
			Unit passenger = unit.heldUnits.get(0);
			for (int i = 0; i < map.mapWidth; i++)
			{
				for (int j = 0; j < map.mapHeight; j++)
				{
					int dist = Math.abs(yLoc-j) + Math.abs(xLoc-i);
					if (dist == 1 &&
						passenger.model.movePower >= passenger.model.propulsion.getMoveCost(map.getEnvironment(i, j)) &&
						map.isLocationEmpty(unit, i, j) )
					{
						map.getLocation(i, j).setHighlight(true);
					}
					else
					{
						map.getLocation(i, j).setHighlight(false);
					}
				}
			}
			break;
		}
	}
	
	/**
	 * Sets the highlight for myGame.gameMap.getLocation(x, y) to true if unit can reach (x, y), and false otherwise.
	 */
	public static void findPossibleDestinations(Unit unit, GameInstance myGame)
	{
		// set all locations to false/remaining move = 0
		int[][] movesLeftGrid = new int[myGame.gameMap.mapWidth][myGame.gameMap.mapHeight];
		for (int i = 0; i < myGame.gameMap.mapWidth; i++)
		{
			for (int j = 0; j < myGame.gameMap.mapHeight; j++)
			{
				myGame.gameMap.getLocation(i, j).setHighlight(false);
				movesLeftGrid[i][j] = 0;
			}
		}
		// set up our search
		SearchNode root = new SearchNode(unit.x, unit.y);
		movesLeftGrid[unit.x][unit.y] = Math.min(unit.model.movePower, unit.fuel);
		Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(movesLeftGrid));
		searchQueue.add(root);
		// do search
		while (!searchQueue.isEmpty())
		{
			// pull out the next search node
			SearchNode currentNode = searchQueue.poll();
			// if the space is empty or holds the current unit, highlight
			Unit obstacle = myGame.gameMap.getLocation(currentNode.x, currentNode.y).getResident();
			if (obstacle == null ||
				obstacle == unit ||
				(obstacle.CO == unit.CO && obstacle.hasCargoSpace(unit.model.type) ) )
			{
				myGame.gameMap.getLocation(currentNode.x, currentNode.y).setHighlight(true);
			}
			// right
			if (checkSpace(unit, myGame.gameMap, currentNode, currentNode.x+1, currentNode.y, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x+1, currentNode.y));
			}
			// left
			if (checkSpace(unit, myGame.gameMap, currentNode, currentNode.x-1, currentNode.y, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x-1, currentNode.y));
			}
			// down
			if (checkSpace(unit, myGame.gameMap, currentNode, currentNode.x, currentNode.y+1, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x, currentNode.y+1));
			}
			// up
			if (checkSpace(unit, myGame.gameMap, currentNode, currentNode.x, currentNode.y-1, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x, currentNode.y-1));
			}
			currentNode = null;
		}
	}
	
	/**
	 * Determines whether the Location (x, y), can be added to the search queue.
	 */
	private static boolean checkSpace(Unit unit, GameMap myMap, SearchNode currentNode, int x, int y, int[][] movesLeftGrid) {
		// if we're past the edges of the map
		if (x < 0 || y < 0 || x >= myMap.mapWidth || y >= myMap.mapHeight)
		{
			return false;
		}
		// if there is a unit in that space
		if (myMap.getLocation(x, y).getResident() != null)
		{	// if that unit is an enemy
			if(myMap.getLocation(x, y).getResident().CO != unit.CO)
			{
				return false;
			}
		}
		// if we have more movepower left than the other route there does
		boolean betterRoute = false;
		final int moveLeft = movesLeftGrid[currentNode.x][currentNode.y];
		int moveCost = findMoveCost(unit, x, y, myMap);
		if (moveLeft - moveCost >= movesLeftGrid[x][y])
		{
			betterRoute = true;
			movesLeftGrid[x][y] = moveLeft - moveCost;
		}
		return betterRoute;
	}
	
	private static int findMoveCost(Unit unit, int x, int y, GameMap map)
	{
		return unit.model.propulsion.getMoveCost(map.getEnvironment(x, y));
	}

	public static boolean isPathValid(Unit unit, Path path, GameMap map)
	{
		//System.out.println("Checking path validity. Length: " + (path.getPathLength()-1));
		boolean canReach = true;

		// Make sure waypoint 1 is under Unit.
		if(path.getPathLength() <= 0 || path.getWaypoint(0).x != unit.x || path.getWaypoint(0).y != unit.y)
		{
			canReach = false;
		}

		int movePower = Math.min(unit.model.movePower, unit.fuel);

		// Index from 1 so we don't count the space the unit is on.
		for(int i = 1; canReach && (i < path.getPathLength()); ++i)
		{
			//System.out.println("Moving over " + map.getEnvironment(path.getWaypoint(i).x, path.getWaypoint(i).y).terrainType);
			movePower -= findMoveCost(unit, path.getWaypoint(i).x, path.getWaypoint(i).y, map);
			if(movePower < 0)
			{
				canReach = false;
			}
		}

		return canReach;
	}

	/**
	 * Calculates and returns the shortest path for unit to take from its current location to map(x, y).
	 * If no valid path is found, null is returned.
	 */
	public static Path findShortestPath(Unit unit, int x, int y, GameMap map)
	{
		//System.out.println("Finding new path for " + unit.model.type + " from " + unit.x + ", " + unit.y + " to " + x + ", " + y);
		// Set all locations to false/remaining move = 0
		int[][] movesLeftGrid = new int[map.mapWidth][map.mapHeight];

		// Set up search parameters.
		SearchNode root = new SearchNode(unit.x, unit.y);
		movesLeftGrid[unit.x][unit.y] = Math.min(unit.model.movePower, unit.fuel);
		Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(movesLeftGrid, x, y));
		searchQueue.add(root);

		ArrayList<SearchNode> waypointList = new ArrayList<SearchNode>();

		// Find optimal route.
		while (!searchQueue.isEmpty())
		{
			// Retrieve the next search node.
			SearchNode currentNode = searchQueue.poll();

			// If this node is our destination, we are done.
			if(currentNode.x == x && currentNode.y == y)
			{
				// Add all of the points on the route to our waypoint list.
				while(currentNode.parent != null)
				{
					waypointList.add(currentNode);
					currentNode = currentNode.parent;
				}
				// Don't forget the starting node (no parent).
				waypointList.add(currentNode);
				break;
			}

			// right
			if (checkSpace(unit, map, currentNode, currentNode.x+1, currentNode.y, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x+1, currentNode.y, currentNode));
			}
			// left
			if (checkSpace(unit, map, currentNode, currentNode.x-1, currentNode.y, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x-1, currentNode.y, currentNode));
			}
			// down
			if (checkSpace(unit, map, currentNode, currentNode.x, currentNode.y+1, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x, currentNode.y+1, currentNode));
			}
			// up
			if (checkSpace(unit, map, currentNode, currentNode.x, currentNode.y-1, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x, currentNode.y-1, currentNode));
			}
			currentNode = null;
		}

		// Populate the Path object itself.
		Path sPath = new Path();
		// We added the waypoints to the list from end to beginning, so populate the Path in reverse order.
		if(!waypointList.isEmpty())
		{
			for(int j = waypointList.size() - 1; j >= 0; --j)
			{
				//System.out.println("Waypoint " + waypointList.get(j).x + ", " + waypointList.get(j).y + " over " + map.getEnvironment(waypointList.get(j).x, waypointList.get(j).y).terrainType);
				sPath.addWaypoint(waypointList.get(j).x, waypointList.get(j).y, MapView.getMapUnitMoveSpeed());
			}
		}

		return sPath;
	}

	/**
	 * Utility class used for pathfinding. Optionally holds a
	 *   reference to a parent node for path reconstruction.
	 */
	private static class SearchNode
	{
		public int x, y;
		public SearchNode parent;

		public SearchNode(int x, int y)
		{
			this(x, y, null);
		}

		public SearchNode(int x, int y, SearchNode parent)
		{
			this.x = x;
			this.y = y;
			this.parent = parent;
		}
	}

	/**
	 * Compares SearchNodes based on the amount of movePower they possess, and optionally
	 *   the remaining distance to a destination.
	 */
	private static class SearchNodeComparator implements Comparator<SearchNode>
	{
		int[][] movesLeftGrid;
		private final boolean hasDestination;
		private int xDest;
		private int yDest;
		
		public SearchNodeComparator(int[][] movesLeftGrid)
		{
			this.movesLeftGrid = movesLeftGrid;
			hasDestination = false;
			xDest = 0;
			yDest = 0;
		}

		public SearchNodeComparator(int[][] movesLeftGrid, int x, int y)
		{
			this.movesLeftGrid = movesLeftGrid;
			hasDestination = true;
			xDest = x;
			yDest = y;
		}

		@Override
		public int compare(SearchNode o1, SearchNode o2)
		{
			int firstDist = Math.abs(o1.x - xDest) + Math.abs(o1.y - yDest);
			int secondDist = Math.abs(o2.x - xDest) + Math.abs(o2.y - yDest);

			int firstPow = movesLeftGrid[o1.x][o1.y] + ((hasDestination)? firstDist : 0);
			int secondPow = movesLeftGrid[o2.x][o2.y] + ((hasDestination)? secondDist : 0);
			return firstPow - secondPow;
		}
	}
}
