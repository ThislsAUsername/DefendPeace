package AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameAction.ActionType;
import Engine.GameActionSet;
import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.Location;
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
}
