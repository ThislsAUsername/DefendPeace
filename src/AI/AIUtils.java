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
import Engine.GameAction.ActionType;
import Engine.GameActionSet;
import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;

public class AIUtils
{
  /**
   * Finds all actions available to unit, and organizes them by location.
   * @param unit The unit under consideration.
   * @param gameMap The world in which the Unit lives.
   * @return a Map of XYCoord to ArrayList<GameActionSet>. Each XYCoord will have a GameActionSet for
   * each type of action the unit can perform from that location.
   */
  public static Map<XYCoord, ArrayList<GameActionSet> > getAvailableUnitActions(Unit unit, GameMap gameMap)
  {
    Map<XYCoord, ArrayList<GameActionSet> > actions = new HashMap<XYCoord, ArrayList<GameActionSet> >();

    // Find the possible destinations.
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap);

    for( XYCoord coord : destinations )
    {
      // Figure out how to get here.
      Path movePath = Utils.findShortestPath(unit, coord, gameMap);

      // Figure out what I can do here.
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);

      // Add it to my collection.
      actions.put(coord, actionSets);
    }

    return actions;
  }

  /**
   * Finds all actions available to unit, and organizes them by type instead of by location.
   * @param unit The unit under consideration.
   * @param gameMap The world in which the Unit lives.
   * @return a Map of ActionType to ArrayList<GameAction>.
   */
  public static Map<ActionType, ArrayList<GameAction> > getAvailableUnitActionsByType(Unit unit, GameMap gameMap)
  {
    // Create the ActionType-indexed map, and ensure we don't have any null pointers.
    Map<ActionType, ArrayList<GameAction> > actionsByType = new HashMap<ActionType, ArrayList<GameAction> >();
    for( ActionType atype : ActionType.values() )
    {
      actionsByType.put(atype, new ArrayList<GameAction>());
    }

    // First collect the actions by location.
    Map<XYCoord, ArrayList<GameActionSet> > actionsByLoc = getAvailableUnitActions(unit, gameMap);

    // Now re-map them by type, irrespective of location.
    for( ArrayList<GameActionSet> actionSets : actionsByLoc.values() )
    {
      for( GameActionSet actionSet : actionSets )
      {
        ActionType type = actionSet.getSelected().getType();

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
        Location loc = gameMap.getLocation(x, y);
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
        Location loc = gameMap.getLocation(x, y);
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
   */
  public static Map<Commander, ArrayList<Unit> > getUnitsByCommander(GameMap gameMap)
  {
    Map<Commander, ArrayList<Unit> > unitMap = new HashMap<Commander, ArrayList<Unit> >();

    for( int x = 0; x < gameMap.mapWidth; ++x )
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        Unit resident = gameMap.getLocation(x, y).getResident();
        if( null != resident )
        {
          if( !unitMap.containsKey(resident.CO) ) unitMap.put(resident.CO, new ArrayList<Unit>());
          unitMap.get(resident.CO).add(resident);
        }
      }

    return unitMap;
  }

  /**
   * Create and return a GameAction.WaitAction that will move unit towards destination, around
   * any intervening obstacles. If no possible route exists, return false.
   * @param unit The unit we want to move.
   * @param destination Where we eventually want the unit to be.
   * @param gameMap The map on which the unit is moving.
   * @return A GameAction to bring the unit closer to the destination, or null if it is unreachable.
   */
  public static GameAction moveTowardLocation(Unit unit, XYCoord destination, GameMap gameMap)
  {
    GameAction move = null;

    // Find the full path that would get this unit to the destination, regardless of how long. 
    Path path = Utils.findShortestPath(unit, destination, gameMap, true);
    if( path.getPathLength() > 0 ) // Check that the destination is reachable at least in theory.
    {
      path.snip(unit.model.movePower+1); // Trim the path so we don't try to walk through walls.
      ArrayList<XYCoord> validMoves = Utils.findPossibleDestinations(unit, gameMap); // Find the valid moves we can make.
      Utils.sortLocationsByDistance(new XYCoord(path.getEnd().x, path.getEnd().y), validMoves); // Sort moves based on intermediate destination. 
      move = new GameAction.WaitAction(unit, Utils.findShortestPath(unit, validMoves.get(0), gameMap)); // Move to best option.
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
    for( XYCoord xyc : unit.CO.ownedProperties )
    {
      Location loc = unit.CO.myView.getLocation(xyc);
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
        if (null != q)
          q.offer(new GameAction.AbilityAction(ab));
        break;
      }
    }
    return retVal;
  }
  
  /**
   * @return The area threatened by the unit, against the specified target type
   */
  public static Map<XYCoord, Double> findThreatPower(GameMap gameMap, Unit unit, UnitModel target)
  {
    XYCoord origin = new XYCoord(unit.x, unit.y);
    Map<XYCoord, Double> shootableTiles = new HashMap<XYCoord, Double>();
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap);
    for( Weapon wep : unit.weapons )
    {
      if( wep.ammo > 0 )
      {
        if( !wep.model.canFireAfterMoving )
        {
          for (XYCoord xyc : Utils.findLocationsInRange(gameMap, origin, wep.model.minRange, wep.model.maxRange))
            shootableTiles.put(xyc, wep.getDamage(target) * (unit.getHP() * 0.1));
        }
        else
        {
          for( XYCoord dest : destinations )
          {
            for (XYCoord xyc : Utils.findLocationsInRange(gameMap, dest, wep.model.minRange, wep.model.maxRange))
              shootableTiles.put(xyc, wep.getDamage(target) * (unit.getHP() * 0.1));
          }
        }
      }
    }
    return shootableTiles;
  }

  /**
   * Keeps track of a commander's production facilities. When created, it will automatically catalog
   * all available facilities, and all units that can be built. It is then easy to ask whether it is
   * possible to build a given type of unit, or find a location to do so.
   * Once a purchase has been scheduled, removeBuildLocation() will remove a given facility from any
   * further consideration.
   */
  public static class CommanderProductionInfo
  {
    public Commander myCo;
    public Set<UnitModel> availableUnitModels;
    public Set<Location> availableProperties;
    public Map<Terrain.TerrainType, Integer> propertyCounts;
    public Map<UnitModel, Set<TerrainType>> modelToTerrainMap;

    /**
     * Build a model of the production capabilities for a given Commander.
     * Could be used for your own, or your opponent's.
     */
    public CommanderProductionInfo(Commander co, GameMap gameMap)
    {
      // Figure out what unit types we can purchase with our available properties.
      myCo = co;
      availableUnitModels = new HashSet<UnitModel>();
      availableProperties = new HashSet<Location>();
      propertyCounts = new HashMap<Terrain.TerrainType, Integer>();
      modelToTerrainMap = new HashMap<UnitModel, Set<TerrainType>>();

      for( XYCoord xyc : co.ownedProperties )
      {
        Location loc = co.myView.getLocation(xyc);
        if( gameMap.isLocationEmpty(loc.getCoordinates()))
        {
          ArrayList<UnitModel> models = co.getShoppingList(loc);
          availableUnitModels.addAll(models);
          availableProperties.add(loc);
          TerrainType terrain = loc.getEnvironment().terrainType;
          if( propertyCounts.containsKey(terrain))
          {
            propertyCounts.put(terrain, propertyCounts.get(loc.getEnvironment().terrainType)+1);
          }
          else
          {
            propertyCounts.put(terrain, 1);
          }

          // Store a mapping from UnitModel to the TerrainType that can produce it.
          for( UnitModel m : models )
          {
            if( modelToTerrainMap.get(m) == null )
              modelToTerrainMap.put(m, new HashSet<TerrainType>());
            modelToTerrainMap.get(m).add( loc.getEnvironment().terrainType );
          }
        }
      }
    }

    /**
     * Return a location that can build the given unitModel, or null if none remains.
     */
    public Location getLocationToBuild(UnitModel model)
    {
      Set<TerrainType> desiredTerrains = modelToTerrainMap.get(model);
      Location location = null;
      for( Location loc : availableProperties )
      {
        if( desiredTerrains.contains(loc.getEnvironment().terrainType) )
        {
          location = loc;
          break;
        }
      }
      return location;
    }

    /**
     * Remove the given location from further consideration, even if it is still available.
     */
    public void removeBuildLocation(Location loc)
    {
      availableProperties.remove(loc);
      TerrainType terrain = loc.getEnvironment().terrainType;
      if( propertyCounts.containsKey(terrain) )
      {
        propertyCounts.put(terrain, propertyCounts.get(terrain) - 1);
        if( propertyCounts.get(terrain) == 0 )
        {
          availableUnitModels.removeAll(myCo.getShoppingList(loc));
        }
      }
    }

    /**
     * Returns the number of facilities that can produce units of the given type.
     */
    public int getNumFacilitiesFor(UnitModel model)
    {
      int num = 0;
      if( modelToTerrainMap.containsKey(model) )
      {
        for( TerrainType terrain : modelToTerrainMap.get(model) )
        {
          num += propertyCounts.get(terrain);
        }
      }
      return num;
    }
  }

  /**
   * Compares SearchNodes based on the amount of movePower they possess, and optionally
   *   the remaining distance to a destination.
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
}
