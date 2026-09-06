package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;

import AI.AITransportUtils.InterceptPaths;
import AI.ReachabilityCache.Island;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.PathCalcParams;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.PathCalcParams.SearchNode;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import lombok.var;

/**
 *  Just build tons of Infantry and try to rush the opponent.
 */
public class InfantrySpamAI implements AIController
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Army co)
    {
      return new InfantrySpamAI(co);
    }

    @Override
    public String getName()
    {
      return "ISAI";
    }

    @Override
    public String getDescription()
    {
      return
          "Infantry Spam AI (ISAI) knows there are two objectives in this game: Shoot things and capture things.\n" +
          "Infantry can do both, so why build anything else?";
    }
  }
  public static final AIMaker info = new instantiator();

  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }

  Queue<GameAction> actions = new ArrayDeque<GameAction>();

  private Army myArmy = null;

  private ArrayList<XYCoord> unownedProperties;
  private ArrayList<XYCoord> capturingProperties;

  private StringBuffer logger = new StringBuffer();
  private boolean shouldLog = true;
  private int turnNum = 0;
  private ReachabilityCache rc = null;
  private HashSet<Unit> transportsPicked = new HashSet<>();

  public InfantrySpamAI(Army army)
  {
    myArmy = army;
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    turnNum++;
    log(String.format("[======== ISAI initializing turn %s for %s =========]", turnNum, myArmy));

    // Make sure we don't have any hang-ons from last time.
    actions.clear();

    // Create a list of every property we don't own, but want to.
    unownedProperties = AIUtils.findNonAlliedProperties(myArmy, gameMap);
    capturingProperties = new ArrayList<XYCoord>();
    for( Unit unit : myArmy.getUnits() )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        capturingProperties.add(unit.getCaptureTargetCoords());
      }
    }

    // Check for a turn-kickoff power
    AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_TURN_START);

    // Temporary?
    rc = new ReachabilityCache(gameMap.game);
    transportsPicked.clear();
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== ISAI ending turn %s for %s =========]", turnNum, myArmy));
    if( shouldLog )
      System.out.println(logger.toString());
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    if( shouldLog )
      logger.append(message).append('\n');
  }
  @Override
  public void setLogging(boolean value) { shouldLog = value; }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    // If we have more actions ready, don't bother calculating stuff.
    if( !actions.isEmpty() )
    {
      return actions.poll();
    }

    // Handle actions for each unit the CO owns.
    for( Unit unit : myArmy.getUnits() )
    {
      if( unit.isTurnOver || !gameMap.isLocationValid(unit.x, unit.y))
        continue; // No actions for units that are stale or out of bounds
      boolean foundAction = false;

      boolean includeOccupiedDestinations = false;
      Map<UnitActionFactory, ArrayList<GameAction>> unitActionsByType = AIUtils.getAvailableUnitActionsByType(unit, gameMap, includeOccupiedDestinations);

      // See if we have the option to attack.
      ArrayList<GameAction> attackActions = unitActionsByType.get(UnitActionFactory.ATTACK);
      if( null != attackActions && !attackActions.isEmpty() )
      {
        actions.offer(attackActions.get(0));
        foundAction = true;
      }
      if(foundAction)break; // Only one action per getNextAction() call, to avoid overlap.

      // Otherwise, see if we have the option to capture.
      ArrayList<GameAction> captureActions = unitActionsByType.get(UnitActionFactory.CAPTURE);
      if( null != captureActions && !captureActions.isEmpty() )
      {
        actions.offer(captureActions.get(0));
        foundAction = true;
      }
      if(foundAction)break; // Only one action per getNextAction() call, to avoid overlap.

      // Otherwise², see if we have the option to hop in a transport, or capture something underneath it (for softlock prevention).
      ArrayList<GameAction> loadActions = unitActionsByType.get(UnitActionFactory.LOAD);
      if( null != loadActions && !loadActions.isEmpty() )
      {
        GameAction la = loadActions.get(0);
        if( unit.hasActionType(UnitActionFactory.CAPTURE) )
        {
          XYCoord moveLoc = la.getMoveLocation();
          var path = new PathCalcParams(unit, gameMap).findShortestPath(moveLoc);
          boolean ignoreResident = true;
          GameActionSet potentialCaps = UnitActionFactory.CAPTURE.getPossibleActions(gameMap, path, unit, ignoreResident);
          var resident = gameMap.getResident(moveLoc); // LOAD requires a transport there.
          if ( null != potentialCaps && !resident.isTurnOver && resident.CO.army == this.myArmy )
          {
            HashSet<XYCoord> exclusions = new HashSet<>();
            exclusions.add(moveLoc);
            GameAction moveT = AIUtils.moveTowardLocation(resident, moveLoc, gameMap, exclusions);
            if( null != moveT )
            {
              actions.offer(moveT); // Move the transport so I can get the juicy bits.
              actions.offer(potentialCaps.getSelected());
            }
          }
        }
        if( actions.isEmpty() ) // No cap action, so do the boring thing.
          actions.offer(la);
        foundAction = true;
      }
      if(foundAction)break; // Only one action per getNextAction() call, to avoid overlap.

      if( unit.hasCargoSpace(UnitModel.TROOP) && unit.heldUnits.isEmpty() )
        continue; // Don't bother with moving infantry transports around until they're called.

      // Unload logic
      if( !unit.heldUnits.isEmpty() )
      {
        // I dunno how to respect Lander travel time and also care about nearby land tiles, so pretend we can fly
        Utils.sortLocationsByDistance(new XYCoord(unit), unownedProperties);

        log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
        for( XYCoord goal : unownedProperties )
        {
          GameAction toUnload = calcUnloadAction(gameMap, unit, unitActionsByType, goal); // Includes approaching the unload zone.
          if( null == toUnload )
            continue;
          actions.offer(toUnload);
          foundAction = true;
          break;
        }
      }
      if(foundAction)break; // Only one action per getNextAction() call, to avoid overlap.
      // This does mean transports who are loaded but can't unload will chase open ports, but that's fine.

      // If no attack/capture actions are available now, just move towards a non-allied building.
      Utils.sortLocationsByTravelTime(unit, unownedProperties, gameMap);
      if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
      {
        log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
        int index = 0;
        XYCoord goal = null;
        GamePath path = null;
        boolean validTarget = false;

        // Loop until we find a valid property to go capture or run out of options.
        do
        {
          goal = unownedProperties.get(index++);
          path = new PathCalcParams(unit, gameMap).setTheoretical().findShortestPath(goal);
          boolean desirable = myArmy.isEnemy(gameMap.getLocation(goal).getOwner()); // Property is not allied.
          desirable &= !capturingProperties.contains(goal); // We aren't already capturing it.
          validTarget = desirable && (path != null); // We can reach it.
          log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal, (validTarget?"Yes":"No")));
          if( !validTarget && desirable )
          {
            queueTransportEnablerActions(gameMap, unit, goal); // Includes production, which seems fine to prioritize since we have stranded units.
            validTarget = !actions.isEmpty();
            log(String.format("      %s at %s via transport? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal, (validTarget?"Yes":"No")));
          }
        } while( !validTarget && (index < unownedProperties.size()) );      // Loop until we run out of properties to check.

        if( !validTarget )
        {
          log("    Failed to find a path to a capturable property. Waiting");
          // We couldn't find a valid move point (are we on an island?). Just give up.
          GameAction wait = new WaitLifecycle.WaitAction(unit, GamePath.stayPut(unit));
          actions.offer(wait);
          break;
        }

        log(String.format("    Selected %s at %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal));

        GameAction move = AIUtils.moveTowardLocation(unit, goal, gameMap);
        if( null != move )
        {
          actions.offer(move);
          break;
        }
      }
    }

    // Check for an available buying enhancement power
    if( actions.isEmpty() )
    {
      AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_BUY);
    }

    // Finally, build more infantry. We will add all build commands at once, since they can't conflict.
    if( actions.isEmpty() )
    {
      int budget = myArmy.money;
      int currentUnitCount = myArmy.getUnits().size();
      // Create a list of actions to build infantry on every open factory, then return these actions until done.
      for( int i = 0; i < gameMap.mapWidth; i++ )
      {
        for( int j = 0; j < gameMap.mapHeight; j++)
        {
          if( myArmy.gameRules.unitCap <= currentUnitCount + actions.size() )
            break; // Don't build too many mans
          MapLocation loc = gameMap.getLocation(i, j);
          Commander buyer = loc.getOwner();
          if(null == buyer)
            continue;
          // If this terrain belongs to me, and I can build something on it, and I have the money, do so.
          if( loc.getEnvironment().terrainType == TerrainType.FACTORY && buyer.army == myArmy && loc.getResident() == null )
          {
            ArrayList<UnitModel> units = buyer.getShoppingList(loc);
            final int buyCost = buyer.getBuyCost(units.get(0), loc.getCoordinates());
            if( !units.isEmpty() && buyCost <= budget )
            {
              GameAction action = new GameAction.UnitProductionAction(buyer, units.get(0), loc.getCoordinates());
              budget -= buyCost;
              actions.offer( action );
            }
          }
        }
      }
    }

    // Check for a turn-ending power
    if( actions.isEmpty() )
    {
      AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_TURN_END);
    }

    // Return the next action, or null if actions is empty.
    GameAction nextAction = actions.poll();
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }

  protected GameAction calcUnloadAction(GameMap gameMap, Unit unit, Map<UnitActionFactory, ArrayList<GameAction>> unitActionsByType, XYCoord goal)
  {
    Unit cargo        = unit.heldUnits.get(0);
    Island goalIsland = rc.getIsland(cargo.model.baseMoveType, goal);
    var myIslands     = rc.getAdjacentIslands(new UnitContext(unit), gameMap);
    if( null == goalIsland || myIslands.isEmpty() )
      return null; // Ignore HQ bboats
    boolean goalReachable = myIslands.contains(goalIsland);
    for (var is : myIslands)
      goalReachable |= is.overlapIslands.contains(goalIsland);
    log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal, (goalReachable?"Yes":"No")));
    if( !goalReachable )
      return null;

    // This is a kind of inefficient/wak way of doing this, but I just wanna have something working to PR.
    // See the other function for what I designed the API for.
    var loadPoints = new HashMap<XYCoord, InterceptPaths>();
    var ip = new InterceptPaths();
    ip.cargo     = new SearchNode(unit.x, unit.y, 0);
    ip.transport = new SearchNode(unit.x, unit.y, 0);
    loadPoints.put(new XYCoord(unit), ip);
    HashMap<UnitModel, HashSet<XYCoord>> modelTToUnloadPoints = AITransportUtils.findUnloadTiles(gameMap, rc, new UnitContext(cargo), goal);
    HashSet<SearchNode> unloadPaths = AITransportUtils.findShortestUnloadTrips(gameMap, new UnitContext(unit), loadPoints, modelTToUnloadPoints.get(unit.model));

    ArrayList<GameAction> unloadActions = unitActionsByType.get(UnitActionFactory.UNLOAD);
    if( null != unloadActions && !unloadActions.isEmpty() )
      for( GameAction action : unloadActions )
      {
        // Rather than dig into the unload action's bits, just assume we're dropping off the first cargo unit.
        if( unloadPaths.contains(action.getMoveLocation()) )
          return action;
      }

    // If we're not able to unload, just move in the appropriate direction.
    for( SearchNode snU : unloadPaths )
    {
      GameAction move = AIUtils.moveTowardLocation(unit, snU, gameMap);
      if( null != move )
        return move;
    }

    return null;
  }

  protected void queueTransportEnablerActions(GameMap gameMap, Unit unit, XYCoord goal)
  {
    Island goalIsland = rc.getIsland(unit.model.baseMoveType, goal);
    Island myIsland   = rc.getIsland(unit);
    if( null == goalIsland )
      return; // Unit cannot even reach the goal with help; landlocked boats get to be sad on their own.
    if( myIsland == goalIsland )
      return; // Units that can already reach the destination don't need a special ride.

    // For existing transports: Select the first one that hasn't been scheduled this turn, could unload, and could pick the cargo up.
    UnitContext uc = new UnitContext(unit);
    HashMap<UnitModel, HashSet<XYCoord>> modelTToUnloadPoints = AITransportUtils.findUnloadTiles(gameMap, rc, uc, goal);
    UnitContext myRide = null;
    for( Unit possibleTransport : myArmy.getUnits() )
      if( !transportsPicked.contains(possibleTransport) )
        if( goalIsland.overlapIslands.contains(rc.getIsland(possibleTransport)) )
          if( modelTToUnloadPoints.containsKey(possibleTransport.model) // Ignore the possibility of carrying 2 inf. If 2 are available, they can load on the same turn.
              && possibleTransport.heldUnits.isEmpty() )
          {
            // Note that unit order is consistent, so I shouldn't have transports changing their minds too much.
            myRide = new UnitContext(possibleTransport);
            transportsPicked.add(myRide.unit);
            break;
          }
    if( modelTToUnloadPoints.isEmpty() )
      return; // No dropping is possible

    // No transport? That's fine; we can try to buy one.
    if( null == myRide )
    {
      boolean includeFriendlyOccupied = false;
      CommanderProductionInfo CPI = new CommanderProductionInfo(myArmy, gameMap, includeFriendlyOccupied);
      for( UnitModel modelT : modelTToUnloadPoints.keySet() )
      {
        ArrayList<MapLocation> modelTsources = CPI.getAllFacilitiesFor(modelT);
        for( MapLocation loc : modelTsources )
        {
          XYCoord locXYC = loc.getCoordinates();
          Island builtTransportIsland = rc.getIsland(modelT.baseMoveType, locXYC);
          if( !goalIsland.overlapIslands.contains(builtTransportIsland) )
            continue; // Can't reach the destination.
          if( !myIsland.overlapIslands.contains(builtTransportIsland) )
            continue; // Can't load into the transport.
          Commander buyer = loc.getOwner();
          if( buyer.getBuyCost(modelT, locXYC) <= myArmy.money )
          {
            actions.offer( new GameAction.UnitProductionAction(buyer, modelT, locXYC) );
            myRide = new UnitContext(buyer, modelT);
            myRide.setCoord(locXYC);
            break; // We can plan the first move action now
          }
        }
        if( null != myRide )
          break; // Buying two transports for one dude is a bit much
      }
    }

    if( null == myRide )
      return; // Oh well, give up

    // We have a transport, so figure out the intercept point and move toward it.
    HashMap<XYCoord, InterceptPaths> loadPoints = AITransportUtils.findFirstLoadIntercepts(gameMap, rc, myRide, uc);
    HashSet<SearchNode> unloadPaths = AITransportUtils.findShortestUnloadTrips(gameMap, myRide, loadPoints, modelTToUnloadPoints.get(myRide.model));

    // Here, we've either found and scheduled a transport, or bought one.
    // Move the cargo (and transport, if able) towards the rendezvous point.
    for( SearchNode snU : unloadPaths )
    {
      GamePath up = snU.getMyPath();
      XYCoord loadPoint = up.getWaypoint(0);
      GameAction moveT = null;
      if( null != myRide.unit && !myRide.unit.isTurnOver )
        moveT = AIUtils.moveTowardLocation(myRide.unit, loadPoint, gameMap);

      HashSet<XYCoord> exclusions = new HashSet<>();
      exclusions.add(loadPoint);
      exclusions.add(new XYCoord(unit));
      GameAction moveC = AIUtils.moveTowardLocation(unit, loadPoint, gameMap, exclusions);
      if( null == moveC ) // We need to make sure the cargo moves first, since it might need to vacate loadPoint.
        continue;
      if( null != moveT )
        actions.offer(moveT);
      actions.offer(moveC);
      break;
    }
  } // ~calcTransitAction

}
