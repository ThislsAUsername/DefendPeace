package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameAction.ActionType;
import Engine.GameActionSet;
import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.CombatEngine;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;
import Units.Weapons.WeaponModel;

/**
 *  Wally values units based on firepower and the area they can threaten.
 *  He tries to keep units safe by keeping them out of range, but will also meatshield to protect more valuable units.
 */
public class WallyAI implements AIController
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Commander co)
    {
      return new WallyAI(co);
    }

    @Override
    public String getName()
    {
      return "Wally";
    }
  }
  public static final AIMaker info = new instantiator();
  
  Queue<GameAction> actions = new ArrayDeque<GameAction>();
  // sort our units by expense first, we want them to hit first
  Queue<Unit> unitQueue = new PriorityQueue<Unit>(11, new AIUtils.UnitCostComparator(false));
  boolean stateChange;

  private Commander myCo = null;

  // % damage dealable to feel "threatened"
  private static final int INDIRECT_THREAT_THRESHHOLD = 0;
  private static final int DIRECT_THREAT_THRESHHOLD = 13;
  private static final double AGGRO_FUNDS_WEIGHT = 1.5;
  private static final double RANGE_WEIGHT = 0.07;

  private ArrayList<XYCoord> unownedProperties;
  private ArrayList<XYCoord> capturingProperties;

  private StringBuffer logger = new StringBuffer();
  private int turnNum = 0;
  private int mobileAttackRange = 0;

  public WallyAI(Commander co)
  {
    myCo = co;
    for( UnitModel um : myCo.unitModels )
    {
      for( WeaponModel wm : um.weaponModels )
      {
        if( wm.canFireAfterMoving )
          mobileAttackRange = Math.max(mobileAttackRange, wm.maxRange);
      }
    }
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    turnNum++;
    log(String.format("[======== Wally initializing turn %s for %s =========]", turnNum, myCo));

    // Make sure we don't have any hang-ons from last time.
    actions.clear();

    // Create a list of every property we don't own, but want to.
    unownedProperties = new ArrayList<XYCoord>();
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        XYCoord loc = new XYCoord(x, y);
        if( gameMap.getLocation(loc).isCaptureable() && myCo.isEnemy(gameMap.getLocation(loc).getOwner()) )
        {
          unownedProperties.add(loc);
        }
      }
    }
    capturingProperties = new ArrayList<XYCoord>();
    for( Unit unit : myCo.units )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        capturingProperties.add(unit.getCaptureTargetCoords());
      }
    }

    // Check for a turn-kickoff power
    AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_TURN_START);
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== Wally ending turn %s for %s =========]", turnNum, myCo));
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    System.out.println(message);
    logger.append(message).append('\n');
  }

  @Override
  /**
   * Our to-do list for a given turn:
   * 1) siege attacks
   * 2) try for any mobile kills
   * 3) capture if needed
   * 4) travel
   * 5) build
   */
  public GameAction getNextAction(GameMap gameMap)
  {
    GameAction nextAction = null;
    do
    {
      // If we have more actions ready, don't bother calculating stuff.
      if( !actions.isEmpty() )
      {
        GameAction action = actions.poll();
        log("  Action: " + action);
        return action;
      }
      else if( unitQueue.isEmpty() )
      {
        stateChange = false; // There's been no gamestate change since we last iterated through all the units, since we're about to do just that
        for( Unit unit : myCo.units )
        {
          if( unit.isTurnOver )
            continue; // No actions for stale units.
          unitQueue.offer(unit);
        }
      }

      Queue<Unit> tempQueue = new ArrayDeque<Unit>();
      // Evaluate siege attacks
      while (actions.isEmpty() && !unitQueue.isEmpty())
      {
        Unit unit = unitQueue.poll();
        if( (gameMap.getEnvironment(unit.x, unit.y).terrainType == TerrainType.FACTORY) || !unit.model.hasImmobileWeapon() )
        {
          tempQueue.offer(unit);
          continue;
        }

        // Find the possible destination.
        XYCoord coord = new XYCoord(unit.x, unit.y);

        // Figure out how to get here.
        Path movePath = Utils.findShortestPath(unit, coord, gameMap);

        // Figure out what I can do here.
        ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);
        GameAction bestAttack = null;
        double bestDamage = 0;
        for( GameActionSet actionSet : actionSets )
        {
          // See if we have the option to attack.
          if( actionSet.getSelected().getType() == GameAction.ActionType.ATTACK )
          {
            for( GameAction action : actionSet.getGameActions() )
            {
              Unit target = gameMap.getLocation(action.getTargetLocation()).getResident();
              double damage = target.model.getCost() * Math.min(target.getPreciseHP(), CombatEngine.simulateBattleResults(unit, target, gameMap, unit.x, unit.y).defenderHPLoss);
              if( damage > bestDamage )
              {
                bestDamage = damage;
                bestAttack = action;
              }
            }
          }
        }
        if( null != bestAttack )
        {
          log(String.format("%s is shooting %s", unit.toStringWithLocation(), gameMap.getLocation(bestAttack.getTargetLocation()).getResident()));
          actions.offer(bestAttack);
          stateChange = true;
          break;
        }
        else // tempqueue the siege unit if it can't attack
        {
          tempQueue.offer(unit);
        }
      }

      // reset our units back into unitQueue if we need to calculate more
      if( actions.isEmpty() )
      {
        unitQueue.addAll(tempQueue);
        tempQueue.clear();
      }

      // Try to get confirmed kills with mobile strikes.
      if( actions.isEmpty() && !unitQueue.isEmpty() )
      {
        boolean foundKill = false;
        // Get a count of enemy forces.
        Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myCo, gameMap);
        for( Commander co : unitLists.keySet() )
        {
          // log(String.format("Hunting CO %s's units", co.coInfo.name));
          if( myCo.isEnemy(co) )
          {
            for( Unit target : unitLists.get(co) )
            {
              // log(String.format("  Would like to kill: %s", target.toStringWithLocation()));
              ArrayList<XYCoord> coordsToCheck = Utils.findLocationsInRange(gameMap, new XYCoord(target.x, target.y), 1, mobileAttackRange);
              Map<XYCoord, Unit> neededAttacks = new HashMap<XYCoord, Unit>();
              double damage = 0;

              // Figure out where we can attack from, and include attackers already in range by default.
              for( XYCoord xyc : coordsToCheck )
              {
                Location loc = gameMap.getLocation(xyc);
                Unit resident = loc.getResident();

                // Units who can attack from their current position volunteer themselves. Probably not smart sometimes, but oh well.
                if( null != resident && resident.CO == myCo && !resident.isTurnOver &&
                    resident.canAttack(target.model, xyc.getDistance(target.x, target.y), false))
                {
                  damage += CombatEngine.simulateBattleResults(resident, target, gameMap, xyc.xCoord, xyc.yCoord).defenderHPLoss;
                  neededAttacks.put(xyc, resident);
                  if( damage >= target.getPreciseHP() )
                  {
                    foundKill = true;
                    break;
                  }
                }
                // Check that we could potentially move into this space. Also we're scared of fog
                else if( (null == resident) && (gameMap.getEnvironment(xyc).terrainType != TerrainType.FACTORY)
                    && !gameMap.isLocationFogged(xyc) )
                  neededAttacks.put(xyc, null);
              }
              if( foundKill || findAssaultKills(gameMap, neededAttacks, target, damage) >= target.getPreciseHP() )
              {
                log(String.format("  Gonna try to kill %s, who has %s HP", target.toStringWithLocation(), target.getHP()));
                double damageSum = 0;
                for( XYCoord xyc : neededAttacks.keySet() )
                {
                  Unit unit = neededAttacks.get(xyc);
                  if( null != unit )
                  {
                    damageSum += CombatEngine.simulateBattleResults(unit, target, gameMap, xyc.xCoord, xyc.yCoord).defenderHPLoss;
                    actions.offer(new GameAction.AttackAction(gameMap, unit, Utils.findShortestPath(unit, xyc, gameMap), target.x, target.y));
                    unitQueue.remove(unit);
                    log(String.format("    %s brings the damage total to %s", unit.toStringWithLocation(), damageSum));
                    if (damageSum >= target.getPreciseHP())
                      break;
                  }
                }
                stateChange = true;
                foundKill = true;
                break;
              }
              else
              {
                // log(String.format("  Can't kill %s, oh well", target.toStringWithLocation()));
              }
            }
          }
          if( foundKill )
            break;
        }
      }

      // Figure out where we don't wanna go
      Map<UnitModel, Map<XYCoord, Double>> threatMap = new HashMap<UnitModel, Map<XYCoord, Double>>();
      if( actions.isEmpty() )
      {
        Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myCo, gameMap);
        for( UnitModel um : myCo.unitModels )
        {
          threatMap.put(um, new HashMap<XYCoord, Double>());
          for( Commander co : unitLists.keySet() )
          {
            if( myCo.isEnemy(co) )
            {
              for( Unit threat : unitLists.get(co) )
              {
                // add each new threat to the existing threats
                Map<XYCoord, Double> threatArea = threatMap.get(um);
                for( Entry<XYCoord, Double> newThreat : AIUtils.findThreatPower(gameMap, threat, um).entrySet() )
                {
                  if( null == threatArea.get(newThreat.getKey()) )
                    threatArea.put(newThreat.getKey(), newThreat.getValue());
                  else
                    threatArea.put(newThreat.getKey(), newThreat.getValue() + threatArea.get(newThreat.getKey()));
                }
              }
            }
          }
        }
      }

      // try to get unit value by capture or attack
      while (actions.isEmpty() && !unitQueue.isEmpty())
      {
        Unit unit = unitQueue.poll();
        XYCoord position = new XYCoord(unit.x, unit.y);
        
        if (unit.getCaptureProgress() > 0)
        {
          actions.offer(new GameAction.CaptureAction(gameMap, unit, Utils.findShortestPath(unit, position, gameMap)));
          break;
        }

        boolean foundAction = false;

        // Find the possible destinations.
        ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, false);
        // sort by furthest away, good for capturing
        Utils.sortLocationsByDistance(position, destinations);
        Collections.reverse(destinations);

        for( XYCoord coord : destinations )
        {
          // Figure out how to get here.
          Path movePath = Utils.findShortestPath(unit, coord, gameMap);

          // Figure out what I can do here.
          ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);
          for( GameActionSet actionSet : actionSets )
          {
            // See if we can bag enough damage to be worth sacrificing the unit
            if( actionSet.getSelected().getType() == GameAction.ActionType.ATTACK )
            {
              for( GameAction ga : actionSet.getGameActions() )
              {
                Location loc = gameMap.getLocation(ga.getTargetLocation());
                Unit target = loc.getResident();
                double damage = CombatEngine.simulateBattleResults(unit, target, gameMap, ga.getMoveLocation().xCoord,
                    ga.getMoveLocation().yCoord).defenderHPLoss;
                
                boolean goForIt = false;
                if( target.model.getCost() * damage * AGGRO_FUNDS_WEIGHT > unit.model.getCost() * unit.getHP() )
                {
                  log(String.format("  %s is going aggro on %s", unit.toStringWithLocation(), target.toStringWithLocation()));
                  log(String.format("    He plans to deal %s HP damage for a net gain of %s funds", damage, (target.model.getCost() * damage - unit.model.getCost() * unit.getHP())/10));
                  goForIt = true;
                }
                else if( (gameMap.getEnvironment(unit.x, unit.y).terrainType != TerrainType.FACTORY) && isSafe(gameMap, threatMap, unit, ga.getMoveLocation()) )
                {
                  log(String.format("  %s thinks it's safe to attack %s", unit.toStringWithLocation(), target.toStringWithLocation()));
                  goForIt = true;
                }

                if( goForIt )
                {
                  actions.offer(ga);
                  foundAction = true;
                  break;
                }
              }
            }
            if( foundAction )
              break; // Only allow one action per unit.

            // Only consider capturing if we can sit still or go somewhere safe.
            if( actionSet.getSelected().getType() == GameAction.ActionType.CAPTURE
                && (coord.getDistance(unit.x, unit.y) == 0 || canWallHere(gameMap, threatMap, unit, coord) ) )
            {
              actions.offer(actionSet.getSelected());
              capturingProperties.add(coord);
              foundAction = true;
              break;
            }
          }
          if( foundAction )
            break; // Only allow one action per unit.
        }
        if( foundAction )
        {
          stateChange = true;
          break; // Only one action per getNextAction() call, to avoid overlap.
        }
        else
        {
          tempQueue.offer(unit); // if we can't do anything useful right now, consider just moving towards a useful destination
        }
      }

      // If no attack/capture actions are available now, just move around
      if( actions.isEmpty() && !stateChange )
      {
        while (!tempQueue.isEmpty())
        {
          Unit unit = tempQueue.poll();

          // Find the possible destinations.
          ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, false);

          if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
          {
//            log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
            int index = 0;
            XYCoord goal = null;
            Path path = null;
            boolean validTarget = false;
            ArrayList<XYCoord> validTargets = new ArrayList<>();

            if( unit.model.possibleActions.contains(ActionType.CAPTURE) )
            {
              validTargets.addAll(unownedProperties);
            }
            else
            {
              for( XYCoord coord : unownedProperties )
              {
                Location loc = gameMap.getLocation(coord);
                if( loc.getEnvironment().terrainType == TerrainType.HEADQUARTERS && loc.getOwner() != null )
                {
                  validTargets.add(coord);
                }
              }
            }
            // Loop until we find a valid property to go capture or run out of options.
            Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), validTargets);
            do
            {
              goal = validTargets.get(index++);
              path = Utils.findShortestPath(unit, goal, gameMap, true);
              validTarget = (myCo.isEnemy(gameMap.getLocation(goal).getOwner()) // Property is not allied.
                  && !capturingProperties.contains(goal) // We aren't already capturing it.
                  && (path.getPathLength() > 0)); // We can reach it.
//              log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal,
//                  (validTarget ? "Yes" : "No")));
            } while (!validTarget && (index < validTargets.size())); // Loop until we run out of properties to check.

            if( validTarget )
            {
              log(String.format("    Selected %s at %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal));

              // Choose the point on the path just out of our range as our 'goal', and try to move there.
              // This will allow us to navigate around large obstacles that require us to move away
              // from our intended long-term goal.
              path.snip(unit.model.movePower + 1); // Trim the path approximately down to size.
              goal = new XYCoord(path.getEnd().x, path.getEnd().y); // Set the last location as our goal.

//              log(String.format("    Intermediate waypoint: %s", goal));

              // Sort my currently-reachable move locations by distance from the goal,
              // and build a GameAction to move to the closest one.
              Utils.sortLocationsByDistance(goal, destinations);
              XYCoord destination = null;
              // try to get somewhere safe
              log(String.format("    %s would like to travel towards %s safely", unit.toStringWithLocation(), goal));
              for( XYCoord xyc : destinations )
              {
                log(String.format("    is it safe to go to %s?", xyc));
                if( canWallHere(gameMap, threatMap, unit, xyc) )
                {
                  log(String.format("    Yes"));
                  destination = xyc;
                  break;
                }
              }
              if( null != destination )
              {
                Path movePath = Utils.findShortestPath(unit, destination, gameMap);
                if( movePath.getPathLength() > 1 ) // We only want to try to travel if we can actually go somewhere
                {
                  GameAction move = new GameAction.WaitAction(unit, movePath);
                  actions.offer(move);
                  stateChange = true;
                  break;
                }
              }
            }
          }
        }
      }

      // Check for an available buying enhancement power
      if( actions.isEmpty() && !stateChange )
      {
        AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_BUY);
      }

      // We will add all build commands at once, since they can't conflict.
      if( actions.isEmpty() && !stateChange )
      {
        queueUnitProductionActions(gameMap);
      }

      // Check for a turn-ending power
      if( actions.isEmpty() && !stateChange )
      {
        AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_TURN_END);
      }

      // Return the next action, or null if actions is empty.
      nextAction = actions.poll();
    } while (nextAction == null && stateChange); // we don't want to end early, so if the state changed and we don't have an action yet, try again
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }

  private boolean isSafe(GameMap gameMap, Map<UnitModel, Map<XYCoord, Double>> threatMap, Unit unit, XYCoord xyc)
  {
    Double threat = threatMap.get(myCo.getUnitModel(unit.model.type)).get(xyc);
    int threshhold = unit.model.hasDirectFireWeapon() ? DIRECT_THREAT_THRESHHOLD : INDIRECT_THREAT_THRESHHOLD;
    return (null == threat || threshhold > threat);
  }

  /**
   * @return whether it's safe or a good place to wall
   */
  private boolean canWallHere(GameMap gameMap, Map<UnitModel, Map<XYCoord, Double>> threatMap, Unit unit, XYCoord xyc)
  {
    // if we're safe, we're safe
    if( isSafe(gameMap, threatMap, unit, xyc) )
      return true;
    // don't stand on a factory or in danger for no good reason
    if( gameMap.getEnvironment(xyc).terrainType == TerrainType.FACTORY || !unit.model.hasDirectFireWeapon())
      return false;

    // TODO: Determine whether the ally actually needs a wall there. Mechs walling for Tanks vs inf is... silly.
    // if we'd be a nice wall for a worthy ally, we can pretend we're safe there also
    ArrayList<XYCoord> adjacentCoords = Utils.findLocationsInRange(gameMap, xyc, 1);
    for( XYCoord coord : adjacentCoords )
    {
      Location loc = gameMap.getLocation(coord);
      if( loc != null )
      {
        Unit resident = loc.getResident();
        if( resident != null && !myCo.isEnemy(resident.CO) 
            && resident.model.getCost() * resident.getHP() > unit.model.getCost() * unit.getHP() )
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Attempts to find a combination of attacks that will create a kill.
   * Recursive.
   */
  private double findAssaultKills(GameMap gameMap, Map<XYCoord, Unit> neededAttacks, Unit target, double pDamage)
  {
    // base case; we found a kill
    if( pDamage >= target.getPreciseHP() )
    {
      return pDamage;
    }

    double damage = pDamage;
    for( XYCoord xyc : neededAttacks.keySet() )
    {
      // Don't try to attack from the same space twice.
      if( null != neededAttacks.get(xyc) )
        continue;

      // Attack with the cheapest assault units, if possible.
      Queue<Unit> assaultQueue = new PriorityQueue<Unit>(11, new AIUtils.UnitCostComparator(true));
      assaultQueue.addAll(unitQueue);
      while (!assaultQueue.isEmpty())
      {
        Unit unit = assaultQueue.poll();
        if( !unit.model.hasDirectFireWeapon() || neededAttacks.containsValue(unit) ) // don't try to attack twice with one unit
          continue;

        int dist = xyc.getDistance(target.x, target.y);

        // Figure out how to get here.
        Path movePath = Utils.findShortestPath(unit, xyc, gameMap);

        if( movePath.getPathLength() > 0 && unit.canAttack(target.model, dist, true) )
        {
          neededAttacks.put(xyc, unit);
          double thisDamage = CombatEngine.simulateBattleResults(unit, target, gameMap, xyc.xCoord, xyc.yCoord).defenderHPLoss;
          thisDamage = findAssaultKills(gameMap, neededAttacks, target, thisDamage);

          log(String.format("  Use %s to deal %sHP?", unit.toStringWithLocation(), thisDamage));

          // Base case, stop iterating.
          if( thisDamage >= target.getPreciseHP() )
          {
            log(String.format("    Yes, shoot %s", target.toStringWithLocation()));
            damage = thisDamage;
            break;
          }
          else
          {
            log(String.format("    Nope"));
            neededAttacks.put(xyc, null);
          }
        }
      }
    }

    return damage;
  }

  private void queueUnitProductionActions(GameMap gameMap)
  {
    log("Evaluating Production needs");
    int budget = myCo.money;

    // Get a count of enemy forces.
    Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myCo, gameMap);
    Map<UnitModel, Double> enemyUnitCounts = new HashMap<UnitModel, Double>();
    for( Commander co : unitLists.keySet() )
    {
      if( myCo.isEnemy(co) )
      {
        for( Unit u : unitLists.get(co) )
        {
          // Count how many of each model of enemy units are in play.
          if( enemyUnitCounts.containsKey(u.model) )
          {
            enemyUnitCounts.put(u.model, enemyUnitCounts.get(u.model) + (u.getHP() / 10));
          }
          else
          {
            enemyUnitCounts.put(u.model, u.getHP() / 10.0);
          }
        }
      }
    }

    // Figure out what unit types we can purchase with our available properties.
    AIUtils.CommanderProductionInfo CPI = new AIUtils.CommanderProductionInfo(myCo, gameMap);

    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return;
    }

    // We like Recons too much. Don't buy them. Their movement is deceptively high.
    CPI.availableUnitModels.remove(myCo.getUnitModel(UnitEnum.RECON));

    // Sort enemy units by cardinality. We will attempt to build counters for the least numerous first.
    // The most numerous enemies are probably cheap, and also countered by whatever we build for the narrow case.
    ArrayList<UnitModel> enemyModels = new ArrayList<UnitModel>();
    ArrayList<Entry<UnitModel, Double>> entryArray = new ArrayList<Entry<UnitModel, Double>>(enemyUnitCounts.entrySet());
    // change unit quantity->funds
    for( Entry<UnitModel, Double> ent : entryArray )
    {
      ent.setValue(ent.getValue() * ent.getKey().getCost());
    }

    Collections.sort(entryArray, new UnitModelFundsComparator());
    for( Entry<UnitModel, Double> ent : entryArray )
    {
      enemyModels.add(ent.getKey());
    }

    // Try to purchase units that will counter the most-represented enemies.
    while (!enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())
    {
      // Find the first (most funds-invested) enemy UnitModel, and remove it. Even if we can't find an adequate counter,
      // there is not reason to consider it again on the next iteration.
      UnitModel enemyToCounter = enemyModels.get(0);
      enemyModels.remove(enemyToCounter);
      double enemyNumber = enemyUnitCounts.get(enemyToCounter);
      log(String.format("Need a counter for %sx%s", enemyToCounter, enemyNumber));
      log(String.format("Remaining budget: %s", budget));

      // Get our possible options for countermeasures.
      ArrayList<UnitModel> availableUnitModels = new ArrayList<UnitModel>(CPI.availableUnitModels);
      while (!availableUnitModels.isEmpty())
      {
        // Sort my available models by their power against this enemy type.
        Collections.sort(availableUnitModels, new UnitPowerComparator(enemyToCounter));

        // Grab the best counter.
        UnitModel idealCounter = availableUnitModels.get(0);
        availableUnitModels.remove(idealCounter); // Make sure we don't try to build two rounds of the same thing in one turn.
        // I only want combat units, since I don't understand transports
        if( !idealCounter.weaponModels.isEmpty() )
        {
          log(String.format("  buy %s?", idealCounter));

          // Figure out how many of idealCounter we want, and how many we can actually build.
          int numberToBuy = 2;
          log(String.format("    Would like to build %s of them", numberToBuy));
          int maxBuildable = CPI.getNumFacilitiesFor(idealCounter);
          log(String.format("    Facilities available: %s", maxBuildable));
          if( numberToBuy > maxBuildable )
            numberToBuy = maxBuildable; // This is the number we have production for right now.
          int totalCost = numberToBuy * idealCounter.getCost();

          // Calculate a cost buffer to ensure we have enough money left so that no factories sit idle.
          UnitModel infModel = myCo.getUnitModel(UnitModel.UnitEnum.INFANTRY);
          int costBuffer = (CPI.getNumFacilitiesFor(infModel) - 1) * infModel.getCost(); // The -1 assumes we will build this unit from a factory. Possibly untrue.
          if( 0 > costBuffer )
            costBuffer = 0; // No granting ourselves extra moolah.
          while (totalCost > (budget - costBuffer)) // This finds how many we can afford.
          {
            totalCost -= idealCounter.getCost();
            numberToBuy--;
          }
          if( numberToBuy > 0 )
          {
            // Go place orders.
            log(String.format("    I can build %s %s, for a cost of %s", numberToBuy, idealCounter, totalCost));
            for( int i = 0; i < numberToBuy; ++i )
            {
              Location loc = CPI.getLocationToBuild(idealCounter);
              actions.offer(new GameAction.UnitProductionAction(myCo, idealCounter, loc.getCoordinates()));
              budget -= idealCounter.getCost();
              CPI.removeBuildLocation(loc);
            }
            // We found a counter for this enemy UnitModel; break and go to the next type.
            // This break means we will build at most one type of unit per turn to counter each enemy type.
            break;
          }
          else
          {
            log(String.format("    %s cost %s, I have %s (witholding %s).", idealCounter, idealCounter.getCost(), budget,
                costBuffer));
          }
        }
      } // ~while( !availableUnitModels.isEmpty() )
    } // ~while( !enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())

    // Build infantry from any remaining facilities.
    log("Building infantry to fill out my production");
    UnitModel infModel = myCo.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    while ((budget >= infModel.getCost()) && (CPI.availableUnitModels.contains(infModel)))
    {
      Location loc = CPI.getLocationToBuild(infModel);
      actions.offer(new GameAction.UnitProductionAction(myCo, infModel, loc.getCoordinates()));
      budget -= infModel.getCost();
      CPI.removeBuildLocation(loc);
    }
  }

  /**
   * Sort units by funds amount in decending order.
   */
  private static class UnitModelFundsComparator implements Comparator<Entry<UnitModel, Double>>
  {
    @Override
    public int compare(Entry<UnitModel, Double> entry1, Entry<UnitModel, Double> entry2)
    {
      double diff = entry2.getValue() - entry1.getValue();
      return (int) (diff * 10); // Multiply by 10 since we return an int, but don't want to lose the decimal-level discrimination.
    }
  }

  /**
   * Arrange UnitModels according to their effective damage/range against a configured UnitModel.
   */
  private static class UnitPowerComparator implements Comparator<UnitModel>
  {
    UnitModel targetModel;

    public UnitPowerComparator(UnitModel targetType)
    {
      targetModel = targetType;
    }

    @Override
    public int compare(UnitModel model1, UnitModel model2)
    {
      double eff1 = 0;
      double eff2 = 0;
      for( WeaponModel wm : model1.weaponModels )
      {
        double damage = Weapon.strategies[Weapon.currentStrategy].getDamage(wm, targetModel);
        double range = wm.maxRange + ((wm.canFireAfterMoving) ? model1.movePower : 0);
        double effectiveness = damage * targetModel.getCost() * (1 + range * RANGE_WEIGHT);
        eff1 = Math.max(eff1, effectiveness);
      }
      for( WeaponModel wm : model2.weaponModels )
      {
        double damage = Weapon.strategies[Weapon.currentStrategy].getDamage(wm, targetModel);
        double range = wm.maxRange + ((wm.canFireAfterMoving) ? model2.movePower : 0);
        double effectiveness = damage * targetModel.getCost() * (1 + range * RANGE_WEIGHT);
        eff2 = Math.max(eff2, effectiveness);
      }

      return (eff1 < eff2) ? 1 : ((eff1 > eff2) ? -1 : 0);
    }
  }
}
