package Engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Set;

import CommandingOfficers.Commander;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.UnitDieEvent;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;
import Units.MoveTypes.MoveType;

public class Utils
{

  /** Returns a list of all locations between 1 and maxRange tiles away from origin, inclusive. */
  public static ArrayList<XYCoord> findLocationsInRange(GameMap map, XYCoord origin, int maxRange)
  {
    return findLocationsInRange(map, origin, 1, maxRange);
  }

  public static ArrayList<XYCoord> findLocationsInRange(GameMap map, XYCoord origin, UnitContext uc)
  {
    return findLocationsInRange(map, origin, uc.rangeMin, uc.rangeMax);
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
        XYCoord coord = new XYCoord(origin.xCoord + xOff, origin.yCoord + yOff);
        if( currentRange >= minRange && currentRange <= maxRange && map.isLocationValid(coord) )
        {
          // Add this location to the set.
          locations.add(coord);
        }
      }
    }

    return locations;
  }

  /** Returns a list of locations of all valid targets that weapon could hit from attackerPosition. */
  public static ArrayList<XYCoord> findTargetsInRange(GameMap map, UnitContext attacker)
  {
    return findTargetsInRange(map, attacker, true);
  }

  /** Returns a list of locations of all valid targets that weapon could hit from attackerPosition. */
  public static ArrayList<XYCoord> findTargetsInRange(GameMap map, UnitContext attacker, boolean includeTerrain)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, attacker.coord, attacker.rangeMin, attacker.rangeMax);
    ArrayList<XYCoord> targets = new ArrayList<XYCoord>();
    for( XYCoord loc : locations )
    {
      Unit resident = map.getLocation(loc).getResident();
      if( resident != null && // Peeps are there.
          resident.CO.isEnemy(attacker.CO) && // They are not friendly.
          attacker.weapon.getDamage(resident.model) > 0 ) // We can shoot them.
      {
        targets.add(loc);
      }
      // You can never be friends with terrain, so shoot anything that's shootable
      else if (includeTerrain && resident == null && // Peeps ain't there.
          attacker.weapon.getDamage(map.getEnvironment(loc).terrainType) > 0)
        targets.add(loc);
    }
    return targets;
  }

  /** Returns a list of locations at distance 1 from transportLoc that cargo can move on. */
  public static ArrayList<XYCoord> findUnloadLocations(GameMap map, Unit transport, XYCoord moveLoc, Unit cargo)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, moveLoc, 1);
    ArrayList<XYCoord> dropoffLocations = new ArrayList<XYCoord>();
    final MoveType cargoMoveType = new UnitContext(cargo).calculateMoveType();
    if( cargoMoveType.canTraverse(map.getEnvironment(moveLoc)) )
      for( XYCoord loc : locations )
      {
        // Add any location that is empty and supports movement of the cargo unit.
        if( (map.isLocationEmpty(loc) || map.getLocation(loc).getResident() == transport)
            && MoveType.IMPASSABLE > cargoMoveType.getMoveCost(map.getEnvironment(loc.xCoord, loc.yCoord)) )
        {
          dropoffLocations.add(loc);
        }
      }
    return dropoffLocations;
  }

  /** Alias for {@link #findPossibleDestinations(XYCoord, Unit, GameMap) findPossibleDestinations()} **/
  public static ArrayList<XYCoord> findPossibleDestinations(Unit unit, GameMap gameMap, boolean includeOccupiedSpaces)
  {
    return findPossibleDestinations(new XYCoord(unit.x, unit.y), unit, gameMap, includeOccupiedSpaces);
  }
  /**
   * Finds the area reachable this turn by input unit.
   * Alias for {@link #findFloodFillArea(XYCoord, FloodFillFunctor, int, GameMap) findFloodFillArea()}
   */
  public static ArrayList<XYCoord> findPossibleDestinations(XYCoord start, Unit unit, GameMap gameMap, boolean includeOccupiedSpaces)
  {
    return findFloodFillArea(start, unit.getMoveFunctor(includeOccupiedSpaces), Math.min(unit.getMovePower(gameMap), unit.fuel), gameMap);
  }
  /**
   * @param start Initial location; will usually be in the output set.
   * @param fff Determines the cost to fill from one tile to another.
   * @param initialFillPower How much juice to give the FloodFillFunctor
   * @param gameMap The map to search over.
   * @return The list of XYCoords in gameMap reachable by the FloodFillFunctor from the start coord, given initialFillPower.
   */
  public static ArrayList<XYCoord> findFloodFillArea(XYCoord start, FloodFillFunctor fff, int initialFillPower, GameMap gameMap)
  {
    ArrayList<XYCoord> reachableTiles = new ArrayList<XYCoord>();

    if( null == fff || null == start || start.xCoord < 0 || start.yCoord < 0 )
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
    SearchNode root = new SearchNode(start.xCoord, start.yCoord);
    powerGrid[start.xCoord][start.yCoord] = initialFillPower;
    Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(powerGrid));
    searchQueue.add(root);
    // do search
    while (!searchQueue.isEmpty())
    {
      // pull out the next search node
      SearchNode currentNode = searchQueue.poll();
      XYCoord coord = new XYCoord(currentNode.x, currentNode.y);
      if( fff.canEnd(gameMap, coord) )
      {
        reachableTiles.add(coord);
      }

      expandSearchNode(fff, gameMap, currentNode, searchQueue, powerGrid);

      currentNode = null;
    }

    return reachableTiles;
  }

  public static boolean isPathValid(Unit unit, GamePath path, GameMap map, boolean includeOccupiedSpaces)
  {
    return isPathValid(new XYCoord(unit.x, unit.y), unit, path, map, includeOccupiedSpaces);
  }
  public static boolean isPathValid(XYCoord start, Unit unit, GamePath path, GameMap map, boolean includeOccupiedSpaces)
  {
    return isPathValid(start, unit.getMoveFunctor(includeOccupiedSpaces), Math.min(unit.getMovePower(map), unit.fuel), path, map);
  }
  public static boolean isPathValid(XYCoord start, FloodFillFunctor fff, int initialFillPower, GamePath path, GameMap map)
  {
    if( (null == path) || (null == fff) || (null == start) )
    {
      return false;
    }

    // Make sure the first waypoint is under the Unit.
    if( path.getPathLength() <= 0 || path.getWaypoint(0).x != start.xCoord || path.getWaypoint(0).y != start.yCoord )
    {
      return false;
    }

    int movePower = initialFillPower;
    XYCoord lastCoord = path.getWaypoint(0).GetCoordinates();

    // Index from 1 so we don't count the space the unit is on.
    for( int i = 1; i < path.getPathLength(); ++i )
    {
      XYCoord newCoord = path.getWaypoint(i).GetCoordinates();

      movePower -= fff.getTransitionCost(map, lastCoord, newCoord);
      lastCoord = newCoord;
      if( movePower < 0 )
      {
        return false;
      }
    }

    return fff.canEnd(map, lastCoord);
  }

  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static GamePath findShortestPath(Unit unit, XYCoord destination, GameMap map, boolean theoretical)
  {
    return findShortestPath(unit, destination.xCoord, destination.yCoord, map, theoretical);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static GamePath findShortestPath(Unit unit, int x, int y, GameMap map, boolean theoretical)
  {
    return findShortestPath(new XYCoord(unit.x, unit.y), unit, x, y, map, theoretical);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static GamePath findShortestPath(Unit unit, XYCoord destination, GameMap map)
  {
    return findShortestPath(unit, destination.xCoord, destination.yCoord, map, false);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static GamePath findShortestPath(Unit unit, int x, int y, GameMap map)
  {
    return findShortestPath(unit, x, y, map, false);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static GamePath findShortestPath(XYCoord start, Unit unit, int x, int y, GameMap map)
  {
    return findShortestPath(start, unit, x, y, map, false);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static GamePath findShortestPath(XYCoord start, Unit unit, XYCoord destination, GameMap map)
  {
    return findShortestPath(start, unit, destination.xCoord, destination.yCoord, map, false);
  }
  /**
   * Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)}
   * @param theoretical If true, ignores other Units and move-power limitations.
   */
  public static GamePath findShortestPath(XYCoord start, Unit unit, int x, int y, GameMap map, boolean theoretical)
  {
    // findShortestPath() is given a particular endpoint already, so it assumes that it is valid to end up there.
    return findShortestPath(start, unit.getMoveFunctor(true, theoretical),
                            (theoretical)? Integer.MAX_VALUE : Math.min(unit.getMovePower(map), unit.fuel),
                            x, y, map);
  }
  /**
   * Calculate and return the minimum-cost path for the FloodFillFunctor from its current location to map(x, y).
   * The Path will avoid non-allied units unless `theoretical` is true.
   * If no valid path is found, an empty Path will be returned.
   * @param start Initial location; will usually be in the output set.
   * @param fff Determines the cost to fill from one tile to another.
   * @param initialFillPower How much juice to give the FloodFillFunctor
   * @param x Final X coordinate
   * @param y Final Y coordinate
   * @param map The map to search over.
   */
  public static GamePath findShortestPath(XYCoord start, FloodFillFunctor fff, int initialFillPower, int x, int y, GameMap map)
  {
    if( null == start || null == fff || null == map || !map.isLocationValid(start.xCoord, start.yCoord) )
    {
      return null;
    }

    GamePath aPath = new GamePath();
    if( !map.isLocationValid(x, y) )
    {
      // Unit is not in a valid place. No path can be found.
      System.out.println("WARNING! Cannot find path for a unit that is not on the map.");
      aPath.clear();
      return aPath;
    }

    int[][] powerGrid = new int[map.mapWidth][map.mapHeight];
    for( int i = 0; i < map.mapWidth; i++ )
    {
      for( int j = 0; j < map.mapHeight; j++ )
      {
        powerGrid[i][j] = -1;
      }
    }

    // Set up search parameters.
    SearchNode root = new SearchNode(start.xCoord, start.yCoord);
    powerGrid[start.xCoord][start.yCoord] = initialFillPower;
    Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(powerGrid, x, y));
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

      expandSearchNode(fff, map, currentNode, searchQueue, powerGrid);

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

    return aPath;
  }

  /**
   * Look at the nodes adjacent to currentNode; if there are any we can reach that we haven't found yet, or that we
   * can reach more economically than previously discovered, update the cost grid and enqueue the node.
   * @param theoretical If set, don't limit range using move power, and don't worry about other Units in the way.
   */
  private static void expandSearchNode(FloodFillFunctor fff, GameMap map, SearchNode currentNode, Queue<SearchNode> searchQueue,
      int[][] powerGrid)
  {
    ArrayList<XYCoord> coordsToCheck = findLocationsInRange(map, currentNode.getCoordinates(), 1, 1);

    for( XYCoord next : coordsToCheck )
    {
      // If we can move more cheaply than previously discovered,
      // then update the power grid and re-queue the next node.
      int oldPower = powerGrid[currentNode.x][currentNode.y];
      int oldNextPower = powerGrid[next.xCoord][next.yCoord];
      int newNextPower = oldPower - fff.getTransitionCost(map, currentNode.getCoordinates(), next);

      if( newNextPower > oldNextPower )
      {
        powerGrid[next.xCoord][next.yCoord] = newNextPower;
        // Prevent wrong path generation due to updating the shared powerGrid
        searchQueue.removeIf(node->next.equals(node.getCoordinates()));
        searchQueue.add(new SearchNode(next, currentNode));
      }
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

    public SearchNode(XYCoord coord, SearchNode parent)
    {
      this(coord.xCoord, coord.yCoord, parent);
    }
    public SearchNode(int x, int y, SearchNode parent)
    {
      this.x = x;
      this.y = y;
      this.parent = parent;
    }
    public XYCoord getCoordinates()
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

      int firstPowerEstimate = powerGrid[o1.x][o1.y] - ((hasDestination) ? firstDist : 0);
      int secondPowerEstimate = powerGrid[o2.x][o2.y] - ((hasDestination) ? secondDist : 0);
      return secondPowerEstimate - firstPowerEstimate;
    }
  }

  /**
   * Returns a list of all vacant industries a army owns
   */
  public static ArrayList<XYCoord> findUsableProperties(Army army, GameMap map)
  {
    ArrayList<XYCoord> industries = new ArrayList<XYCoord>();
    // We don't want to bother if we're trying to find nobody's properties
    if( null != army )
    {
      // Add all vacant, <co>-owned industries to the list
      for( XYCoord xyc : army.getOwnedProperties() )
      {
        MapLocation loc = map.getLocation(xyc);
        if( army.canBuyOn(loc) )
        {
          industries.add(loc.getCoordinates());
        }
      }
    }
    return industries;
  }

  /**
   * Compare XYCoords based on how far they are from myCenter.
   * XYCoords closer to myCenter will be "less than" XYCoords that are farther away.
   */
  public static class ManhattanDistanceComparator implements Comparator<XYCoord>
  {
    XYCoord myCenter;

    public ManhattanDistanceComparator(XYCoord center)
    {
      myCenter = center;
    }

    @Override
    public int compare(XYCoord xy1, XYCoord xy2)
    {
      int xy1Dist = xy1.getDistance(myCenter);
      int xy2Dist = xy2.getDistance(myCenter);

      return xy1Dist - xy2Dist;
    }
  }

  /**
   * Sorts the mapLocations array by distance from the 'center' coordinate.
   * @param center
   * @param mapLocations
   */
  public static void sortLocationsByDistance(XYCoord center, ArrayList<XYCoord> mapLocations)
  {
    ManhattanDistanceComparator mdc = new ManhattanDistanceComparator(center);
    Collections.sort(mapLocations, mdc);
  }

  /**
   * Compare XYCoords based on how much time it would take myUnit to reach them on the given map.
   * XYCoords closer to myCenter will be "less than" XYCoords that are farther away.
   */
  public static class TravelDistanceComparator implements Comparator<XYCoord>
  {
    Unit myUnit;
    GameMap myMap;

    public TravelDistanceComparator(Unit unit, GameMap map)
    {
      myUnit = unit;
      myMap = map;
    }

    @Override
    public int compare(XYCoord xy1, XYCoord xy2)
    {
      int xy1Dist = findShortestPath(myUnit, xy1, myMap).getPathLength();
      int xy2Dist = findShortestPath(myUnit, xy2, myMap).getPathLength();

      return xy1Dist - xy2Dist;
    }
  }

  /**
   * Sorts the mapLocations array by distance from the 'center' coordinate.
   * @param center
   * @param mapLocations
   */
  public static void sortLocationsByTravelTime(Unit unit, ArrayList<XYCoord> mapLocations, GameMap map)
  {
    TravelDistanceComparator tdc = new TravelDistanceComparator(unit, map);
    Collections.sort(mapLocations, tdc);
  }
  

  /** Returns a list of all locations visible to the unit at its current location. */
  public static ArrayList<XYCoord> findVisibleLocations(GameMap map, Unit viewer, boolean piercing)
  {
    return findVisibleLocations(map, viewer, viewer.x, viewer.y, piercing);
  }
  /** Returns a list of all locations that would be visible to the unit if it were at (x, y). */
  public static ArrayList<XYCoord> findVisibleLocations(GameMap map, Unit viewer, int x, int y, boolean piercing)
  {
    ArrayList<XYCoord> viewables = new ArrayList<XYCoord>();

    if( map.isLocationValid(x, y) )
    {
      int range = (piercing)? viewer.model.visionRangePiercing : viewer.model.visionRange;
      // if it's a surface unit, give it the boost the terrain would provide, so long as it's not adjacent-only vision
      if( (!piercing || range > 1) && viewer.model.isSurfaceUnit() )
        range += map.getEnvironment(x, y).terrainType.getVisionBoost();
      viewables.addAll(findVisibleLocations(map, new XYCoord(x, y), range, piercing));
    }
    
    return viewables;
  }
  /** Returns a list of all locations visible to a unit at origin that could see range tiles. */
  public static ArrayList<XYCoord> findVisibleLocations(GameMap map, XYCoord origin, int range)
  {
    return findVisibleLocations(map, origin, range, false);
  }
  /** Returns a list of all visible locations within range of origin, ignoring cover effects. */
  public static ArrayList<XYCoord> findVisibleLocations(GameMap map, XYCoord origin, int range, boolean piercing)
  {
    ArrayList<XYCoord> locations = new ArrayList<XYCoord>();

    // Loop through all the valid x and y offsets, as dictated by the max range, and add valid spaces to our collection.
    for( int yOff = -range; yOff <= range; ++yOff )
    {
      for( int xOff = -range; xOff <= range; ++xOff )
      {
        int currentRange = Math.abs(xOff) + Math.abs(yOff);
        XYCoord coord = new XYCoord(origin.xCoord + xOff, origin.yCoord + yOff);
        if( currentRange <= range && map.isLocationValid(coord) )
        {
          // If we're adjacent, or we can see through cover, or it's *not* cover, we can see into it.
          if( piercing || !map.getEnvironment(coord).terrainType.isCover() )
          {
            // Add this location to the set.
            locations.add(coord);
          }
        }
      }
    }

    return locations;
  }

  public static boolean pathCollides(GameMap map, Unit unit, GamePath path)
  {
    boolean result = false;
    boolean includeOccupiedSpaces = true; // Shouldn't matter, as we don't invoke canEnd()
    FloodFillFunctor fff = unit.getMoveFunctor(includeOccupiedSpaces);
    if( !fff.canEnd(map, path.getEndCoord()) )
      return false;

    for( int i = 1; i < path.getPathLength(); ++i)
    {
      XYCoord from = path.getWaypoint(i-1).GetCoordinates();
      XYCoord to   = path.getWaypoint( i ).GetCoordinates();
      // If there are collisions relevant to the unit, the cost will be IMPASSABLE
      if( unit.getMovePower(map) < fff.getTransitionCost(map, from, to) )
      {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Evaluates the proposed move, creates a MoveEvent describing it, and adds that event to eventQueue
   * If the move passes over an obstacle, the resulting MoveEvent will have its path shortened accordingly.
   * @param gameMap The world in which the action is to take place.
   * @param unit The unit who is to move.
   * @param movePath The complete sequence of steps the unit proposes to take.
   * @param eventQueue Will be given the new MoveEvent.
   * @return true if the move is created as specified, false if the path was shortened.
   */
  public static boolean enqueueMoveEvent(MapMaster gameMap, Unit unit, GamePath movePath, GameEventQueue eventQueue)
  {
    boolean originalPathOK = true;
    if( Utils.pathCollides(gameMap, unit, movePath) )
    {
      movePath.snipCollision(gameMap, unit);
      originalPathOK = false;
    }
    if( unit.model.needsFuel ) // Check fuel.
    {
      int fuelBurn = movePath.getFuelCost(unit, gameMap);
      if( fuelBurn > unit.fuel )
      {
        movePath.snipForFuel(gameMap, unit);
        originalPathOK = false;
      }
    }
    eventQueue.add(new Engine.GameEvents.MoveEvent(unit, movePath));
    return originalPathOK;
  }

  /**
   * Adds a `UnitDeathEvent` to `eventQueue` for the specified unit. Death events will also be
   * queued for any units being carried as cargo (including if the cargo also has its own cargo).
   * @param unit The unit who is to die.
   * @param eventQueue Will be given the new MoveEvent(s).
   */
  public static void enqueueDeathEvent(Unit unit, GameEventQueue eventQueue)
  {
    Queue<Unit> unitsToDie = new ArrayDeque<Unit>();
    unitsToDie.add(unit);
    do
    {
      Unit utd = unitsToDie.poll();
      eventQueue.add(new UnitDieEvent(utd));
      unitsToDie.addAll(utd.heldUnits);
    } while( !unitsToDie.isEmpty() );
  }

  /**
   * @return Whether the owner of this property will lose if the property is lost
   */
  public static boolean willLoseFromLossOf(MapMaster map, MapLocation capLoc)
  {
    if(null == capLoc.getOwner())
      return false;

    boolean playerWillLose = false;

    TerrainType propertyType = capLoc.getEnvironment().terrainType;

    if( (propertyType == TerrainType.HEADQUARTERS) )
    {
      playerWillLose = true;
    }

    else if( (propertyType == TerrainType.LAB) )
    {
      int numLabs = 0;
      int numHQs = 0;
      for( XYCoord xy : capLoc.getOwner().ownedProperties )
      {
        MapLocation loc = map.getLocation(xy);
        if( loc.getEnvironment().terrainType == TerrainType.LAB )
        {
          numLabs += 1;
          if( numLabs > 1)
            break;
        }
        if( loc.getEnvironment().terrainType == TerrainType.HEADQUARTERS )
        {
          numHQs += 1;
          break;
        }
      }
      if( numLabs < 2 && numHQs == 0 )
      {
        playerWillLose = true;
      }
    }

    return playerWillLose;
  }

  /**
   * Find all locations within `range` spaces of all known properties owned by `cmdr`.
   * @return a Set of all qualifying locations.
   */
  public static Set<XYCoord> findLocationsNearProperties(GameMap gameMap, Commander cmdr, int range)
  {
    HashSet<XYCoord> propTiles = new HashSet<XYCoord>();

    // NOTE: We can't just use cmdr.ownedProperties because that gives away unit locations that we may not be able to see.
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        XYCoord xyc = new XYCoord(x, y);
        if( gameMap.getLocation(xyc).getOwner() == cmdr )
        {
          propTiles.add(xyc);
        }
      }
    }
    return findLocationsNearPoints(gameMap, propTiles, range);
  }

  /**
   * Find all locations within `range` spaces of all known units owned by `cmdr`.
   * @return a Set of all qualifying locations.
   */
  public static Set<XYCoord> findLocationsNearUnits(GameMap gameMap, Commander cmdr, int range)
  {
    HashSet<XYCoord> unitTiles = new HashSet<XYCoord>();

    // NOTE: We can't just use cmdr.units because that gives away unit locations that we may not be able to see.
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        XYCoord xyc = new XYCoord(x, y);
        Unit unit = gameMap.getLocation(xyc).getResident();
        if( unit != null && cmdr == unit.CO )
        {
          unitTiles.add(xyc);
        }
      }
    }
    return findLocationsNearPoints(gameMap, unitTiles, range);
  }

  /**
   * Find all locations within `range` spaces of the specified units.
   * @return a Set of all qualifying locations.
   */
  public static Set<XYCoord> findLocationsNearUnits(GameMap gameMap, Collection<Unit> units, int range)
  {
    HashSet<XYCoord> unitTiles = new HashSet<XYCoord>();

    for( Unit u : units )
    {
      unitTiles.add(new XYCoord(u.x, u.y));
    }
    return findLocationsNearPoints(gameMap, unitTiles, range);
  }

  /**
   * Find all locations within `range` spaces of the given points.
   * @return a Set of all qualifying locations.
   */
  public static Set<XYCoord> findLocationsNearPoints(GameMap gameMap, Collection<XYCoord> points, int range)
  {
    HashSet<XYCoord> tilesInRange = new HashSet<XYCoord>();

    for( XYCoord point : points )
    {
      tilesInRange.addAll(Utils.findLocationsInRange(gameMap, point, 0, range));
    }

    return tilesInRange;
  }

}
