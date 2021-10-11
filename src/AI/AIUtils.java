package AI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Terrain.MapLocation;
import Units.Unit;

public class AIUtils
{
  /**
   * Finds all actions available to unit, and organizes them by location.
   * @param unit The unit under consideration.
   * @param gameMap The world in which the Unit lives.
   * @return a Map of XYCoord to ArrayList<GameActionSet>. Each XYCoord will have a GameActionSet for
   * each type of action the unit can perform from that location.
   */
  public static Map<XYCoord, ArrayList<GameActionSet> >
                getAvailableUnitActions(Unit unit, GameMap gameMap)
  {
    boolean includeOccupiedDestinations = false;
    return getAvailableUnitActions(unit, gameMap, includeOccupiedDestinations);
  }

  /**
   * Finds all actions available to unit, and organizes them by location.
   * @param unit The unit under consideration.
   * @param gameMap The world in which the Unit lives.
   * @param includeOccupiedDestinations Whether to include destinations underneath our other units.
   * @return a Map of XYCoord to ArrayList<GameActionSet>. Each XYCoord will have a GameActionSet for
   * each type of action the unit can perform from that location.
   */
  public static Map<XYCoord, ArrayList<GameActionSet> >
                getAvailableUnitActions(Unit unit, GameMap gameMap, boolean includeOccupiedDestinations)
  {
    Map<XYCoord, ArrayList<GameActionSet> > actions = new HashMap<XYCoord, ArrayList<GameActionSet> >();

    // Find the possible destinations.
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedDestinations);

    for( XYCoord coord : destinations )
    {
      // Figure out how to get here.
      GamePath movePath = Utils.findShortestPath(unit, coord, gameMap);

      // Figure out what I can do here.
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, includeOccupiedDestinations);

      // Add it to my collection.
      actions.put(coord, actionSets);
    }

    return actions;
  }

  /**
   * Finds all actions available to unit, and organizes them by type instead of by location.
   * Assumes caller isn't interested in moving into units' current spaces
   * @param unit The unit under consideration.
   * @param gameMap The world in which the Unit lives.
   * @return a Map of ActionType to ArrayList<GameAction>.
   */
  public static Map<UnitActionFactory, ArrayList<GameAction> > getAvailableUnitActionsByType(Unit unit, GameMap gameMap)
  {
    boolean includeOccupiedDestinations = false;
    return getAvailableUnitActionsByType(unit, gameMap, includeOccupiedDestinations);
  }
  /**
   * Finds all actions available to unit, and organizes them by type instead of by location.
   * @param unit The unit under consideration.
   * @param gameMap The world in which the Unit lives.
   * @param includeOccupiedDestinations Determines whether to consider actions that would require moving to a space already occupied by a friendly unit.
   * @return a Map of ActionType to ArrayList<GameAction>.
   */
  public static Map<UnitActionFactory, ArrayList<GameAction> > getAvailableUnitActionsByType(Unit unit, GameMap gameMap, boolean includeOccupiedDestinations)
  {
    // Create the ActionType-indexed map, and ensure we don't have any null pointers.
    Map<UnitActionFactory, ArrayList<GameAction> > actionsByType = new HashMap<UnitActionFactory, ArrayList<GameAction> >();
    for( UnitActionFactory atype : unit.model.possibleActions )
    {
      actionsByType.put(atype, new ArrayList<GameAction>());
    }

    // First collect the actions by location.
    Map<XYCoord, ArrayList<GameActionSet> > actionsByLoc = getAvailableUnitActions(unit, gameMap, includeOccupiedDestinations);

    // Now re-map them by type, irrespective of location.
    for( ArrayList<GameActionSet> actionSets : actionsByLoc.values() )
    {
      for( GameActionSet actionSet : actionSets )
      {
        UnitActionFactory type = actionSet.getSelected().getType();

        // Add these actions to the correct map bucket.
        actionsByType.get(type).addAll(actionSet.getGameActions());
      }
    }
    return actionsByType;
  }

  /**
   * Find locations of every property that is not owned by myCo or an ally.
   * @param myCo Properties of this Commander and allies will be ignored.
   * @param gameMap The map to search for properties.
   * @return An ArrayList with the XYCoord of every property that fills the bill.
   */
  public static ArrayList<XYCoord> findNonAlliedProperties(Commander myCo, GameMap gameMap)
  {
    ArrayList<XYCoord> props = new ArrayList<XYCoord>();
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        MapLocation loc = gameMap.getLocation(x, y);
        if( loc.isCaptureable() && myCo.isEnemy(loc.getOwner()) )
        {
          props.add(new XYCoord(x, y));
        }
      }
    }
    return props;
  }

  /**
   * Find the locations of all non-allied units.
   * @param myCo Units owned by myCo or allies are skipped.
   * @param gameMap The map to search for units.
   * @return An ArrayList with the XYCoord of every unit not allied with myCo.
   */
  public static ArrayList<XYCoord> findEnemyUnits(Commander myCo, GameMap gameMap)
  {
    ArrayList<XYCoord> unitLocs = new ArrayList<XYCoord>();
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        MapLocation loc = gameMap.getLocation(x, y);
        if( loc.getResident() != null && myCo.isEnemy(loc.getResident().CO) )
        {
          unitLocs.add(new XYCoord(x, y));
        }
      }
    }
    return unitLocs;
  }

  /**
   * Creates a map of COs to the units they control, based on what can be seen in the passed-in map.
   * Units owned by allies are ignored - ours can be trivially accessed via Commander.units, and we don't control ally behavior.
   */
  public static Map<Commander, ArrayList<Unit> > getEnemyUnitsByCommander(Commander myCommander, GameMap gameMap)
  {
    Map<Commander, ArrayList<Unit> > unitMap = new HashMap<Commander, ArrayList<Unit> >();

    for( int x = 0; x < gameMap.mapWidth; ++x )
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        Unit resident = gameMap.getLocation(x, y).getResident();
        if( (null != resident) && (myCommander.isEnemy(resident.CO)) )
        {
          if( !unitMap.containsKey(resident.CO) ) unitMap.put(resident.CO, new ArrayList<Unit>());
          unitMap.get(resident.CO).add(resident);
        }
      }

    return unitMap;
  }

  /** Overload of {@link #moveTowardLocation(Unit, XYCoord, GameMap, Set)} **/
  public static GameAction moveTowardLocation(Unit unit, XYCoord destination, GameMap gameMap )
  {
    return moveTowardLocation(unit, destination, gameMap, null);
  }

  /**
   * Create and return a GameAction.WaitAction that will move unit towards destination, around
   * any intervening obstacles. If no possible route exists, return false.
   * @param unit The unit we want to move.
   * @param destination Where we eventually want the unit to be.
   * @param gameMap The map on which the unit is moving.
   * @return A GameAction to bring the unit closer to the destination, or null if no available move exists.
   * @param excludeDestinations A list of coordinates of Map locations we don't want to move to.
   */
  public static GameAction moveTowardLocation(Unit unit, XYCoord destination, GameMap gameMap, Set<XYCoord> excludeDestinations )
  {
    GameAction move = null;

    // Find the full path that would get this unit to the destination, regardless of how long. 
    GamePath path = Utils.findShortestPath(unit, destination, gameMap, true);
    boolean includeOccupiedSpaces = false;
    ArrayList<XYCoord> validMoves = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedSpaces); // Find the valid moves we can make.

    if( path.getPathLength() > 0 && validMoves.size() > 0 ) // Check that the destination is reachable at least in theory.
    {
      path.snip(unit.getMovePower(gameMap)+1); // Trim the path so we go the right immediate direction.
      if( null != excludeDestinations) validMoves.removeAll(excludeDestinations);
      if( !validMoves.isEmpty() )
      {
        Utils.sortLocationsByDistance(path.getEndCoord(), validMoves); // Sort moves based on intermediate destination.
        move = new WaitLifecycle.WaitAction(unit, Utils.findShortestPath(unit, validMoves.get(0), gameMap)); // Move to best option.
      }
    }
    return move;
  }

  /**
   * Return a list of all friendly territories at which a unit can resupply and repair.
   * @param unit
   * @return
   */
  public static ArrayList<XYCoord> findRepairDepots(Unit unit)
  {
    ArrayList<XYCoord> stations = new ArrayList<XYCoord>();
    for( XYCoord xyc : unit.CO.ownedProperties ) // TODO: Revisit if we ever get a CO that repairs on non-owned or non-properties
    {
      MapLocation loc = unit.CO.myView.getLocation(xyc);
      if( unit.model.canRepairOn(loc) )
      {
        stations.add(loc.getCoordinates());
      }
    }
    return stations;
  }
  
  /**
   * Find a usable ability that has all specified flags, and add it to the provided queue.
   */
  public static CommanderAbility queueCromulentAbility(Queue<GameAction> q, Commander co, int flags)
  {
    CommanderAbility retVal = null;
    ArrayList<CommanderAbility> abilities = co.getReadyAbilities();
    for( CommanderAbility ab : abilities )
    {
      if( flags == (ab.AIFlags & flags) )
      {
        retVal = ab;
      }
    }
    if (null != q && null != retVal)
      q.offer(new GameAction.AbilityAction(co, retVal));
    return retVal;
  }

  /**
   * @return Whether a friendly CO is currently in the process of acquiring the specified coordinates
   */
  public static boolean isCapturing(GameMap map, Commander co, XYCoord coord)
  {
    Unit unit = map.getLocation(coord).getResident();
    if( null == unit || co.isEnemy(unit.CO) )
      return false;
    return unit.getCaptureProgress() > 0;
  }

  /**
   * @return Whether a friendly CO can build from the specified coordinates
   */
  public static boolean isFriendlyProduction(GameMap map, Commander co, XYCoord coord)
  {
    Commander owner = map.getLocation(coord).getOwner();
    if( null == owner || co.isEnemy(owner) )
      return false;
    return owner.unitProductionByTerrain.containsKey(map.getEnvironment(coord).terrainType);
  }

  /**
   * Compares Units based on their value, scaled with HP.
   */
  public static class UnitCostComparator implements Comparator<Unit>
  {
    boolean sortAscending;

    public UnitCostComparator(boolean sortAscending)
    {
      this.sortAscending = sortAscending;
    }

    @Override
    public int compare(Unit o1, Unit o2)
    {
      int diff = o2.model.getCost()*o2.getHP() - o1.model.getCost()*o1.getHP();
      if (sortAscending)
        diff *= -1;
      return diff;
    }
  }

  /**
   * Compares GameActions based on their `getMoveLocation()`'s distance from the provided `destination`.
   * GameActions closer to `destination` will be sorted to be first.
   */
  public static class DistanceFromLocationComparator implements Comparator<GameAction>
  {
    XYCoord target;

    public DistanceFromLocationComparator(XYCoord destination)
    {
      target = destination;
    }

    @Override
    public int compare(GameAction o1, GameAction o2)
    {
      XYCoord o1c = o1.getMoveLocation();
      XYCoord o2c = o2.getMoveLocation();
      int o1Dist = (null == o1c) ? Integer.MAX_VALUE : o1c.getDistance(target);
      int o2Dist = (null == o2c) ? Integer.MAX_VALUE : o2c.getDistance(target);
      int diff = o1Dist - o2Dist;
      return diff;
    }
  }

  /**
   * Finds allied production centers in the input set
   */
  public static Set<XYCoord> findAlliedIndustries(GameMap gameMap, Commander co, Iterable<XYCoord> coords, boolean ignoreMyOwn)
  {
    Set<XYCoord> result = new HashSet<XYCoord>();
    for( XYCoord coord : coords )
    {
      Commander owner = gameMap.getLocation(coord).getOwner();
      if( co.isEnemy(owner) )
        continue; // counts as a null check on owner
      if( ignoreMyOwn && co == owner )
        continue;
      if( owner.unitProductionByTerrain.containsKey(gameMap.getEnvironment(coord).terrainType) )
        result.add(coord);
    }
    return result;
  }
}
