package Engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;

import CommandingOfficers.Commander;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;
import Units.Weapons.Weapon;

public class Utils
{

  /** Returns a list of all locations between 1 and maxRange tiles away from origin, inclusive. */
  public static ArrayList<XYCoord> findLocationsInRange(GameMap map, XYCoord origin, int maxRange)
  {
    return findLocationsInRange(map, origin, 1, maxRange);
  }

  /** Returns a list of all locations between minRange and maxRange tiles away from origin, inclusive. */
  public static ArrayList<XYCoord> findLocationsInRange(GameMap map, XYCoord origin, int minRange, int maxRange)
  {
    ArrayList<XYCoord> locations = new ArrayList<XYCoord>();

    // Loop through all the valid x and y offsets, as dictated by the max range, and add valid spaces to our collection.
    for( int yOff = -maxRange; yOff <= maxRange; ++yOff )
    {
      for( int xOff = -maxRange; xOff <= maxRange; ++xOff )
      {
        int currentRange = Math.abs(xOff) + Math.abs(yOff);
        if( currentRange < minRange || currentRange > maxRange )
        {
          // This location is not in the desired range; move to the next.
          continue;
        }

        // Add this location to the set.
        locations.add(new XYCoord(origin.xCoord + xOff, origin.yCoord + yOff));
      }
    }

    return locations;
  }

  /** Returns a list of locations of all enemy units that weapon could hit from attackerPosition. */
  public static ArrayList<XYCoord> findTargetsInRange(GameMap map, Commander co, XYCoord attackerPosition, Weapon weapon)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, attackerPosition, weapon.model.minRange,
        weapon.model.maxRange);
    ArrayList<XYCoord> targets = new ArrayList<XYCoord>();
    for( XYCoord loc : locations )
    {
      if( map.getLocation(loc).getResident() != null && // Someone is there.
          map.getLocation(loc).getResident().CO != co && // They are not friendly.
          weapon.getDamage(map.getLocation(loc).getResident().model) > 0 ) // We can shoot them.
      {
        targets.add(loc);
      }
    }
    return targets;
  }

  /** Returns a list of locations at distance 1 from transportLoc that cargo can move on. */
  public static ArrayList<XYCoord> findUnloadLocations(GameMap map, XYCoord transportLoc, Unit cargo)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, transportLoc, 1);
    ArrayList<XYCoord> dropoffLocations = new ArrayList<XYCoord>();
    for( XYCoord loc : locations )
    {
      // Add any location that is empty and supports movement of the cargo unit.
      if( map.isLocationEmpty(loc)
          && cargo.model.movePower >= cargo.model.propulsion.getMoveCost(map.getEnvironment(loc.xCoord, loc.yCoord)) )
      {
        dropoffLocations.add(loc);
      }
    }
    return dropoffLocations;
  }

  /** Sets the highlight of all tiles in the provided list, and unsets the highlight on all others. */
  public static void highlightLocations(GameMap map, ArrayList<XYCoord> locations)
  {
    map.clearAllHighlights();
    for( XYCoord loc : locations )
    {
      map.getLocation(loc).setHighlight(true);
    }
  }

  /**
   * Sets the highlight for myGame.gameMap.getLocation(x, y) to true if unit can reach (x, y), and false otherwise.
   */
  public static void findPossibleDestinations(Unit unit, GameInstance myGame)
  {
    // set all locations to false/remaining move = 0
    int[][] movesLeftGrid = new int[myGame.gameMap.mapWidth][myGame.gameMap.mapHeight];
    for( int i = 0; i < myGame.gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < myGame.gameMap.mapHeight; j++ )
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
      if( obstacle == null || obstacle == unit || (obstacle.CO == unit.CO && obstacle.hasCargoSpace(unit.model.type)) )
      {
        myGame.gameMap.getLocation(currentNode.x, currentNode.y).setHighlight(true);
      }

      expandSearchNode(unit, myGame.gameMap, currentNode, searchQueue, movesLeftGrid);

      currentNode = null;
    }
  }

  /**
   * Determines whether the Location (x, y), can be added to the search queue.
   */
  private static boolean checkSpace(Unit unit, GameMap myMap, SearchNode currentNode, int x, int y, int[][] movesLeftGrid)
  {
    // if we're past the edges of the map
    if( x < 0 || y < 0 || x >= myMap.mapWidth || y >= myMap.mapHeight )
    {
      return false;
    }
    // if there is a unit in that space
    if( myMap.getLocation(x, y).getResident() != null )
    { // if that unit is an enemy
      if( myMap.getLocation(x, y).getResident().CO != unit.CO )
      {
        return false;
      }
    }
    // if we have more movepower left than the other route there does
    boolean betterRoute = false;
    final int moveLeft = movesLeftGrid[currentNode.x][currentNode.y];
    int moveCost = findMoveCost(unit, x, y, myMap);
    if( moveLeft - moveCost >= movesLeftGrid[x][y] )
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

    // Make sure the first waypoint is under the Unit.
    if( path.getPathLength() <= 0 || path.getWaypoint(0).x != unit.x || path.getWaypoint(0).y != unit.y )
    {
      canReach = false;
    }

    int movePower = Math.min(unit.model.movePower, unit.fuel);

    // Index from 1 so we don't count the space the unit is on.
    for( int i = 1; canReach && (i < path.getPathLength()); ++i )
    {
      int wayX = path.getWaypoint(i).x;
      int wayY = path.getWaypoint(i).y;
      Location loc = map.getLocation(wayX, wayY);
      Unit resident = loc.getResident();

      movePower -= findMoveCost(unit, wayX, wayY, map);
      if( movePower < 0 || (resident != null && resident.CO != unit.CO) )
      {
        canReach = false;
      }
    }

    return canReach;
  }

  /**
   * Calculate the shortest path for unit to take from its current location to map(x, y), and populate
   * the path parameter with those waypoints.
   * If no valid path is found, the path will be returned empty.
   */
  public static void findShortestPath(Unit unit, int x, int y, Path aPath, GameMap map)
  {
    if( map.mapWidth < unit.x || map.mapHeight < unit.y || unit.x < 0 || unit.y < 0 )
    {
      // Unit is not in a valid place. No path can be found.
      System.out.println("WARNING! Cannot find path for a unit that is not on the map.");
      aPath.clear();
      return;
    }

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
      if( currentNode.x == x && currentNode.y == y )
      {
        // Add all of the points on the route to our waypoint list.
        while (currentNode.parent != null)
        {
          waypointList.add(currentNode);
          currentNode = currentNode.parent;
        }
        // Don't forget the starting node (no parent).
        waypointList.add(currentNode);
        break;
      }

      expandSearchNode(unit, map, currentNode, searchQueue, movesLeftGrid);

      currentNode = null;
    }

    // Clear and Populate the Path object.
    aPath.clear();
    // We added the waypoints to the list from end to beginning, so populate the Path in reverse order.
    if( !waypointList.isEmpty() )
    {
      for( int j = waypointList.size() - 1; j >= 0; --j )
      {
        //System.out.println("Waypoint " + waypointList.get(j).x + ", " + waypointList.get(j).y + " over " + map.getEnvironment(waypointList.get(j).x, waypointList.get(j).y).terrainType);
        aPath.addWaypoint(waypointList.get(j).x, waypointList.get(j).y);
      }
    }
  }

  private static void expandSearchNode(Unit unit, GameMap map, SearchNode currentNode, Queue<SearchNode> searchQueue,
      int[][] movesLeftGrid)
  {
    // right
    if( checkSpace(unit, map, currentNode, currentNode.x + 1, currentNode.y, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x + 1, currentNode.y, currentNode));
    }
    // left
    if( checkSpace(unit, map, currentNode, currentNode.x - 1, currentNode.y, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x - 1, currentNode.y, currentNode));
    }
    // down
    if( checkSpace(unit, map, currentNode, currentNode.x, currentNode.y + 1, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x, currentNode.y + 1, currentNode));
    }
    // up
    if( checkSpace(unit, map, currentNode, currentNode.x, currentNode.y - 1, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x, currentNode.y - 1, currentNode));
    }
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

      int firstPow = movesLeftGrid[o1.x][o1.y] + ((hasDestination) ? firstDist : 0);
      int secondPow = movesLeftGrid[o2.x][o2.y] + ((hasDestination) ? secondDist : 0);
      return firstPow - secondPow;
    }
  }
}
