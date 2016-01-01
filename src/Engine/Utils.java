package Engine;

import java.util.Comparator;
import java.util.Queue;

import Terrain.GameMap;
import Units.Unit;

public class Utils {
	
	/**
	 * Sets the highlight for myGame.gameMap.getLocation(x, y) to true if unit can act on Location (x, y), and false otherwise.
	 */
	public static void findActionableLocations(Unit unit, MapController.GameAction action, GameMap map)
	{
		switch (action)
		{
		case ATTACK:
			// Set highlight for locations within weapon range, regardless of whether an enemy is present.
			for (int i = 0; i < map.mapWidth; i++)
			{
				for (int j = 0; j < map.mapHeight; j++)
				{
					Unit target = map.getLocation(i, j).getResident();
					if (target != null && target.CO != unit.CO)
					{
						if (unit.getDamage(target) > 0)
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
					int dist = Math.abs(unit.y-j) + Math.abs(unit.x-i);
					if (dist == 1 &&
						passenger.model.movePower >= passenger.model.propulsion.getMoveCost(map.getEnvironment(i, j)) &&
						map.getLocation(i, j).getResident() == null)
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
			if (checkSpace(unit, myGame, currentNode, currentNode.x+1, currentNode.y, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x+1, currentNode.y));
			}
			// left
			if (checkSpace(unit, myGame, currentNode, currentNode.x-1, currentNode.y, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x-1, currentNode.y));
			}
			// down
			if (checkSpace(unit, myGame, currentNode, currentNode.x, currentNode.y+1, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x, currentNode.y+1));
			}
			// up
			if (checkSpace(unit, myGame, currentNode, currentNode.x, currentNode.y-1, movesLeftGrid))
			{
				searchQueue.add(new SearchNode(currentNode.x, currentNode.y-1));
			}
			currentNode = null;
		}
	}
	
	private static boolean checkSpace(Unit unit, GameInstance myGame, SearchNode currentNode, int x, int y, int[][] movesLeftGrid) {
		// if we're past the edges of the map
		if (x < 0 || y < 0 || x >= myGame.gameMap.mapWidth || y >= myGame.gameMap.mapHeight)
		{
			return false;
		}
		// if there is a unit in that space
		if (myGame.gameMap.getLocation(x, y).getResident() != null)
		{	// if that unit is an enemy
			if(myGame.gameMap.getLocation(x, y).getResident().CO != unit.CO)
			{
				return false;
			}
		}
		// if we have more movepower left than the other route there does
		boolean betterRoute = false;
		final int moveLeft = movesLeftGrid[currentNode.x][currentNode.y];
		int moveCost = unit.model.propulsion.getMoveCost(myGame.gameMap.getEnvironment(x, y).weatherType, myGame.gameMap.getEnvironment(x, y).terrainType);
		if (moveLeft - moveCost >= movesLeftGrid[x][y])
		{
			betterRoute = true;
			movesLeftGrid[x][y] = moveLeft - moveCost;
		}
		return betterRoute;
	}
	
	private static class SearchNode
	{
		public int x, y;
		public SearchNode(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}
	
	private static class SearchNodeComparator implements Comparator<SearchNode>
	{
		int[][] movesLeftGrid;
		
		public SearchNodeComparator(int[][] movesLeftGrid)
		{
			this.movesLeftGrid = movesLeftGrid;
		}
		
		@Override
		public int compare(SearchNode o1, SearchNode o2)
		{
			int firstPow = movesLeftGrid[o1.x][o1.y];
			int secondPow = movesLeftGrid[o2.x][o2.y];
			return firstPow - secondPow;
		}
		
	}
}
