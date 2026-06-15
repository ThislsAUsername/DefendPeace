package Engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;

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
  public int maxTurns = 1;
  public final GameMap gameMap;
  public boolean includeOccupiedSpaces;
  public boolean canTravelThroughEnemies;
  public boolean findAllValidParents;

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
    findAllValidParents = false;
  }
  public PathCalcParams(MoveType pMT, int movePower, XYCoord pStart, GameMap gameMap)
  {
    this.gameMap = gameMap;
    start = pStart;
    moverIdentity = null;
    team = null;
    mt = pMT;
    initialMovePower = movePower;
    includeOccupiedSpaces = true;
    canTravelThroughEnemies = false;
    findAllValidParents = false;
  }
  /**
   * Tell this to ignore other units and move-power limitations.
   */
  public PathCalcParams setTheoretical()
  {
    maxTurns = Integer.MAX_VALUE >> 16;
    canTravelThroughEnemies = true;
    return this;
  }

  private int encodeMovePower(int power, int turns)
  {
    int result = 0xFFFF & power;
    result += (maxTurns - turns) << 16; // Fewer turns spent = more move power
    return result;
  }
  private static int decodeMovePower(int overallPower)
  {
    if( (0x8000 & overallPower) != 0 ) // The new sign bit matches
      return -1;
    return 0xFFFF & overallPower;
  }
  private int decodeTurns(int overallPower)
  {
    return -1 * ((overallPower >> 16) - maxTurns);
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
        powerGrid[i][j] = encodeMovePower(-1, maxTurns);
      }
    }

    // set up our search
    SearchNode root = new SearchNode(start.x, start.y, 1);
    if( findAllValidParents )
      root.allParents = new HashSet<>();
    powerGrid[start.x][start.y] = encodeMovePower(initialMovePower, 1);
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

      if( findAllValidParents )
        expandSearchNodeWithParents(currentNode, searchQueue, powerGrid, reachableTiles);
      else
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
        powerGrid[i][j] = encodeMovePower(-1, maxTurns);
      }
    }

    // Set up search parameters.
    SearchNode root = new SearchNode(start.x, start.y, 1);
    powerGrid[start.x][start.y] = encodeMovePower(initialMovePower, 1);
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
      if( decodeTurns(powerGrid[next.x][next.y]) < decodeTurns(powerGrid[currentNode.x][currentNode.y]) )
        continue; // Is from a previous turn

      final int transitionCost = mt.getTransitionCost(map, currentNode, next, team, canTravelThroughEnemies);
      if( transitionCost >= MoveType.IMPASSABLE || transitionCost >= initialMovePower )
        continue; // We cannot enter this tile even in principle.

      int oldPower     = decodeMovePower(powerGrid[currentNode.x][currentNode.y]);
      int oldNextPower = decodeMovePower(powerGrid[next.x][next.y]);
      int newNextPower = oldPower - transitionCost;
      int newTurns     = currentNode.turn;
      if( newNextPower < 0 && currentNode.turn < maxTurns )
      {
        newNextPower = initialMovePower - transitionCost;
        newTurns    += 1;
      }

      if( newNextPower > oldNextPower )
      {
        powerGrid[next.x][next.y] = encodeMovePower(newNextPower, newTurns);
        // Prevent wrong path generation due to updating the shared powerGrid
        searchQueue.removeIf(node -> next.equals(node));
        searchQueue.add(new SearchNode(next, newTurns, currentNode));
      }
    }
  }

  private void expandSearchNodeWithParents(SearchNode currentNode, Queue<SearchNode> searchQueue, int[][] powerGrid, ArrayList<SearchNode> reachableTiles)
  {
    GameMap map = gameMap;
    ArrayList<XYCoord> coordsToCheck = Utils.findLocationsInRange(map, currentNode, 1, 1);

    for( XYCoord next : coordsToCheck )
    {
      // If we can move more cheaply than previously discovered,
      // then update the power grid and re-queue the next node.
      if( decodeTurns(powerGrid[next.x][next.y]) < decodeTurns(powerGrid[currentNode.x][currentNode.y]) )
        continue; // I'm not entirely sure this makes sense, but I also dunno why you would care about tracking the possibility that you could move an extra turn away and then come back.

      final int transitionCost = mt.getTransitionCost(map, currentNode, next, team, canTravelThroughEnemies);
      if( transitionCost >= MoveType.IMPASSABLE || transitionCost >= initialMovePower )
        continue; // We cannot enter this tile even in principle.

      int oldPower     = decodeMovePower(powerGrid[currentNode.x][currentNode.y]);
      int oldNextPower = decodeMovePower(powerGrid[next.x][next.y]);
      int newNextPower = oldPower - transitionCost;
      int newTurns     = currentNode.turn;
      if( newNextPower < 0 && currentNode.turn < maxTurns )
      {
        newNextPower = initialMovePower - transitionCost;
        newTurns    += 1;
      }

      if( newNextPower >= 0 )
      {
        Optional<SearchNode> oldNextOpt = reachableTiles.stream().filter(node -> next.equals(node)).findFirst();
        // We've already found the best path here, so just add the new parent
        if( oldNextOpt.isPresent() )
        {
          SearchNode oldNext = oldNextOpt.get();
          oldNext.allParents.add(currentNode);
          continue;
        }
        // We either haven't seen this node before, or it's in the queue
        oldNextOpt = searchQueue.stream().filter(node -> next.equals(node)).findFirst();
        if( newNextPower > oldNextPower )
        {
          powerGrid[next.x][next.y] = encodeMovePower(newNextPower, newTurns);

          SearchNode snNext = new SearchNode(next, newTurns, currentNode);
          snNext.allParents = new HashSet<>();
          snNext.allParents.add(currentNode);
          // Prevent wrong path generation due to updating the shared powerGrid
          searchQueue.removeIf(node -> next.equals(node));
          searchQueue.add(snNext);

          if( !oldNextOpt.isPresent() )
            continue;
          SearchNode oldNext = oldNextOpt.get();
          snNext.allParents.addAll(oldNext.allParents);
        }
        else if( oldNextOpt.isPresent() )
        {
          SearchNode oldNext = oldNextOpt.get();
          oldNext.allParents.add(currentNode);
        }
        else
          System.out.println("expandSearchNodeWithParents: Somehow, "+next+" is not a new node, a destination, or in the queue. Ehh?");
      }
    }
  }

  /**
   * Utility class used for pathfinding. Optionally holds a
   *   reference to a parent node for path reconstruction.<p>
   * Caveat emptor: the SearchNode quacks like an XYCoord for equality checks
   */
  public static class SearchNode extends XYCoord
  {
    private static final long serialVersionUID = 2637721435469761667L;
    public SearchNode parent;
    public HashSet<SearchNode> allParents;
    public int turn;

    public SearchNode(int x, int y, int pTurn)
    {
      this(x, y, pTurn, null);
    }

    public SearchNode(XYCoord coord, int pTurn, SearchNode parent)
    {
      this(coord.x, coord.y, pTurn, parent);
    }
    public SearchNode(int x, int y, int pTurn, SearchNode parent)
    {
      super(x, y);
      turn = pTurn;
      this.parent = parent;
    }
    public XYCoord getCoordinates()
    {
      return this;
    }
    public GamePath getMyPath()
    {
      GamePath aPath = new GamePath();

      SearchNode currentNode = this;
      // Add all of the points on the route to our waypoint list.
      while (currentNode != null)
      {
        // Since we're iterating dest->start, each point is the new "first" point.
        aPath.addWaypoint(0, currentNode.x, currentNode.y);
        currentNode = currentNode.parent;
      }

      return aPath;
    }
  }

  /**
   * Compares SearchNodes based on the amount of movePower they possess, and optionally
   *   the remaining distance to a destination.
   */
  private static class SearchNodeComparator implements Comparator<SearchNode>
  {
    int[][] powerGrid;
    private final boolean hasDestination;
    private int xDest;
    private int yDest;

    public SearchNodeComparator(int[][] powerGrid)
    {
      this.powerGrid = powerGrid;
      hasDestination = false;
      xDest = 0;
      yDest = 0;
    }

    public SearchNodeComparator(int[][] powerGrid, int x, int y)
    {
      this.powerGrid = powerGrid;
      hasDestination = true;
      xDest = x;
      yDest = y;
    }

    @Override
    public int compare(SearchNode o1, SearchNode o2)
    {
      int firstDist = Math.abs(o1.x - xDest) + Math.abs(o1.y - yDest);
      int secondDist = Math.abs(o2.x - xDest) + Math.abs(o2.y - yDest);

      // Note: These values are not decoded because the encoded values will be in the right order for non-negative movePowers
      int firstPowerEstimate  = powerGrid[o1.x][o1.y] - ((hasDestination) ? firstDist : 0);
      int secondPowerEstimate = powerGrid[o2.x][o2.y] - ((hasDestination) ? secondDist : 0);
      return secondPowerEstimate - firstPowerEstimate;
    }
  }
}