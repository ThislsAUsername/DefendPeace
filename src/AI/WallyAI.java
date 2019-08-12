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
import java.util.Set;

import AI.AIUtils.CommanderProductionInfo;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionType;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.MoveTypes.MoveType;
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

    @Override
    public String getDescription()
    {
      return
          "Wally values units based on firepower and the area they can threaten.\n" +
          "He tries to keep units out of harm's way, and to protect expensive units with cheaper ones.\n" +
          "He can be overly timid, and thus is a fan of artillery.";
    }
  }
  public static final AIMaker info = new instantiator();

  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }
  
  Queue<GameAction> actions = new ArrayDeque<GameAction>();

  private Commander myCo = null;

  // What % damage I'll ignore when checking safety
  private static final int INDIRECT_THREAT_THRESHHOLD = 7;
  private static final int DIRECT_THREAT_THRESHHOLD = 13;
  private static final double UNIT_REFUEL_THRESHHOLD = 0.25; // Fuel fraction for refuel
  private static final double UNIT_REARM_THRESHHOLD = 0.25; // Fraction of ammo in any weapon below which to consider resupply
  private static final double AGGRO_EFFECT_THRESHHOLD = 0.42; // How effective do I need to be against a unit to target it?
  private static final double AGGRO_FUNDS_WEIGHT = 1.5; // How many times my value I need to get before sacrifice is worth it
  private static final double RANGE_WEIGHT = 1; // Exponent for how powerful range is considered to be
  private static final double TERRAIN_PENALTY_WEIGHT = 3; // Exponent for how crippling we think high move costs are
  private static final double MIN_SIEGE_RANGE_WEIGHT = 0.8; // Exponent for how much to penalize siege weapon ranges for their min ranges 

  private ArrayList<XYCoord> unownedProperties;
  private ArrayList<XYCoord> capturingProperties;
  
  private HashMap<UnitModel, Double> unitEffectiveMove = null; // How well the unit can move, on average, on this map
  public double getEffectiveMove(UnitModel model)
  {
    if (unitEffectiveMove.containsKey(model))
      return unitEffectiveMove.get(model);

    MoveType p = model.propulsion;
    GameMap map = myCo.myView;
    double totalCosts = 0;
    int validTiles = 0;
    double totalTiles = map.mapWidth * map.mapHeight; // to avoid integer division
    // Iterate through the map, counting up the move costs of all valid terrain
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        Environment terrain = map.getLocation(w, h).getEnvironment();
        if( p.canTraverse(terrain) )
        {
          validTiles++;
          int cost = p.getMoveCost(terrain);
          totalCosts += Math.pow(cost, TERRAIN_PENALTY_WEIGHT);
        }
      }
    }
    //             term for how fast you are   term for map coverage
    double ratio = (validTiles / totalCosts) * (validTiles / totalTiles); // 1.0 is the max expected value
    
    double effMove = model.movePower * ratio;
    unitEffectiveMove.put(model, effMove);
    return effMove;
  }

  private StringBuffer logger = new StringBuffer();
  private int turnNum = 0;

  public WallyAI(Commander co)
  {
    myCo = co;
  }

  private void init(GameMap map)
  {
    unitEffectiveMove = new HashMap<UnitModel, Double>();
    // init all move multipliers before powers come into play
    for( Commander co : map.commanders )
    {
      for( UnitModel model : co.unitModels.values() )
      {
        getEffectiveMove(model);
      }
    }
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    if (null == unitEffectiveMove)
      init(gameMap);
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

    // Check for a turn-kickoff power
    AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_TURN_START);

    capturingProperties = new ArrayList<XYCoord>();
    for( Unit unit : myCo.units )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        capturingProperties.add(unit.getCaptureTargetCoords());
        XYCoord position = new XYCoord(unit.x, unit.y);
        actions.offer(new GameAction.CaptureAction(gameMap, unit, Utils.findShortestPath(unit, position, gameMap)));
      }
    }
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
    // If we have more actions ready, don't bother calculating stuff.
    if( !actions.isEmpty() )
    {
      GameAction action = actions.poll();
      log("  Action: " + action);
      return action;
    }

    // Prioritize using our most expensive units first
    Queue<Unit> unitQueue = new PriorityQueue<Unit>(11, new AIUtils.UnitCostComparator(false));
    for( Unit unit : myCo.units )
    {
      if( unit.isTurnOver )
        continue; // No actions for stale units.
      unitQueue.offer(unit);
    }

    GameAction nextAction = null;
    do
    {
      Queue<Unit> tempQueue = new ArrayDeque<Unit>();
      // Evaluate siege attacks
      while (actions.isEmpty() && !unitQueue.isEmpty())
      {
        Unit unit = unitQueue.poll();
        if( unit.CO.unitProductionByTerrain.containsKey(gameMap.getEnvironment(unit.x, unit.y).terrainType) || !unit.model.hasImmobileWeapon() )
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
          if( actionSet.getSelected().getType() == UnitActionType.ATTACK )
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
            Queue<Unit> targetQueue = new PriorityQueue<Unit>(unitLists.get(co).size(), new AIUtils.UnitCostComparator(false));
            targetQueue.addAll(unitLists.get(co)); // We want to kill the most expensive enemy units
            for( Unit target : targetQueue )
            {
              if (target.getHP() < 1) // Try not to pick fights with zombies
                continue;
              // log(String.format("  Would like to kill: %s", target.toStringWithLocation()));
              ArrayList<XYCoord> coordsToCheck = Utils.findLocationsInRange(gameMap, new XYCoord(target.x, target.y), 1, AIUtils.findMaxStrikeWeaponRange(myCo));
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
                else if( (null == resident) && myCo.unitProductionByTerrain.containsKey(gameMap.getEnvironment(xyc).terrainType)
                    && !gameMap.isLocationFogged(xyc) )
                  neededAttacks.put(xyc, null);
              }
              if( foundKill || findAssaultKills(gameMap, unitQueue, neededAttacks, target, damage) >= target.getPreciseHP() )
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
      ArrayList<Unit> allThreats = new ArrayList<Unit>();
      Map<UnitModel, Map<XYCoord, Double>> threatMap = new HashMap<UnitModel, Map<XYCoord, Double>>();
      if( actions.isEmpty() )
      {
        Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myCo, gameMap);
        for( UnitModel um : myCo.unitModels.values() )
        {
          threatMap.put(um, new HashMap<XYCoord, Double>());
          for( Commander co : unitLists.keySet() )
          {
            if( myCo.isEnemy(co) )
            {
              for( Unit threat : unitLists.get(co) )
              {
                // add each new threat to the existing threats
                allThreats.add(threat);
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

        boolean foundAction = false;

        // Find the possible destinations.
        ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, true);
        // sort by furthest away, good for capturing
        Utils.sortLocationsByDistance(position, destinations);
        Collections.reverse(destinations);

        for( XYCoord coord : destinations )
        {
          // Figure out how to get here.
          Path movePath = Utils.findShortestPath(unit, coord, gameMap);

          // Figure out what I can do here.
          ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, true);
          for( GameActionSet actionSet : actionSets )
          {
            boolean spaceFree = gameMap.isLocationEmpty(unit, coord);
            Unit resident = gameMap.getLocation(coord).getResident();
            if (!spaceFree)
            {
              if (resident.isTurnOver || resident.getHP()*resident.model.getCost() >= unit.getHP()*unit.model.getCost())
                continue; // If we can't evict or we're not worth more than the other dude, we don't get to kick him out
              log(String.format("  Evicting %s if I need to", resident.toStringWithLocation()));
            }

            // See if we can bag enough damage to be worth sacrificing the unit
            if( actionSet.getSelected().getType() == UnitActionType.ATTACK )
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
                else if( unit.CO.unitProductionByTerrain.containsKey(gameMap.getEnvironment(unit.x, unit.y).terrainType) && isSafe(gameMap, threatMap, unit, ga.getMoveLocation()) )
                {
                  log(String.format("  %s thinks it's safe to attack %s", unit.toStringWithLocation(), target.toStringWithLocation()));
                  goForIt = true;
                }

                if( goForIt && (spaceFree || queueTravelAction(gameMap, allThreats, threatMap, resident, true)))
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
            if( actionSet.getSelected().getType() == UnitActionType.CAPTURE
                && ( coord.getDistance(unit.x, unit.y) == 0 || canWallHere(gameMap, threatMap, unit, coord) ) 
                && ( spaceFree || queueTravelAction(gameMap, allThreats, threatMap, resident, true) ) )
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
          break; // Only one action per getNextAction() call, to avoid overlap.
        }
        else
        {
          tempQueue.offer(unit); // if we can't do anything useful right now, consider just moving towards a useful destination
        }
      }

      // If no attack/capture actions are available now, just move around
      if( actions.isEmpty() )
      {
        while (!tempQueue.isEmpty())
        {
          Unit unit = tempQueue.poll();
          if (queueTravelAction(gameMap, allThreats, threatMap, unit, false))
            break;
        }
      }

      // Check for an available buying enhancement power
      if( actions.isEmpty() )
      {
        AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_BUY);
      }

      // We will add all build commands at once, since they can't conflict.
      if( actions.isEmpty() )
      {
        queueUnitProductionActions(gameMap);
      }

      // Check for a turn-ending power
      if( actions.isEmpty() )
      {
        AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_TURN_END);
      }

      // Return the next action, or null if actions is empty.
      nextAction = actions.poll();
    } while ( nextAction == null && !unitQueue.isEmpty() ); // we don't want to end early, so if the state changed and we don't have an action yet, try again
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }

  /** Produces a list of destinations for the unit, ordered by their relative precedence */
  private ArrayList<XYCoord> findTravelDestinations(GameMap gameMap, ArrayList<Unit> allThreats, Map<UnitModel, Map<XYCoord, Double>> threatMap, Unit unit)
  {
    ArrayList<XYCoord> goals = new ArrayList<XYCoord>();

    boolean shouldResupply = (unit.getHP() < unit.model.maxHP) || (unit.fuel < unit.model.maxFuel*UNIT_REFUEL_THRESHHOLD);
    if( !shouldResupply )
    {
      // Resupply also if we need ammo.
      for( Weapon weap : unit.weapons )
      {
        if( weap.ammo <= weap.model.maxAmmo * UNIT_REARM_THRESHHOLD )
          shouldResupply = true;
      }
    }

    if( shouldResupply )
    {
      log(String.format("%s needs supplies.", unit.toStringWithLocation()));
      ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
      for( XYCoord coord : stations )
      {
        Location station = gameMap.getLocation(coord);
        // Go to the nearest unoccupied friendly depot, but don't gum up the production lines.
        if( station.getResident() == null && unit.CO.unitProductionByTerrain.containsKey(station.getEnvironment().terrainType) )
          goals.add(coord);
      }
      Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), goals);
    }
    else if( unit.model.possibleActions.contains(UnitActionType.CAPTURE) )
    {
      goals.addAll(unownedProperties);
      Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), goals);
    }
    else if( unit.model.possibleActions.contains(UnitActionType.ATTACK) )
    {
      Map<UnitModel, Double> valueMap = new HashMap<UnitModel, Double>();
      Map<UnitModel, ArrayList<XYCoord>> targetMap = new HashMap<UnitModel, ArrayList<XYCoord>>();

      // Categorize all enemies by type, and all types by how well we match up vs them
      for (Unit target : allThreats)
      {
        UnitModel model = target.model;
        XYCoord targetCoord = new XYCoord(target.x, target.y);
        double effectiveness = findEffectiveness(unit.model, target.model);
        if (Utils.findShortestPath(unit, targetCoord, gameMap, true) != null &&
            AGGRO_EFFECT_THRESHHOLD > effectiveness)
        {
          valueMap.put(model, effectiveness*model.getCost());
          if (!targetMap.containsKey(model)) targetMap.put(model, new ArrayList<XYCoord>());
          targetMap.get(model).add(targetCoord);
        }
      }

      // Sort all individual target lists by distance
      for (ArrayList<XYCoord> targetList : targetMap.values())
        Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), targetList);

      // Sort all target types by how much we want to shoot them with this unit
      Queue<Entry<UnitModel, Double>> targetTypesInOrder = 
          new PriorityQueue<Entry<UnitModel, Double>>(myCo.unitModels.size(), new UnitModelFundsComparator());
      targetTypesInOrder.addAll(valueMap.entrySet());

      while (!targetTypesInOrder.isEmpty())
      {
        UnitModel model = targetTypesInOrder.poll().getKey(); // peel off the juiciest
        goals.addAll(targetMap.get(model)); // produce a list ordered by juiciness first, then distance TODO: consider a holistic "juiciness" metric that takes into account both matchup and distance?
      }
    }

    if (goals.isEmpty()) // Send 'em to the HQ if they haven't got anything better to do
    {
      for( XYCoord coord : unownedProperties )
      {
        Location loc = gameMap.getLocation(coord);
        if( loc.getEnvironment().terrainType == TerrainType.HEADQUARTERS && loc.getOwner() != null )
        {
          goals.add(coord);
        }
      }
      Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), goals);
    }

    return goals;
  }

  /** Find a good long-term objective for the given unit, and pursue it (with consideration for life-preservation optional) */
  private boolean queueTravelAction(GameMap gameMap, ArrayList<Unit> allThreats, Map<UnitModel, Map<XYCoord, Double>> threatMap, Unit unit, boolean ignoreSafety)
  {
    // Find the possible destinations.
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, false);
    if (ignoreSafety) // If we *must* travel, make sure we do actually move.
      destinations.remove(0);

    if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
    {
      log(String.format("  Evaluating travel for %s", unit.toStringWithLocation()));
      int index = 0;
      XYCoord goal = null;
      Path path = null;
      boolean validTarget = false;
      ArrayList<XYCoord> validTargets = findTravelDestinations(gameMap, allThreats, threatMap, unit);

      // Loop until we find a valid property to go capture or run out of options.
      do
      {
        goal = validTargets.get(index++);
        path = Utils.findShortestPath(unit, goal, gameMap, true);
        validTarget = (myCo.isEnemy(gameMap.getLocation(goal).getOwner()) // Property is not allied.
            && !capturingProperties.contains(goal) // We aren't already capturing it.
            && (path.getPathLength() > 0)); // We can reach it.
//        log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal,
//            (validTarget ? "Yes" : "No")));
      } while (!validTarget && (index < validTargets.size())); // Loop until we run out of properties to check.

      if( validTarget )
      {
        log(String.format("    Selected %s at %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal));

        // Choose the point on the path just out of our range as our 'goal', and try to move there.
        // This will allow us to navigate around large obstacles that require us to move away
        // from our intended long-term goal.
        path.snip(unit.model.movePower + 1); // Trim the path approximately down to size.
        goal = new XYCoord(path.getEnd().x, path.getEnd().y); // Set the last location as our goal.

//        log(String.format("    Intermediate waypoint: %s", goal));

        // Sort my currently-reachable move locations by distance from the goal,
        // and build a GameAction to move to the closest one.
        Utils.sortLocationsByDistance(goal, destinations);
        XYCoord destination = null;
        // try to get somewhere safe
        log(String.format("    %s would like to travel towards %s. Safely?: %s", unit.toStringWithLocation(), goal, !ignoreSafety));
        for( XYCoord xyc : destinations )
        {
          log(String.format("    is it safe to go to %s?", xyc));
          if( ignoreSafety || canWallHere(gameMap, threatMap, unit, xyc) )
          {
            log(String.format("    Yes"));
            destination = xyc;
            break;
          }
        }
        if( null != destination )
        {
          Path movePath = Utils.findShortestPath(unit, destination, gameMap);
          GameAction action = null;
          if( movePath.getPathLength() > 1 ) // We only want to try to travel if we can actually go somewhere
          {
            ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, false);

            // Since we're moving anyway, might as well try shooting the scenery
            for( GameActionSet actionSet : actionSets )
            {
              if( actionSet.getSelected().getType() == UnitActionType.ATTACK )
              {
                double bestDamage = 0;
                for( GameAction attack : actionSet.getGameActions() )
                {
                  Unit target = gameMap.getLocation(attack.getTargetLocation()).getResident();
                  BattleSummary results = CombatEngine.simulateBattleResults(unit, target, gameMap, destination.xCoord, destination.yCoord);
                  double loss   = Math.min(unit  .getHP(), (int)results.attackerHPLoss);
                  double damage = Math.min(target.getHP(), (int)results.defenderHPLoss);
                  if( damage > bestDamage && damage > loss ) // only shoot that which you hurt more than it hurts you
                  {
                    bestDamage = damage;
                    action = attack;
                  }
                }
              }
            }

            if( null == action) // Just wait if we can't do anything cool
             action = new GameAction.WaitAction(unit, movePath);
            actions.offer(action);
            return true;
          }
        }
      }
    }
    return false;
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
    // Don't stand on a friendly factory for no good reason
    Location destination = gameMap.getLocation(xyc);
    if( !unit.CO.isEnemy(destination.getOwner()) && unit.CO.unitProductionByTerrain.containsKey(destination.getEnvironment().terrainType) )
      return false;
    // if we're safe, we're safe
    if( isSafe(gameMap, threatMap, unit, xyc) )
      return true;

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
  private double findAssaultKills(GameMap gameMap, Queue<Unit> unitQueue, Map<XYCoord, Unit> neededAttacks, Unit target, double pDamage)
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

          if (thisDamage > target.getPreciseHP())
            continue; // OHKOs should be decided using different logic

          log(String.format("  Use %s to deal %sHP?", unit.toStringWithLocation(), thisDamage));
          thisDamage = findAssaultKills(gameMap, unitQueue, neededAttacks, target, thisDamage);

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

  /**
   * Returns the center mass of a given unit type, weighted by HP
   * NOTE: Will violate fog knowledge
   */
  private static XYCoord findAverageDeployLocation(GameMap gameMap, Commander co, UnitModel model)
  {
    // init with the center of the map
    int totalX = gameMap.mapWidth /2;
    int totalY = gameMap.mapHeight/2;
    int totalPoints = 1;
    for( Unit unit : co.units )
    {
      if( unit.model == model )
      {
        totalX += unit.x * unit.getHP();
        totalY += unit.y * unit.getHP();
        totalPoints += unit.getHP();
      }
    }

    return new XYCoord(totalX/totalPoints, totalY/totalPoints);
  }

  /**
   * Returns the ideal place to build a unit type or null if it's impossible
   * Kinda-sorta copied from AIUtils
   */
  public XYCoord getLocationToBuild(CommanderProductionInfo CPI, UnitModel model)
  {
    Set<TerrainType> desiredTerrains = CPI.modelToTerrainMap.get(model);
    ArrayList<XYCoord> candidates = new ArrayList<XYCoord>();
    for( Location loc : CPI.availableProperties )
    {
      if( desiredTerrains.contains(loc.getEnvironment().terrainType) )
      {
        candidates.add(loc.getCoordinates());
      }
    }
    if (candidates.isEmpty())
      return null;

    // Sort locations by how close they are to "center mass" of that unit type, then reverse since we want to distribute our forces
    Utils.sortLocationsByDistance(findAverageDeployLocation(myCo.myView, myCo, model), candidates);
    Collections.reverse(candidates);
    return candidates.get(0);
  }

  private void queueUnitProductionActions(GameMap gameMap)
  {
    // Figure out what unit types we can purchase with our available properties.
    AIUtils.CommanderProductionInfo CPI = new AIUtils.CommanderProductionInfo(myCo, gameMap);

    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return;
    }

    log("Evaluating Production needs");
    int budget = myCo.money;
    UnitModel infModel = myCo.getUnitModel(UnitModel.UnitEnum.INFANTRY);

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

    // Figure out how well we think we have the existing threats covered
    Map<UnitModel, Double> myUnitCounts = new HashMap<UnitModel, Double>();
    for( Unit u : myCo.units )
    {
      // Count how many of each model of enemy units are in play.
      if( myUnitCounts.containsKey(u.model) )
      {
        myUnitCounts.put(u.model, myUnitCounts.get(u.model) + (u.getHP() / 10));
      }
      else
      {
        myUnitCounts.put(u.model, u.getHP() / 10.0);
      }
    }

    for (UnitModel threat : enemyUnitCounts.keySet())
    {
      for (UnitModel counter : myUnitCounts.keySet()) // Subtract how well we think we counter each enemy from their HP counts
      {
        double counterPower = findEffectiveness(counter, threat);
        enemyUnitCounts.put(threat, enemyUnitCounts.get(threat) - counterPower * myUnitCounts.get(counter));
      }
    }

    // change unit quantity->funds
    for( Entry<UnitModel, Double> ent : enemyUnitCounts.entrySet() )
    {
      ent.setValue(ent.getValue() * ent.getKey().getCost());
    }

    Queue<Entry<UnitModel, Double>> enemyModels = 
        new PriorityQueue<Entry<UnitModel, Double>>(myCo.unitModels.size(), new UnitModelFundsComparator());
    enemyModels.addAll(enemyUnitCounts.entrySet());

    // Try to purchase units that will counter the most-represented enemies.
    while (!enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())
    {
      // Find the first (most funds-invested) enemy UnitModel, and remove it. Even if we can't find an adequate counter,
      // there is not reason to consider it again on the next iteration.
      UnitModel enemyToCounter = enemyModels.poll().getKey();
      double enemyNumber = enemyUnitCounts.get(enemyToCounter);
      log(String.format("Need a counter for %sx%s", enemyToCounter, enemyNumber / enemyToCounter.getCost() / enemyToCounter.maxHP));
      log(String.format("Remaining budget: %s", budget));

      // Get our possible options for countermeasures.
      ArrayList<UnitModel> availableUnitModels = new ArrayList<UnitModel>(CPI.availableUnitModels);
      while (!availableUnitModels.isEmpty())
      {
        // Sort my available models by their power against this enemy type.
        Collections.sort(availableUnitModels, new UnitPowerComparator(enemyToCounter, this));

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
              XYCoord coord = getLocationToBuild(CPI, idealCounter);
              actions.offer(new GameAction.UnitProductionAction(myCo, idealCounter, coord));
              budget -= idealCounter.getCost();
              CPI.removeBuildLocation(gameMap.getLocation(coord));
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
    while ((budget >= infModel.getCost()) && (CPI.availableUnitModels.contains(infModel)))
    {
      XYCoord coord = getLocationToBuild(CPI, infModel);
      actions.offer(new GameAction.UnitProductionAction(myCo, infModel, coord));
      budget -= infModel.getCost();
      CPI.removeBuildLocation(gameMap.getLocation(coord));
    }
  }

  /**
   * Sort units by funds amount in descending order.
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
    private WallyAI wally;

    public UnitPowerComparator(UnitModel targetType, WallyAI pWally)
    {
      targetModel = targetType;
      wally = pWally;
    }

    @Override
    public int compare(UnitModel model1, UnitModel model2)
    {
      double eff1 = wally.findEffectiveness(model1, targetModel);
      double eff2 = wally.findEffectiveness(model2, targetModel);

      return (eff1 < eff2) ? 1 : ((eff1 > eff2) ? -1 : 0);
    }
  }
  
  /** Returns effective power in terms of whole kills per unit, based on respective threat areas and how much damage I deal */
  public double findEffectiveness(UnitModel model, UnitModel target)
  {
    double theirRange = 0;
    for( WeaponModel wm : target.weaponModels )
    {
      double range = wm.maxRange;
      if( wm.canFireAfterMoving )
        range += getEffectiveMove(target);
      theirRange = Math.max(theirRange, range);
    }
    double counterPower = 0;
    for( WeaponModel wm : model.weaponModels )
    {
      double damage = WeaponModel.getDamage(wm, target);
      double myRange = wm.maxRange;
      if( wm.canFireAfterMoving )
        myRange += getEffectiveMove(model);
      else
        myRange -= (Math.pow(wm.minRange, MIN_SIEGE_RANGE_WEIGHT) - 1); // penalize range based on inner range
      double rangeMod = Math.pow(myRange / theirRange, RANGE_WEIGHT);
      // TODO: account for average terrain defense?
      double effectiveness = damage * rangeMod / 100;
      counterPower = Math.max(counterPower, effectiveness);
    }
    return counterPower;
  }
}
