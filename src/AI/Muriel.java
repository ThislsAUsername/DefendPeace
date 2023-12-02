package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.CombatEngine;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 * Muriel will Make Units Reactively, Informed by the Enemy Loadout.
 */
public class Muriel implements AIController
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Army co)
    {
      return new Muriel(co);
    }

    @Override
    public String getName()
    {
      return "Muriel";
    }

    @Override
    public String getDescription()
    {
      return
          "Muriel attempts to choose new units to build based on the enemy force composition.\n" +
          "She knows basic unit tactics, but doesn't currently understand ranged attacks.";
    }
  }
  public static final AIMaker info = new instantiator();
  
  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }
  
  private Queue<GameAction> queuedActions = new ArrayDeque<GameAction>();
  private class ActionWithValue {
    GameAction action; Double value;
    public ActionWithValue(GameAction ga, Double val) {action = ga; value = val;}
  }
  private class ObstructedUnitActions extends HashMap<Unit, ActionWithValue>{
    private static final long serialVersionUID = 1L;
    public ActionWithValue put(Unit u, GameAction ga, Double value) {
      return put(u, new ActionWithValue(ga, value));
    }
  }
  private ObstructedUnitActions obstructedActions = new ObstructedUnitActions();

  private UnitOrchestrator unitSelector = new UnitOrchestrator();

  private Army myArmy = null;

  private StringBuffer logger = new StringBuffer();
  private boolean shouldLog = true;
  private int turnNum = 0;
  
  private UnitEffectivenessMap myUnitEffectMap;
  private final double COST_EFFECTIVENESS_MIN = 0.75;
  private final double COST_EFFECTIVENESS_HIGH = 1.25;
  private final double INFANTRY_PROPORTION = 0.5;

  private ArrayList<XYCoord> nonAlliedProperties; // set from AIUtils.

  public Muriel(Army co)
  {
    myArmy = co;
  }

  private void init(Army[] allArmies)
  {
    // Initialize UnitModel collections.
    Collection<ModelForCO> myUnitModels = ModelForCO.getListFor(myArmy);

    ArrayList<Commander> enemyCos = new ArrayList<>();
    Map<Commander, Collection<ModelForCO>> otherUnitModels = new HashMap<Commander, Collection<ModelForCO>>();
    for( Army other : allArmies )
    {
      if( myArmy.isEnemy(other) )
      {
        for( Commander co : other.cos )
        {
          enemyCos.add(co);
          otherUnitModels.put(co, ModelForCO.getListFor(co));
        }
      }
    }

    // Figure out unit matchups.
    myUnitEffectMap = new UnitEffectivenessMap();
    for( ModelForCO myModel : myUnitModels )
    {
      for( Commander oCo : enemyCos )
      {
        for( ModelForCO otherModel : otherUnitModels.get(oCo) )
        {
          getUnitMatchupInfo(myModel, otherModel); // Calculates the matchup and adds it to myUnitEffectMap.
        }
      }
    }
  }

  /**
   * Returns the UnitMatchupAndMetaInfo for this unit pair, calculating it first if needed.
   */
  private UnitMatchupAndMetaInfo getUnitMatchupInfo(ModelForCO myModel, ModelForCO otherModel)
  {
    Unit myUnit = new Unit(myModel.co, myModel.um);
    myUnit.x = 0;
    myUnit.y = 0;
    Unit otherUnit = new Unit( otherModel.co, otherModel.um );
    otherUnit.x = 0;
    otherUnit.y = 0;
    return getUnitMatchupInfo(myUnit, otherUnit);
  }

  /**
   * Returns the UnitMatchupAndMetaInfo for this unit pair, calculating it first if needed.
   */
  private UnitMatchupAndMetaInfo getUnitMatchupInfo(Unit myUnit, Unit otherUnit)
  {
    ModelForCO myModel = new ModelForCO(myUnit);
    ModelForCO otherModel = new ModelForCO(otherUnit);
    UnitMatchupAndMetaInfo umami = myUnitEffectMap.get(new UnitModelPair(myModel, otherModel));
    if( null != umami ) return umami;

    double myDamage = CombatEngine.calculateOneStrikeDamage(myUnit, 1, otherUnit, myArmy.myView, 0, myUnit.model.hasMobileWeapon());

    // Now go the other way.
    double otherDamage = CombatEngine.calculateOneStrikeDamage(otherUnit, 1, myUnit, myArmy.myView, 0, false);

    // Calculate and store the damage and cost-effectiveness ratios.
    double damageRatio = 0;
    double invRatio = 0;
    if( myDamage != 0 && otherDamage != 0)
    {
      damageRatio = myDamage / otherDamage;
      invRatio = 1/damageRatio;
    }
    if( myDamage == 0 ) damageRatio = 0;
    if( otherDamage == 0 ) invRatio = 0;
    if( myDamage != 0 && otherDamage == 0 ) damageRatio = 10000;
    if( myDamage == 0 && otherDamage != 0 ) invRatio = 10000;
    double costRatio = damageRatio * ((double)otherUnit.getCost() / myUnit.getCost());
    double otherCostRatio = invRatio * ((double)myUnit.getCost() / otherUnit.getCost());
    myUnitEffectMap.put(new UnitModelPair(myModel, otherModel), new UnitMatchupAndMetaInfo(damageRatio, costRatio));
    myUnitEffectMap.put(new UnitModelPair(otherModel, myModel), new UnitMatchupAndMetaInfo(invRatio, otherCostRatio));

    log(String.format("Adding matchup: %s vs %s: %s/%s, damageRatio: %s, costRatio: %s", myUnit, otherUnit, myDamage, otherDamage, damageRatio, costRatio));
    return myUnitEffectMap.get(new UnitModelPair(myModel, otherModel));
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    if (null == myUnitEffectMap)
      init(gameMap.game.armies);
    turnNum++;
    log(String.format("[======== Muriel initializing turn %s for %s =========]", turnNum, myArmy));

    // Make a list of properties we want to claim.
    nonAlliedProperties = AIUtils.findNonAlliedProperties(myArmy, gameMap);

    // Order the units by cost.
    final ArrayList<Unit> armyUnits = myArmy.getUnits();
    Collections.sort(armyUnits, (Unit u1, Unit u2) ->
      (int)(u2.getCost()*u2.getHP() - u1.getCost()*u1.getHP()));

    // If we are already capturing any of these properties, remove them from the list.
    for( Unit unit : armyUnits )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        nonAlliedProperties.remove(unit.getCaptureTargetCoords());
      }
    }
    unitSelector.reinit(armyUnits);

    // Check for a turn-kickoff power
    CommanderAbility ability = AIUtils.queueCromulentAbility(queuedActions, myArmy, CommanderAbility.PHASE_TURN_START);
    if( null != ability )
    {
      log("Activating " + ability);
    }
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== Muriel ending turn %s for %s =========]", turnNum, myArmy));
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    if( shouldLog )
    {
      System.out.println(message);
      logger.append(message).append('\n');
    }
  }
  @Override
  public void setLogging(boolean value) { shouldLog = value; }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    // If we have actions ready to go, don't bother calculating anything.
    if( !queuedActions.isEmpty() )
    {
      GameAction action = queuedActions.poll();
      log(String.format("  Action: %s", action));
      return action;
    }

    // Make sure we perform all important actions before giving actions to the "on hold" units.
    while( queuedActions.isEmpty() && !unitSelector.isEmpty() )
    {
      Unit unit = unitSelector.next();

      log("Considering " + unit.toStringWithLocation());
      if( unit.isTurnOver || !gameMap.isLocationValid(unit.x, unit.y))
      {
        log("  Cannot move; off-map or already moved.");
        unitSelector.remove(unit);
        continue; // No actions for units that are stale or out of bounds
      }

      // A unit may defer action if it hasn't deferred yet, and isn't at the top of unitsInTheWay. Otherwise it must
      // select an action (or push another unit into the unitsInTheWay stack, then the new unit must move first).
      boolean allowDeferring = unitSelector.mayDeferAction(unit);

      // Try to get an action. The unit may indicate another unit is in
      // the way by adding the other unit to `unitsInTheWay`.
      boolean actionQueued = queueUnitAction(gameMap, unit, allowDeferring);

      // If we found an action for this guy, Remove him from further consideration.
      if( actionQueued )
      {
        unitSelector.remove(unit);
        obstructedActions.remove(unit);
      }
    }

    // Check for an available buying enhancement power
    if( queuedActions.isEmpty() )
    {
      CommanderAbility ability = AIUtils.queueCromulentAbility(queuedActions, myArmy, CommanderAbility.PHASE_BUY);
      if( null != ability )
      {
        log("Activating " + ability);
      }
    }

    // If we don't have anything else to do, build units.
    if( queuedActions.isEmpty() )
    {
      queueUnitProductionActions(gameMap);
    }

    // Check for a turn-ending power
    if( queuedActions.isEmpty() )
    {
      CommanderAbility ability = AIUtils.queueCromulentAbility(queuedActions, myArmy, CommanderAbility.PHASE_TURN_END);
      if( null != ability )
      {
        log("Activating " + ability);
      }
    }

    GameAction action = queuedActions.poll();
    log(String.format("  Action: %s", action));
    return action;
  }
  private void displaceUnit(GameMap gameMap, Unit actor, GameAction desiredAction, double actionValue, Unit obstacle)
  {
    log(actor.toStringWithLocation() + " wants to do action " + desiredAction +
        ", but " + obstacle.toStringWithLocation() + " is in the way. Telling it to move.");

    // Flag the actor as an obstacle so the unit it's pushing can't push back.
    if( !unitSelector.isObstacle(actor) )
      unitSelector.flagObstacle(actor);
    obstructedActions.put(actor, desiredAction, actionValue);

    // Give the obstacle priority so it has to move first.
    unitSelector.flagObstacle(obstacle);
  }
  /** Compare input action against known blocked actions doable from that space. Return true if it is better
   * than any of those, and false if an existing known action should take priority. */
  public boolean isBestCurrentAction(Unit actor, GameAction action, double value)
  {
    boolean shouldQueue = true;
    for( Unit ou : obstructedActions.keySet() )
    {
      GameAction oa = obstructedActions.get(ou).action;
      if( ou != actor // Don't worry about replacing our own previously-considered actions.
          && oa.getMoveLocation().equals(action.getMoveLocation()) // Only one unit can move here.
          && action.getType().equals(oa.getType()) ) // For now just break ties between like types.
      {
        // If we find any action better than ga, then we don't want to do ga.
        double ov = obstructedActions.get(ou).value;
        log("  Two units want the same space:\n    "
            + oa.toString() + "(" + ov + ")\n    " + action.toString() + "(" + value + ")");
        if( ov > value )
          return false;
      }
    }
    return shouldQueue;
  }
  private boolean queueUnitAction(GameMap gameMap, Unit unit, boolean allowDeferring)
  {
    // If we are capturing something, finish what we started.
    if( unit.getCaptureProgress() > 0 )
    {
      log(String.format("%s is currently capturing; continue", unit.toStringWithLocation()));
      queuedActions.add( new CaptureLifecycle.CaptureAction(gameMap, unit, GamePath.stayPut(unit)) );
      return true;
    }

    //////////////////////////////////////////////////////////////////
    // If we are currently healing, stick around, unless that would stem the tide of reinforcements.
    MapLocation loc = gameMap.getLocation(unit.x, unit.y);
    if( (unit.getHP() <= 8) && unit.model.canRepairOn(loc) && (loc.getEnvironment().terrainType != TerrainType.FACTORY) && (loc.getOwner() == unit.CO) )
    {
      log(String.format("%s is damaged and on a repair tile. Will continue to repair for now.", unit.toStringWithLocation()));
      ArrayList<GameActionSet> actionSet = unit.getPossibleActions(gameMap, GamePath.stayPut(unit));
      for( GameActionSet set : actionSet )
      {
        // Go ahead and attack someone as long as we don't have to move.
        if( set.getSelected().getType() == UnitActionFactory.ATTACK )
        {
          for( GameAction action : set.getGameActions() )
          {
            Unit other = gameMap.getLocation(action.getTargetLocation()).getResident();
            if( null == other ) continue; // Don't bother with terrain.
            if( shouldAttack(unit, other, gameMap) )
            {
              log(String.format("  May as well try to shoot %s since I'm here anyway", other));
              queuedActions.add(action);
              return true;
            }
          }
        }
      }
      // We didn't find someone adjacent to smash, so just sit tight for now.
      queuedActions.add( new WaitLifecycle.WaitAction(unit, GamePath.stayPut(unit)) );
      return true;
    } // ~Continue repairing if in a depot.

    //////////////////////////////////////////////////////////////////
    // Figure out if we should go resupply.
    boolean shouldResupply = false;
    // If we are low on fuel.
    if( unit.fuel < (unit.model.maxFuel/4.0) )
    {
      log(String.format("%s is low on fuel.", unit.toStringWithLocation()));
      shouldResupply = true;
    }
    // If we are low on HP, go heal.
    if( unit.getHP() < 6 ) // Arbitrary threshold
    {
      log(String.format("%s is damaged (%s HP).", unit.toStringWithLocation(), unit.getHP()));
      shouldResupply = true;
    }
    // If we are out of ammo.
    if( unit.ammo == 0 )
    {
      log(String.format("%s is out of ammo.", unit.toStringWithLocation()));
      shouldResupply = true;
    }

    if( shouldResupply )
    {
      ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
      XYCoord unitCoords = new XYCoord(unit.x, unit.y);
      Utils.sortLocationsByDistance(unitCoords, stations);
      for( XYCoord coord : stations )
      {
        MapLocation station = gameMap.getLocation(coord);
        // Go to the nearest unoccupied friendly space, but don't gum up the production lines.
        if( station.getResident() == null && (station.getEnvironment().terrainType != TerrainType.FACTORY) )
        {
          // Plot a course towards a repair station, but only apply the action if it moves us.
          // If a unit is stuck on the front lines and can't get away past reinforcements, just gotta knuckle up.
          GameAction goHome = AIUtils.moveTowardLocation(unit, coord, gameMap);
          if( (null != goHome) && !goHome.getMoveLocation().equals(unitCoords) )
          {
            log(String.format("  Heading towards %s to resupply", coord));
            queuedActions.add(goHome);
            return true;
          }
          else
          {
            log(String.format("  Can't find a way to move towards resupply station at %s", coord));
          }
        }
      }
      log("  Cannot find an available resupply station.");
    }

    // Find all the things we can do from here, entertaining moves that require displacing other units.
    boolean includeOccupiedDestinations = true;
    Map<UnitActionFactory, ArrayList<GameAction> > unitActionsByType = AIUtils.getAvailableUnitActionsByType(unit, gameMap, includeOccupiedDestinations);

    //////////////////////////////////////////////////////////////////
    // Look for advantageous attack actions.
    ArrayList<GameAction> attackActions = unitActionsByType.get(UnitActionFactory.ATTACK);
    GameAction maxCarnageAction = null;
    double maxDamageValue = 0;
    if( null != attackActions && !attackActions.isEmpty() )
    {
      for( GameAction action : attackActions )
      {
        // If another of our units is in the way and has already moved, then we can't consider this attack action.
        if( !gameMap.isLocationEmpty(action.getMoveLocation()) && gameMap.getResident(action.getMoveLocation()).isTurnOver )
        {
          continue;
        }
        Unit unitInTheWay = gameMap.getResident(action.getMoveLocation()); // Could be us, could be nobody.

        // Sift through all attack actions we can perform.
        double damageValue = AICombatUtils.scoreAttackAction(unit, action, gameMap,
            (results) -> {
              double hpDamage = Math.min(results.defender.getPreciseHPDamage(), results.defender.unit.getPreciseHP());

              if( shouldAttack(unit, results.defender.unit, gameMap) )
                return (results.defender.unit.getCost() / 10) * hpDamage;

              return 0.;
            }, (terrain, params) -> 0.); // Don't mess with terrain

        // Find the attack that causes the most monetary damage, provided it's at least a halfway decent idea.
        if( (damageValue > maxDamageValue) )
        {
          // If this action would require displacing a unit, and that unit is already flagged as needing to be
          // displaced, then we are in ITS way, and cannot tell it to move, and cannot perform this attack.
          if( unit != unitInTheWay && unitSelector.isObstacle(unitInTheWay) )
            continue;
          // If this unit action would prevent another one, and isn't better than
          // the other one, then don't do this action.
          if( !isBestCurrentAction(unit, action, damageValue))
            continue;

          maxDamageValue = damageValue;
          maxCarnageAction = action;
        }
      }
      if( maxCarnageAction != null )
      {
        // If this attack is valid now, do it. Otherwise, see if we can free up the space.
        XYCoord dest = maxCarnageAction.getMoveLocation();
        if( gameMap.isLocationEmpty(unit, dest) )
        {
          queuedActions.add(maxCarnageAction);
          return true;
        }
        else // Don't check the obstacle's isTurnOver here; that should be handled in the GameAction loop above.
        {
          displaceUnit(gameMap, unit, maxCarnageAction, maxDamageValue, gameMap.getResident(dest));
          return false;
        }
      }
    }

    //////////////////////////////////////////////////////////////////
    // See if there's something to capture (but only if we are moderately healthy).
    if( unit.getHP() > 7 )
    {
      ArrayList<GameAction> captureActions = unitActionsByType.get(UnitActionFactory.CAPTURE);
      if( null != captureActions && !captureActions.isEmpty() )
        for( GameAction capture : captureActions )
        {
          XYCoord dest = capture.getMoveLocation();
          Unit obst = gameMap.getResident(dest);
          if( null == obst || obst == unit )
          {
            // If this unit action would prevent another one, and isn't better than
            // the other one, then don't do this action.
            if( !isBestCurrentAction(unit, capture, unit.getHP()))
              continue;

            queuedActions.add(capture);
            return true;
          }
          else if( obst.isTurnOver || unitSelector.isObstacle(obst) )
            continue; // We can't displace the obstacle unit; find something else to do.
          else
          {
            displaceUnit(gameMap, unit, capture, unit.getHP(), obst);
            return false;
          }
        }
    }

    //////////////////////////////////////////////////////////////////
    // Tabulate our production facilities so we can avoid stepping on them later.
    HashSet<XYCoord> destinationsToAvoid = new HashSet<XYCoord>();
    for( XYCoord xyl : myArmy.getOwnedProperties() )
      if(gameMap.getEnvironment(xyl).terrainType == TerrainType.FACTORY)
        destinationsToAvoid.add(xyl);

    // If someone else wants us out of the way, go ahead and oblige since we haven't found something better to do.
    if( unitSelector.isObstacle(unit) )
    {
      // Since we flag a unit as an obstacle if it is obstructed just to prevent recursive blocking,
      // we can remove this unit from the list if it's the only one there (actual blocks are already removed.
      if( unitSelector.numObstacles() == 1 )
      {
        log(unit.toStringWithLocation() + " is not actually an obstacle.");
        unitSelector.remove(unit);
      }
      else
      {
        log(unit.toStringWithLocation() + " is in the way, and must move");
        destinationsToAvoid.add(new XYCoord(unit.x, unit.y));
      }
    }

    //////////////////////////////////////////////////////////////////
    // We didn't find an immediate ATTACK or CAPTURE action we can do.
    // Things that can capture; go find something to capture, if you are moderately healthy.
    if( unit.hasActionType(UnitActionFactory.CAPTURE) && (unit.getHP() >= 7) )
    {
      log(String.format("Seeking capture target for %s", unit.toStringWithLocation()));
      XYCoord unitCoords = new XYCoord(unit.x, unit.y);
      Utils.sortLocationsByDistance(unitCoords, nonAlliedProperties);
      for(int i = 0; i < nonAlliedProperties.size(); ++i)
      {
        XYCoord coord = nonAlliedProperties.get(i);

        ArrayList<GameAction> possibleMoves = unitActionsByType.get(UnitActionFactory.WAIT);
        AIUtils.DistanceFromLocationComparator dflc = new AIUtils.DistanceFromLocationComparator(coord);
        Collections.sort(possibleMoves, dflc);

        for( GameAction ga : possibleMoves )
        {
          if(destinationsToAvoid.contains( ga.getMoveLocation() ))
            continue;

          // Check if there is a friendly in our way.
          Unit obstacle = gameMap.getResident(ga.getMoveLocation());
          if( null == obstacle || obstacle == unit )
          {
            queuedActions.add(ga);
            return true;
          }
          else if( obstacle.isTurnOver || unitSelector.isObstacle(obstacle) )
            continue; // We can't displace the obstacle unit; need a different destination.
          else
          {
            displaceUnit(gameMap, unit, ga, unit.getHP(), obstacle);
            return false;
          }
        }
      }
    }

    //////////////////////////////////////////////////////////////////
    // Everyone else, go hunting.
    if( queuedActions.isEmpty() && unit.hasActionType(UnitActionFactory.ATTACK) )
    {
      log(String.format("Seeking attack target for %s", unit.toStringWithLocation()));
      ArrayList<XYCoord> enemyLocations = AIUtils.findEnemyUnits(myArmy, gameMap); // Get enemy locations.
      Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), enemyLocations); // Sort them by accessibility.

      for(int i = 0; i < enemyLocations.size(); ++i)
      {
        XYCoord coord = enemyLocations.get(i);
        Unit target = gameMap.getLocation(coord).getResident();

        if( !unit.canTarget(target.model) ) continue; // Make sure we can attack this type; also accounts for ammo.

        // Only chase this unit if we will be effective against it. Don't check shouldAttack here, because we can't actually attack yet.
        UnitMatchupAndMetaInfo umami = getUnitMatchupInfo(unit, target);
        if( umami.costEffectivenessRatio < COST_EFFECTIVENESS_MIN ) continue;

        // Find locations that would be dangerous for us so we can avoid sauntering into enemy fire.
        HashSet<XYCoord> noGoZone = new HashSet<XYCoord>();
        final int MAX_RELEVANT_DISTANCE = 27; // ~3x the move distance of the fastest units. Up for tweaking.
        for(int j = 0; j < enemyLocations.size(); ++j)
        {
          XYCoord threatCoord = enemyLocations.get(j);
          Unit threat = gameMap.getLocation(threatCoord).getResident();
          XYCoord unitCoord = new XYCoord(unit.x, unit.y);
          if( unitCoord.getDistance(threatCoord) <= MAX_RELEVANT_DISTANCE )
          {
            // If we, in the enemy's place, would attack `unit` with `threat`, then we should not let them attack us.
            if( threat.canTarget(unit.model) && shouldAttack(threat, unit, gameMap) )
            {
              // Add coordinates that `threat` could target to our "no-go" list.
              Map<XYCoord, Double> threatMap = AICombatUtils.findThreatPower(gameMap, threat, unit.model);
              noGoZone.addAll(threatMap.keySet()); // Ignore the valueMap of the return; we have already decided `threat` is dangerous.
            }
          }
          else break; // Don't bother considering far-away baddies for our no-go zone.
        }

        // Try to move towards the enemy, but avoid blocking production.
        noGoZone.addAll(destinationsToAvoid);

        // Sort the possible move actions by distance from the target.
        ArrayList<GameAction> possibleMoves = unitActionsByType.get(UnitActionFactory.WAIT);
        AIUtils.DistanceFromLocationComparator dflc = new AIUtils.DistanceFromLocationComparator(coord);
        Collections.sort(possibleMoves, dflc);

        for( GameAction ga : possibleMoves )
        {
          if(noGoZone.contains( ga.getMoveLocation() ))
            continue;

          // Check if there is a friendly in our way.
          Unit obstacle = gameMap.getResident(ga.getMoveLocation());
          if( null == obstacle || obstacle == unit )
          {
            queuedActions.add(ga);
            return true;
          }
          else if( obstacle.isTurnOver || unitSelector.isObstacle(obstacle) )
            continue; // We can't displace the obstacle unit; need a different destination.
          else
          {
            displaceUnit(gameMap, unit, ga, 0, obstacle);
            return false;
          }
        }
      }
    }

    // Couldn't find any capture or attack actions. This unit is
    // either a transport, or stranded on an island somewhere.
    boolean moving = false;
    if( allowDeferring )
    {
      log(String.format("  Could not find an action for %s. Deferring action for now.", unit.toStringWithLocation()));
      unitSelector.defer(unit);
    }
    else
    {
      // Try to find a place to move, avoiding blocking production, and honoring requests for us to move.
      GameAction move = AIUtils.moveTowardLocation(unit, new XYCoord(unit.x, unit.y), gameMap, destinationsToAvoid);
      if( null == move )
      {
        // If no valid move was found, then don't.
        log(String.format("  Could not find an action for %s. Staying put.", unit.toStringWithLocation()));
        move = new WaitLifecycle.WaitAction(unit, GamePath.stayPut(unit));
      }
      queuedActions.add(move);
      moving = true;
    }
    return moving;
  }

  private boolean shouldAttack(Unit unit, Unit target, GameMap gameMap)
  {
    // Calculate the cost of the damage we can do.
    double damage = CombatEngine.calculateOneStrikeDamage(unit, 1, target, gameMap, gameMap.getEnvironment(target.x, target.y).terrainType.getDefLevel(), unit.model.hasMobileWeapon());

    UnitMatchupAndMetaInfo umami = getUnitMatchupInfo(unit, target);

    // This attack is a good idea if our cost effectiveness is in the acceptable range, or if we can at least half-kill them.
    // The second check is needed because one glass cannon may not have a great overall ratio against another; whoever hits first wins, e.g. Mech vs Anti-Air.
    return (umami.costEffectivenessRatio > COST_EFFECTIVENESS_MIN) || (damage > (target.getHP() / 2.0));
  }

  private void queueUnitProductionActions(GameMap gameMap)
  {
    int budget = myArmy.money;
    log("Evaluating Production needs");
    log("Budget: " + budget);

    // Figure out what unit types we can purchase with our available properties.
    boolean includeFriendlyOccupied = false;
    CommanderProductionInfo CPI = new CommanderProductionInfo(myArmy, gameMap, includeFriendlyOccupied);

    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return;
    }

    // Get a count of enemy forces.
    Map<Commander, ArrayList<Unit> > unitLists = AIUtils.getEnemyUnitsByCommander(myArmy, gameMap);
    Map<ModelForCO, Double> enemyUnitCounts = new HashMap<>();
    for( Commander co : unitLists.keySet() )
    {
      for( Unit u : unitLists.get(co) )
      {
        if( !u.model.hasDirectFireWeapon() ) continue; // Only handle direct-fire units for now.
        ModelForCO coModel = new ModelForCO(u.CO, u.model);
        // Count how many of each model of enemy units are in play.
        if( enemyUnitCounts.containsKey(coModel))
        {
          enemyUnitCounts.put(coModel, enemyUnitCounts.get(coModel) + (u.getHP() / 10.0) );
        }
        else
        {
          enemyUnitCounts.put(coModel, u.getHP() / 10.0 );
        }
      }
    }
    // Count up my own army men.
    Map<ModelForCO, Double> myUnitCounts = new HashMap<>();
    for( Unit u : myArmy.getUnits() )
    {
      if( !u.model.hasDirectFireWeapon() ) continue; // Only handle direct-fire units for now.
      ModelForCO coModel = new ModelForCO(u.CO, u.model);
      // Count how many of each model I have.
      if( myUnitCounts.containsKey(coModel))
      {
        myUnitCounts.put(coModel, myUnitCounts.get(coModel) + (u.getHP() / 10.0) );
      }
      else
      {
        myUnitCounts.put(coModel, u.getHP() / 10.0 );
      }
    }
    log("My Forces:");
    for( ModelForCO um : myUnitCounts.keySet() )
    {
      log(String.format("  %sx%s", um, myUnitCounts.get(um)));
    }
    log("Enemy Forces:");
    for( ModelForCO um : enemyUnitCounts.keySet() )
    {
      log(String.format("  %sx%s", um, enemyUnitCounts.get(um)));
    }
    double enemyArmyHP = 0; // Count up the total size of the enemy forces.
    for( Commander key : unitLists.keySet() )
    {
      for( Unit u : unitLists.get(key) ) enemyArmyHP += u.getHP();
    }

    // Build a map of how threatened I am by each enemy unit type.
    // Larger values will represent a greater threat.
    Queue<ModelValuePair> enemyUnitStrengths = new PriorityQueue<ModelValuePair>();
    for( ModelForCO em : enemyUnitCounts.keySet() )
    {
      double effectiveThreat = enemyUnitCounts.get(em); // Start with how many of them there are.
      for( ModelForCO um : myUnitCounts.keySet() )
      {
        double myCount = myUnitCounts.get(um);
        UnitMatchupAndMetaInfo umami = getUnitMatchupInfo(um,  em);
        double myStoppingPower = umami.damageRatio * myCount; // I can stop THIS MANY of those things with what I have.
        effectiveThreat -= myStoppingPower; // Subtract my effective weight with this type from their number.
      }
      enemyUnitStrengths.offer(new ModelValuePair(em, effectiveThreat)); // If effectiveThreat is still positive, I can't handle all of them.
    }

    // Try to purchase units that will counter the enemies I am least equipped to fight.
    // We should place one order per iteration of this loop.
    ArrayList<PurchaseOrder> shoppingCart = new ArrayList<PurchaseOrder>();
    boolean orderedSomething = true;
    while( (budget > 0) && (enemyUnitCounts.size() > 0) && !CPI.availableUnitModels.isEmpty() && orderedSomething )
    {
      orderedSomething = false; // If we fail to find something to build, don't keep trying forever.

      // Sort enemy units by the effective threat they provide to our current forces, and build counters for the most dangerous first.
      log("Threat ratings:");
      Iterator<ModelValuePair> enemyTypeIter = enemyUnitStrengths.iterator();
      while( enemyTypeIter.hasNext() )
      {
        ModelValuePair enemyMVP = enemyTypeIter.next();
        log(String.format("  %s: %s", enemyMVP.model, enemyMVP.value));
      }

      // Grab the first enemy unit type, and try to build something that will counter it.
      ModelForCO enemyToCounter = enemyUnitStrengths.peek().model;
      log("Want to counter " + enemyToCounter);
      log(String.format("  Remaining budget: %s", budget));

      // If we have a lot of cash on hand, don't worry about cost effectiveness - just maximize damage instead.
      // If we ever collect more than twice our income in funds, we just aren't spending fast enough. Fix that.
      int incomePerTurn = myArmy.getIncomePerTurn();
      boolean useDamageRatio = (myArmy.money > (incomePerTurn*2)); // Rich people can afford to think differently.
      if(useDamageRatio) log("  High funds - sorting units by damage ratio instead of cost effectiveness.");

      // If we are low on grunts, make sure we save money to build more.
      final UnitModel infModel = myArmy.cos[0].getUnitModel(UnitModel.TROOP);
      int infCount = 0;
      for( ModelForCO unitType : myUnitCounts.keySet() )
        if( infModel == unitType.um )
          infCount += myUnitCounts.get(unitType);

      int costBuffer = 0;
      if( (infCount < (myArmy.getUnits().size() * INFANTRY_PROPORTION)) )
      {
        int gruntsWanted = (int)Math.ceil(myArmy.getUnits().size() * INFANTRY_PROPORTION);
        int gruntFacilities = CPI.getNumFacilitiesFor(infModel)-1; // The -1 assumes we are about to build from a factory. Possibly untrue.
        if( gruntFacilities < 0 ) gruntFacilities = 0;
        costBuffer = (int)Math.min(gruntFacilities, gruntsWanted) * CPI.getAverageCostFor(infModel);
        log(String.format("  Low on Infantry: witholding %s for possible extra grunts", costBuffer));
      }

      // Make a list of possible counters: types with a good cost effectiveness vs enemyToCounter.
      log("  Viable counters:");
      HashSet<ModelForCO> counters = new HashSet<>();
      for( ModelForCO counter : CPI.availableUnitModels )
      {
        if( myArmy.money < counter.co.getCost(counter.um) ) continue; // If we can't afford it, don't bother.
        UnitMatchupAndMetaInfo umami = getUnitMatchupInfo(counter, enemyToCounter);
        if( umami.costEffectivenessRatio >= COST_EFFECTIVENESS_MIN )
        {
          log(String.format("    %s has cost ratio %s", counter, umami.costEffectivenessRatio));
          counters.add(counter);
        }
      }

      if( counters.isEmpty() )
      {
        log("  No suitable counters identified.");
        enemyUnitStrengths.poll();
        continue; // We can't build anything useful. Bah, humbug.
      }

      // Sort the possible counters by how good they are against the enemy force composition in
      // general; we want units that are good against more than just enemyToCounter, if possible.
      log("  Initial scoring:");
      Queue<ModelValuePair> counterScores = new PriorityQueue<ModelValuePair>(counters.size());
      for(ModelForCO counter : counters)
      {
        int score = 0;
        for( Commander enemyCo : unitLists.keySet() )
        {
          for( Unit enemyUnit : unitLists.get(enemyCo) )
          {
            UnitMatchupAndMetaInfo umami = getUnitMatchupInfo(counter, new ModelForCO(enemyUnit.CO, enemyUnit.model));
            if( useDamageRatio )
            {
              if( umami.damageRatio >= 1.0 ) score++; // Plus one if it's worth building.
              if( umami.damageRatio >= 1.5 ) score++; // An extra bump in score if they are very good vs this type.
              if( umami.damageRatio < 0.8 ) score--; // Discount if my counter is countered.
            }
            else
            {
              if( umami.costEffectivenessRatio >= COST_EFFECTIVENESS_MIN ) score++; // Plus one if it's worth building.
              if( umami.costEffectivenessRatio >= COST_EFFECTIVENESS_HIGH ) score++; // An extra bump in score if they are very good vs this type.
              if( umami.costEffectivenessRatio < COST_EFFECTIVENESS_MIN ) score--; // Discount if my counter is countered.
            }
          }
        }
        log(String.format("    %s has counter score %s", counter, score));
        counterScores.offer(new ModelValuePair(counter, score));
      }

      // Loop through my counters for enemyToCounter, in order of how generally applicable they are.
      // This second pass will allow us to break any ties and populate orderedCounters.
      ArrayList<ModelForCO> orderedCounters = new ArrayList<>();
      while( !counterScores.isEmpty() )
      {
        // Collect all units tied with the highest counter score (of those still in counterScores).
        // If equalCounters ends up with more than one entry, then each model it contains can counter
        // the same proportion of the enemy force, to some extent. Below we distinguish by "how well".
        HashSet<ModelForCO> equalCounters = new HashSet<>();
        ModelValuePair bestCounter = counterScores.poll();
        equalCounters.add(bestCounter.model);
        while( !counterScores.isEmpty() && (bestCounter.value == counterScores.peek().value) ) equalCounters.add(counterScores.poll().model);

        // Sort equalCounters into counterScoresFine, based on a weighted goodness metric.
        // If equalCounters has only one entry, there's really no reason to calculate the goodness metric.
        if( equalCounters.size() > 1 )
        {
          log("  Breaking ties");
          Queue<ModelValuePair> counterScoresFine = new PriorityQueue<ModelValuePair>(counters.size());
          for( ModelForCO counter : equalCounters )
          {
            // Overall goodness of each option is it's effectiveness vs each enemy unit type, times the density of that enemy type.
            // This lets us make fine distinctions between units that are equally applicable, broadly speaking.
            //log(String.format("    Evaluating %s", counter));
            double goodness = 0;
            for( ModelForCO enemy : enemyUnitCounts.keySet() )
            {
              UnitMatchupAndMetaInfo umami = getUnitMatchupInfo(counter, enemy);
              double percent = (enemyUnitCounts.get(enemy)*10) / enemyArmyHP;
              double thisGoodness = (useDamageRatio) ? (umami.damageRatio * percent) : (umami.costEffectivenessRatio * percent);
              //log(String.format("      goodness vs %s: %s (%s * %s)", enemy, thisGoodness, umami.costEffectivenessRatio, percent));
              goodness += thisGoodness;
            }
            log(String.format("    %s has weighted goodness %s", counter, goodness));

            counterScoresFine.offer(new ModelValuePair(counter, goodness));
          }

          // Unload our finely-evaluated counters into a list.
          while(!counterScoresFine.isEmpty())
          {
            log(String.format("  Adding %s to shopping list", counterScoresFine.peek().model));
            orderedCounters.add(counterScoresFine.poll().model);
          }
        }
        else
        {
          log(String.format("  Adding %s to shopping list", bestCounter.model));
          orderedCounters.add(bestCounter.model);
        }
      }

      // Go through the list and see what we can build, in order.
      Iterator<ModelForCO> modelIter = orderedCounters.iterator();
      while( modelIter.hasNext() )
      {
        ModelForCO idealCounter = modelIter.next();
        log(String.format("  Would like to build %s", idealCounter));

        // Figure out if we can afford the desired unit type.
        int maxBuildable = CPI.getNumFacilitiesFor(idealCounter);
        log(String.format("    Facilities available: %s", maxBuildable));
        MapLocation loc = CPI.getLocationToBuild(idealCounter);
        int cost = myArmy.getBuyCost(idealCounter.um, loc.getCoordinates());
        if( cost <= (budget - costBuffer))
        {
          // Go place orders.
          log(String.format("    I can build a %s for a cost of %s", idealCounter, cost));
          shoppingCart.add(new PurchaseOrder(loc, loc.getOwner(), idealCounter.um));
          budget -= cost;
          CPI.removeBuildLocation(loc);
          orderedSomething = true;

          // We found something useful to build; update our estimate of how well we match up.
          Iterator<ModelValuePair> eusIter = enemyUnitStrengths.iterator();
          while(eusIter.hasNext())
          {
            ModelValuePair enemyStrength = eusIter.next();
            UnitMatchupAndMetaInfo matchup = getUnitMatchupInfo(idealCounter, enemyStrength.model);
            enemyStrength.value = enemyStrength.value - matchup.damageRatio; // Subtract this unit's strength from theirs.
          }
          break; // Loop around, re-sort the enemies by strength, and figure out what to build next.
        }
        else {log(String.format("    %s cost %s, I have %s (witholding %s).", idealCounter, cost, budget, costBuffer));}
      } // ~while( !availableUnitModels.isEmpty() )
    } // ~while( still choosing units to build )

    // Build infantry from any remaining facilities.
    UnitModel infModel = myArmy.cos[0].getUnitModel(UnitModel.TROOP);
    MapLocation loc = CPI.getLocationToBuild(infModel);
    int infCost = Integer.MAX_VALUE;
    if( null != loc && null != loc.getOwner() )
      infCost = loc.getOwner().getCost(infModel);
    while ((budget >= infCost) &&
        null != loc && (CPI.availableUnitModels.contains(new ModelForCO(loc.getOwner(), infModel))))
    {
      shoppingCart.add(new PurchaseOrder(loc, loc.getOwner(), infModel));
      budget -= infCost;
      CPI.removeBuildLocation(loc);
      // Reset values for the next iteration
      loc = CPI.getLocationToBuild(infModel);
      if( null != loc )
        infCost = loc.getOwner().getCost(infModel);
    }

    // Convert our PurchaseOrders into GameActions.
    for( PurchaseOrder order : shoppingCart )
    {
      queuedActions.offer(new GameAction.UnitProductionAction(order.co, order.model, order.location.getCoordinates()));
    }
  }

  private static class UnitOrchestrator
  {
    private Queue<Unit> unitsToMove = new ArrayDeque<Unit>(); // Units who haven't acted yet this turn.
    private Queue<Unit> unitsOnHold = new ArrayDeque<Unit>(); // Units whose actions have been deferred (because they would just WAIT)
    private Stack<Unit> unitsInTheWay = new Stack<Unit>(); // Units who are in the way and should move. These cannot defer action.

    public void reinit(Collection<Unit> unitsToManage)
    {
      unitsToMove.addAll(unitsToManage);
      unitsOnHold.clear();
      unitsInTheWay.clear();
    }
    public boolean isEmpty()
    {
      return unitsInTheWay.isEmpty() && unitsToMove.isEmpty() && unitsOnHold.isEmpty();
    }
    public Unit next()
    {
      Unit unit = null;
      // If units are flagged as in the way, move them first.
      if( !unitsInTheWay.isEmpty() )
        unit = unitsInTheWay.peek();
      else if( !unitsToMove.isEmpty() )
        unit = unitsToMove.peek();
      else if( !unitsOnHold.isEmpty() )
        unit = unitsOnHold.peek();
      return unit;
    }
    public void remove(Unit unit)
    {
      unitsInTheWay.remove(unit);
      unitsToMove.remove(unit);
      unitsOnHold.remove(unit);
    }
    public boolean mayDeferAction(Unit unit)
    {
      // A unit may defer action if it hasn't deferred yet, and isn't at the top of unitsInTheWay. Otherwise it must
      // select an action (or push another unit into the unitsInTheWay stack, then the new unit must move first).
      boolean allowDeferring = !unitsOnHold.contains(unit);
      if(allowDeferring && !unitsInTheWay.isEmpty() )
        allowDeferring = unit != unitsInTheWay.peek();
      return allowDeferring;
    }
    public boolean isObstacle(Unit unit)
    {
      return unitsInTheWay.contains(unit);
    }
    public int numObstacles()
    {
      return unitsInTheWay.size();
    }
    public void flagObstacle(Unit unit)
    {
      if( !isObstacle(unit) )
      {
        unitsInTheWay.push(unit);
      }
      else throw new RuntimeException("May not flag a unit (" + unit.toStringWithLocation() + ") that is already an obstacle!");
    }
    public void defer(Unit unit)
    {
      unitsToMove.remove(unit);
      unitsOnHold.add(unit);
    }
  }

  private static class PurchaseOrder implements Comparable<PurchaseOrder>
  {
    MapLocation location;
    Commander co;
    UnitModel model;

    public PurchaseOrder(MapLocation loc, Commander co, UnitModel um)
    {
      location = loc;
      this.co = co;
      model = um;
    }

    @Override
    public int compareTo(PurchaseOrder other)
    {
      return co.getBuyCost(model, location.getCoordinates()) - other.co.getBuyCost(other.model, location.getCoordinates());
    }
  }

  private static class ModelValuePair implements Comparable<ModelValuePair>
  {
    public ModelForCO model;
    public double value;
    public ModelValuePair(ModelForCO unitModel, double val)
    {
      model = unitModel;
      value = val;
    }

    @Override
    /** If this has a higher value, we want this to come before other. */
    public int compareTo(ModelValuePair other)
    {
      return (int)((other.value - value)*100);
    }
  }

  private static class UnitModelPair
  {
    public final ModelForCO first;
    public final ModelForCO second;
    public UnitModelPair(ModelForCO first, ModelForCO second)
    {
      this.first = first;
      this.second = second;
    }

    @Override
    public int hashCode()
    {
      final int prime = 160091;
      int result = 1;
      result = prime * result + first.hashCode();
      result = prime * result + second.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if( this == obj )
        return true;
      if( obj == null )
        return false;
      if( getClass() != obj.getClass() )
        return false;
      UnitModelPair other = (UnitModelPair) obj;
      if( first != other.first )
        return false;
      if( second != other.second )
        return false;
      return true;
    }
  }

  private static class UnitMatchupAndMetaInfo
  {
    public final double damageRatio;
    public final double costEffectivenessRatio;

    public UnitMatchupAndMetaInfo(double dmgRatio, double costRatio)
    {
      damageRatio = dmgRatio;
      costEffectivenessRatio = costRatio;
    }
  }

  /** Stores an object with info about how well UnitModelPair.first fares against UnitModelPair.second on average. */
  private class UnitEffectivenessMap extends HashMap<UnitModelPair, UnitMatchupAndMetaInfo>
  {private static final long serialVersionUID = 1L;}
}
