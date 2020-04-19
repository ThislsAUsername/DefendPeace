package Engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Queue;

import CommandingOfficers.Commander;
import Engine.Path.PathNode;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Units.Unit;
import Units.WeaponModel;

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

  /** Returns a list of locations of all enemy units that weapon could hit from attackerPosition. */
  public static ArrayList<XYCoord> findTargetsInRange(GameMap map, Commander co, XYCoord attackerPosition, WeaponModel weapon)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, attackerPosition, weapon.minRange, weapon.maxRange);
    ArrayList<XYCoord> targets = new ArrayList<XYCoord>();
    for( XYCoord loc : locations )
    {
      if( map.getLocation(loc).getResident() != null && // Someone is there.
          map.getLocation(loc).getResident().CO.isEnemy(co) && // They are not friendly.
          weapon.getDamage(map.getLocation(loc).getResident().model) > 0 ) // We can shoot them.
      {
        targets.add(loc);
      }
    }
    return targets;
  }

  /** Returns a list of locations at distance 1 from transportLoc that cargo can move on. */
  public static ArrayList<XYCoord> findUnloadLocations(GameMap map, Unit transport, XYCoord moveLoc, Unit cargo)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, moveLoc, 1);
    ArrayList<XYCoord> dropoffLocations = new ArrayList<XYCoord>();
    if( cargo.model.propulsion.canTraverse(map.getEnvironment(moveLoc)) )
      for( XYCoord loc : locations )
      {
        // Add any location that is empty and supports movement of the cargo unit.
        if( (map.isLocationEmpty(loc) || map.getLocation(loc).getResident() == transport)
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

  /** Alias for {@link #findPossibleDestinations(XYCoord, Unit, GameMap, boolean) findPossibleDestinations()} **/
  public static ArrayList<XYCoord> findPossibleDestinations(Unit unit, GameMap gameMap)
  {
    return findPossibleDestinations(new XYCoord(unit.x, unit.y), unit, gameMap);
  }
  /** Alias for {@link #findPossibleDestinations(XYCoord, Unit, GameMap, boolean) findPossibleDestinations()} **/
  public static ArrayList<XYCoord> findPossibleDestinations(XYCoord start, Unit unit, GameMap gameMap)
  {
    return findFloodFillArea(start, unit.getMoveFunctor(), Math.min(unit.model.movePower, unit.fuel), gameMap);
  }
  /**
   * Returns the list of XYCoords in gameMap reachable by unit this turn.
   * @param unit The unit to evaluate.
   * @param gameMap The map to search over.
   * @param includeOccupiedSpaces If true, will include spaces occupied by a friendly unit, if some action could end on this space (e.g. LOAD, JOIN).
   */
  public static ArrayList<XYCoord> findFloodFillArea(XYCoord start, FloodFillFunctor fff, int initialFillPower, GameMap gameMap)
  {
    ArrayList<XYCoord> reachableTiles = new ArrayList<XYCoord>();

    if( null == fff || null == start || start.xCoord < 0 || start.yCoord < 0 )
    {
      System.out.println("WARNING! Finding destinations for ineligible unit!");
      return reachableTiles;
    }

    // set all locations to unreachable/remaining move = 0
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

      expandSearchNode(fff, gameMap, currentNode, searchQueue, powerGrid, false);

      currentNode = null;
    }

    return reachableTiles;
  }

  public static boolean isPathValid(Unit unit, Path path, GameMap map)
  {
    return isPathValid(new XYCoord(unit.x, unit.y), unit, path, map);
  }
  public static boolean isPathValid(XYCoord start, Unit unit, Path path, GameMap map)
  {
    return isPathValid(start, unit.getMoveFunctor(), Math.min(unit.model.movePower, unit.fuel), path, map);
  }
  public static boolean isPathValid(XYCoord start, FloodFillFunctor fff, int initialFillPower, Path path, GameMap map)
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

      movePower = fff.getRemainingFillPower(map, movePower, lastCoord, newCoord, false);
      lastCoord = newCoord;
      if( movePower < 0 )
      {
        return false;
      }
    }

    return fff.canEnd(map, lastCoord);
  }

  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static Path findShortestPath(Unit unit, XYCoord destination, GameMap map, boolean theoretical)
  {
    return findShortestPath(unit, destination.xCoord, destination.yCoord, map, theoretical);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static Path findShortestPath(Unit unit, int x, int y, GameMap map, boolean theoretical)
  {
    return findShortestPath(new XYCoord(unit.x, unit.y), unit, x, y, map, theoretical);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static Path findShortestPath(Unit unit, XYCoord destination, GameMap map)
  {
    return findShortestPath(unit, destination.xCoord, destination.yCoord, map, false);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static Path findShortestPath(Unit unit, int x, int y, GameMap map)
  {
    return findShortestPath(unit, x, y, map, false);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static Path findShortestPath(XYCoord start, Unit unit, int x, int y, GameMap map)
  {
    return findShortestPath(start, unit, x, y, map, false);
  }
  /** Alias for {@link #findShortestPath(XYCoord, Unit, int, int, GameMap, boolean) findShortestPath(XYCoord, Unit, int, int, GameMap, boolean=false)} **/
  public static Path findShortestPath(XYCoord start, Unit unit, int x, int y, GameMap map, boolean theoretical)
  {
    return findShortestPath(start, unit.getMoveFunctor(), Math.min(unit.model.movePower, unit.fuel), x, y, map, theoretical);
  }
  /**
   * Calculate and return the shortest path for unit to take from its current location to map(x, y).
   * The Path will avoid non-allied units unless `theoretical` is true.
   * If no valid path is found, an empty Path will be returned.
   * @param unit The unit under consideration. The Unit's current location will be the starting point for the path,
   *             and the unit's move-power will limit the path length.
   * @param destination The desired endpoint for the Path.
   * @param map The current GameMap referenced by the Path returned.
   * @param theoretical If true, ignores other Units and move-power limitations.
   */
  public static Path findShortestPath(XYCoord start, FloodFillFunctor fff, int initialFillPower, int x, int y, GameMap map, boolean theoretical)
  {
    if( null == start || null == fff || null == map || !map.isLocationValid(start.xCoord, start.yCoord) )
    {
      return null;
    }

    Path aPath = new Path(100);
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
    powerGrid[start.xCoord][start.yCoord] = (theoretical)? Integer.MAX_VALUE : initialFillPower;
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

      expandSearchNode(fff, map, currentNode, searchQueue, powerGrid, theoretical);

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
      int[][] powerGrid, boolean theoretical)
  {
    ArrayList<XYCoord> coordsToCheck = findLocationsInRange(map, currentNode.getCoordinates(), 1, 1);

    for( XYCoord next : coordsToCheck )
    {
      // If we can move more cheaply than previously discovered,
      // then update the power grid and re-queue the next node.
      int oldPower = powerGrid[currentNode.x][currentNode.y];
      int oldNextPower = powerGrid[next.xCoord][next.yCoord];
      int newNextPower = fff.getRemainingFillPower(map, oldPower, currentNode.getCoordinates(), next, theoretical);

      // If we are playing "What if" then don't worry too much about move cost.
      boolean canMove = (theoretical) ? true : (newNextPower >= 0);

      if( canMove && (newNextPower > oldNextPower) )
      {
        powerGrid[next.xCoord][next.yCoord] = newNextPower;
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
   * Returns a list of all vacant industries a commander owns
   */
  public static ArrayList<XYCoord> findUsableProperties(Commander co, GameMap map)
  {
    ArrayList<XYCoord> industries = new ArrayList<XYCoord>();
    // We don't want to bother if we're trying to find nobody's properties
    if( null != co )
    {
      // Add all vacant, <co>-owned industries to the list
      for( XYCoord xyc : co.ownedProperties )
      {
        Location loc = map.getLocation(xyc);
        Unit resident = loc.getResident();
        // We only want industries we can act on, which means they need to be empty
        // TODO: maybe calculate whether the CO has enough money to buy something at this industry
        if( null == resident && loc.getOwner() == co )
        {
          if( co.getShoppingList(loc).size() > 0 )
          {
            industries.add(loc.getCoordinates());
          }
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
      int xy1Dist = Math.abs(xy1.xCoord - myCenter.xCoord) + Math.abs(xy1.yCoord - myCenter.yCoord);
      int xy2Dist = Math.abs(xy2.xCoord - myCenter.xCoord) + Math.abs(xy2.yCoord - myCenter.yCoord);

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

  public static boolean pathCollides(GameMap map, Unit unit, Path path)
  {
    boolean result = false;
    for( PathNode point : path.getWaypoints() )
    {
      Unit obstacle = map.getLocation(point.x, point.y).getResident();
      if( null != obstacle && unit.CO.isEnemy(obstacle.CO) )
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
  public static boolean enqueueMoveEvent(MapMaster gameMap, Unit unit, Path movePath, GameEventQueue eventQueue)
  {
    boolean originalPathOK = true;
    if( Utils.pathCollides(gameMap, unit, movePath) )
    {
      movePath.snipCollision(gameMap, unit);
      originalPathOK = false;
    }
    eventQueue.add(new Engine.GameEvents.MoveEvent(unit, movePath));
    return originalPathOK;
  }

  public static HashSet<XYCoord> findLocationsNearProperties(GameMap gameMap, Commander cmdr, int range)
  {
    HashSet<XYCoord> tilesInRange = new HashSet<XYCoord>();
    for( XYCoord prop : cmdr.ownedProperties )
    {
      tilesInRange.addAll(Utils.findLocationsInRange(gameMap, prop, 0, range));
    }
    return tilesInRange;
  }

  public static HashSet<XYCoord> findLocationsNearUnits(GameMap gameMap, Commander cmdr, int range)
  {
    HashSet<XYCoord> tilesInRange = new HashSet<XYCoord>();
    for( Unit unit : cmdr.units )
    {
      tilesInRange.addAll(Utils.findLocationsInRange(gameMap, new XYCoord(unit.x, unit.y), 0, range));
    }
    return tilesInRange;
  }
}
