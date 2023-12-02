package Engine;

import java.util.ArrayList;
import java.util.Queue;

import Engine.Utils.SearchNode;
import Engine.Utils.SearchNodeComparator;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitContext;
import Units.MoveTypes.MoveType;

public class PathCalcParams
{
  public XYCoord start; // Initial location; will usually be in the output set.
  public Unit moverIdentity; // May be null
  public Army team; // The affiliation of the unit moving; may be null to assume everyone's an enemy
  public MoveType mt;
  public int initialMovePower;
  public final GameMap gameMap;
  public boolean includeOccupiedSpaces;
  public boolean canTravelThroughEnemies;

  public PathCalcParams(Unit unit, GameMap gameMap)
  {
    this(new UnitContext(gameMap, unit), gameMap);
  }
  public PathCalcParams(UnitContext uc, GameMap gameMap)
  {
    this.gameMap = gameMap;
    start = uc.coord;
    moverIdentity = uc.unit;
    team = uc.CO.army;
    mt = uc.calculateMoveType();
    initialMovePower = Math.min(uc.calculateMovePower(), uc.fuel);
    includeOccupiedSpaces = true;
    canTravelThroughEnemies = false;
  }
  /**
   * Tell this to ignore other units and move-power limitations.
   */
  public PathCalcParams setTheoretical()
  {
    initialMovePower = Integer.MAX_VALUE;
    canTravelThroughEnemies = true;
    return this;
  }

  public ArrayList<SearchNode> findAllPaths()
  {
    ArrayList<SearchNode> reachableTiles = new ArrayList<>();

    if( null == mt || null == start || start.x < 0 || start.y < 0 )
    {
      System.out.println("WARNING! Finding destinations for ineligible unit!");
      return reachableTiles;
    }

    // set all locations to unreachable
    int[][] powerGrid = new int[gameMap.mapWidth][gameMap.mapHeight];
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        powerGrid[i][j] = -1;
      }
    }

    // set up our search
    SearchNode root = new SearchNode(start.x, start.y);
    powerGrid[start.x][start.y] = initialMovePower;
    Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(powerGrid));
    searchQueue.add(root);
    // do search
    while (!searchQueue.isEmpty())
    {
      // pull out the next search node
      SearchNode currentNode = searchQueue.poll();
      if( mt.canStandOn(gameMap, currentNode, moverIdentity, includeOccupiedSpaces) )
      {
        reachableTiles.add(currentNode);
      }

      expandSearchNode(currentNode, searchQueue, powerGrid);

      currentNode = null;
    }

    return reachableTiles;
  }

  public GamePath findShortestPath(XYCoord dest)
  {
    return findShortestPath(dest.x, dest.y);
  }
  /**
   * Calculate and return the minimum-cost path to map(x, y).<p>
   * If no valid path is found, returns null.
   */
  public GamePath findShortestPath(int x, int y)
  {
    if( null == start || null == mt || null == gameMap || !gameMap.isLocationValid(start.x, start.y) )
    {
      return null;
    }

    if( !gameMap.isLocationValid(x, y) )
    {
      System.out.println("WARNING! Cannot find path to a place that is not on the map.");
      return null;
    }

    int[][] powerGrid = new int[gameMap.mapWidth][gameMap.mapHeight];
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        powerGrid[i][j] = -1;
      }
    }

    // Set up search parameters.
    SearchNode root = new SearchNode(start.x, start.y);
    powerGrid[start.x][start.y] = initialMovePower;
    Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(powerGrid, x, y));
    searchQueue.add(root);

    SearchNode currentNode = null;

    // Find optimal route.
    while (!searchQueue.isEmpty())
    {
      // Retrieve the next search node.
      currentNode = searchQueue.poll();

      // If this node is our destination, we are done.
      if( currentNode.x == x && currentNode.y == y )
        break; // findShortestPath() is given a particular endpoint already, so it assumes that the mover can stand there

      expandSearchNode(currentNode, searchQueue, powerGrid);

      currentNode = null;
    }

    if( null == currentNode )
      return null;
    else
      return currentNode.getMyPath();
  }

  /**
   * Look at the nodes adjacent to currentNode; if there are any we can reach that we haven't found yet, or that we
   * can reach more economically than previously discovered, update the cost grid and enqueue the node.
   */
  private void expandSearchNode(SearchNode currentNode, Queue<SearchNode> searchQueue, int[][] powerGrid)
  {
    GameMap map = gameMap;
    ArrayList<XYCoord> coordsToCheck = Utils.findLocationsInRange(map, currentNode, 1, 1);

    for( XYCoord next : coordsToCheck )
    {
      // If we can move more cheaply than previously discovered,
      // then update the power grid and re-queue the next node.
      int oldPower = powerGrid[currentNode.x][currentNode.y];
      int oldNextPower = powerGrid[next.x][next.y];
      final int transitionCost = mt.getTransitionCost(map, currentNode, next, team, canTravelThroughEnemies);
      int newNextPower = oldPower - transitionCost;

      if( transitionCost < MoveType.IMPASSABLE && newNextPower > oldNextPower )
      {
        powerGrid[next.x][next.y] = newNextPower;
        // Prevent wrong path generation due to updating the shared powerGrid
        searchQueue.removeIf(node -> next.equals(node));
        searchQueue.add(new SearchNode(next, currentNode));
      }
    }
  }
}