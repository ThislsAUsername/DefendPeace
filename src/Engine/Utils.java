package Engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Set;

import CommandingOfficers.Commander;
import Engine.GameEvents.ArmyDefeatEvent;
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
        XYCoord coord = new XYCoord(origin.x + xOff, origin.y + yOff);
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
    if( cargoMoveType.canStandOn(map.getEnvironment(moveLoc)) )
      for( XYCoord loc : locations )
      {
        // Add any location that is empty and supports movement of the cargo unit.
        if( (map.isLocationEmpty(loc) || map.getLocation(loc).getResident() == transport)
            && cargoMoveType.canStandOn(map.getEnvironment(loc.x, loc.y)) )
        {
          dropoffLocations.add(loc);
        }
      }
    return dropoffLocations;
  }

  // This is much less painful than rewriting all that test code
  public static GamePath findShortestPath(Unit unit, int x, int y, GameMap gameMap)
  {
    GamePath path = new PathCalcParams(unit, gameMap).findShortestPath(x, y);
    return path;
  }
  public static GamePath findShortestPath(Unit unit, XYCoord dest, GameMap gameMap)
  {
    return findShortestPath(unit, dest.x, dest.y, gameMap);
  }

  public static boolean isPathValid(Unit unit, GamePath path, GameMap map, boolean includeOccupiedSpaces)
  {
    return isPathValid(unit, unit.CO.army, unit.getMoveFunctor(), Math.min(unit.getMovePower(map), unit.fuel), path, map, includeOccupiedSpaces);
  }
  public static boolean isPathValid(Unit mover, Army team, MoveType fff, int initialFillPower, GamePath path, GameMap map, boolean includeOccupiedSpaces)
  {
    if( (null == path) || (null == fff) )
    {
      return false;
    }

    // Make sure the path has at least the starting position.
    if( path.getPathLength() <= 0 )
    {
      return false;
    }

    boolean canTravelThroughEnemies = false;
    int movePower = initialFillPower;
    XYCoord lastCoord = path.getWaypoint(0).GetCoordinates();

    // Index from 1 so we don't count the space the unit is on.
    for( int i = 1; i < path.getPathLength(); ++i )
    {
      XYCoord newCoord = path.getWaypoint(i).GetCoordinates();

      movePower -= fff.getTransitionCost(map, lastCoord, newCoord, team, canTravelThroughEnemies);
      lastCoord = newCoord;
      if( movePower < 0 )
      {
        return false;
      }
    }

    return fff.canStandOn(map, lastCoord, mover, includeOccupiedSpaces);
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

    public SearchNode(int x, int y)
    {
      this(x, y, null);
    }

    public SearchNode(XYCoord coord, SearchNode parent)
    {
      this(coord.x, coord.y, parent);
    }
    public SearchNode(int x, int y, SearchNode parent)
    {
      super(x, y);
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
  public static void sortLocationsByDistance(XYCoord center, ArrayList<? extends XYCoord> mapLocations)
  {
    ManhattanDistanceComparator mdc = new ManhattanDistanceComparator(center);
    Collections.sort(mapLocations, mdc);
  }

  /**
   * Compare XYCoords based on how much time it would take myUnit to reach them on the given map.
   * <p>Coords closer to the input coord will be "less than" XYCoords that are farther away.
   */
  public static class TravelDistanceComparator implements Comparator<XYCoord>
  {
    final Unit myUnit;
    // Either the start or the end of our prospective journey, based on reverse
    // Normally the start.
    final XYCoord fixedEndpoint;
    final boolean reverse;
    GameMap myMap;
    HashMap<XYCoord, Integer> distCache;

    public TravelDistanceComparator(Unit unit, XYCoord coord, GameMap map)
    {
      this(unit, coord, map, false);
    }
    public TravelDistanceComparator(Unit unit, XYCoord coord, GameMap map, boolean reverseTravel)
    {
      myUnit = unit;
      fixedEndpoint = coord;
      myMap = map;
      distCache = new HashMap<>();
      reverse = reverseTravel;
    }

    @Override
    public int compare(XYCoord xy1, XYCoord xy2)
    {
      return getCachedDistance(xy1) - getCachedDistance(xy2);
    }
    /**
     * @return The distance from the unit (assumed static) to the coordinate
     */
    public int getCachedDistance(XYCoord xyc)
    {
      if( distCache.containsKey(xyc) )
        return distCache.get(xyc);

      XYCoord start = fixedEndpoint;
      XYCoord end = xyc;
      if( reverse )
      {
        start = xyc;
        end = fixedEndpoint;
      }

      PathCalcParams pcp = new PathCalcParams(myUnit, myMap);
      pcp.setTheoretical();
      pcp.start = start;
      final GamePath path = pcp.findShortestPath(end);
      int distance = Integer.MAX_VALUE;
      if( null != path )
        distance = path.getMoveCost(myUnit, myMap);
      distCache.put(xyc, distance);
      return distance;
    }
  }

  /**
   * Sort coordinates by how long the input unit would take to get to them
   */
  public static void sortLocationsByTravelTime(Unit unit, ArrayList<XYCoord> mapLocations, GameMap map)
  {
    sortLocationsByTravelTime(new XYCoord(unit), unit, mapLocations, map);
  }
  /**
   * Sort coordinates by how long the input unit would take to get to them from "start"
   */
  public static void sortLocationsByTravelTime(XYCoord start, Unit unit, ArrayList<? extends XYCoord> mapLocations, GameMap map)
  {
    TravelDistanceComparator tdc = new TravelDistanceComparator(unit, start, map);
    Collections.sort(mapLocations, tdc);
  }
  /**
   * Sort coordinates by how long the input unit would take to get from them to "end"
   */
  public static void sortLocationsByTravelTimeToCoord(Unit unit, ArrayList<? extends XYCoord> mapLocations, GameMap map, XYCoord end)
  {
    boolean reverseTravel = true;
    TravelDistanceComparator tdc = new TravelDistanceComparator(unit, end, map, reverseTravel);
    Collections.sort(mapLocations, tdc);
  }

  public static boolean pathCollides(GameMap map, Unit unit, GamePath path, boolean includeOccupiedSpaces)
  {
    boolean result = false;
    boolean canTravelThroughEnemies = false;
    final UnitContext uc = new UnitContext(map, unit);
    final MoveType fff = uc.calculateMoveType();
    if( !fff.canStandOn(map, path.getEndCoord(), unit, includeOccupiedSpaces) )
      return true;

    final int movePower = uc.calculateMovePower();
    for( int i = 1; i < path.getPathLength(); ++i)
    {
      XYCoord from = path.getWaypoint(i-1).GetCoordinates();
      XYCoord to   = path.getWaypoint( i ).GetCoordinates();
      // If there are collisions relevant to the unit, the cost will be IMPASSABLE
      if( movePower < fff.getTransitionCost(map, from, to, unit.CO.army, canTravelThroughEnemies) )
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
    return enqueueMoveEvent(gameMap, unit, movePath, eventQueue, false);
  }
  /**
   * enqueueMoveEvent(), but for when you want to end your movement on a friendly
   */
  public static boolean enqueueBoardEvent(MapMaster gameMap, Unit unit, GamePath movePath, GameEventQueue eventQueue)
  {
    return enqueueMoveEvent(gameMap, unit, movePath, eventQueue, true);
  }
  public static boolean enqueueMoveEvent(MapMaster gameMap, Unit unit, GamePath movePath, GameEventQueue eventQueue, boolean includeOccupiedSpaces)
  {
    boolean originalPathOK = true;
    if( Utils.pathCollides(gameMap, unit, movePath, includeOccupiedSpaces) )
    {
      movePath.snipCollision(gameMap, unit);
      originalPathOK = false;
    }
    if( unit.model.fuelBurnPerTile > 0 ) // Check fuel.
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
  public static void enqueueDeathEvent(Unit unit, XYCoord grave, boolean canLose, GameEventQueue eventQueue)
  {
    Queue<Unit> unitsToDie = new ArrayDeque<Unit>();
    unitsToDie.add(unit);
    int totalDeaths = 0;
    do
    {
      Unit utd = unitsToDie.poll();
      eventQueue.add(new UnitDieEvent(utd, grave));
      totalDeaths++;
      unitsToDie.addAll(utd.heldUnits);
    } while (!unitsToDie.isEmpty());

    // If that's the last of your units, it's loss time
    if( canLose && unit.CO.army.getUnits().size() <= totalDeaths )
      eventQueue.add(new ArmyDefeatEvent(unit.CO.army));
  }
  public static void enqueueDeathEvent(Unit unit, GameEventQueue eventQueue)
  {
    enqueueDeathEvent(unit, new XYCoord(unit), true, eventQueue);
  }

  /**
   * @return Whether the owner of this property will lose if the property is lost
   */
  public static boolean willLoseFromLossOf(GameMap map, MapLocation capLoc)
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
