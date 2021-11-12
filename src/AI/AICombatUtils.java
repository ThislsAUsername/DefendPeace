package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GamePath;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.Combat.StrikeParams;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import Units.WeaponModel;

public class AICombatUtils
{
  /**
   * Evaluates an attack action based on caller-provided logic
   * @param unit The attacking unit
   * @param action The action to evaluate
   * @param map The user's current game knowledge
   * @param combatScorer Evaluates combat with a unit
   * @param demolishScorer Evaluates targeting terrain
   * @return
   */
  public static double scoreAttackAction(Unit unit, GameAction action, GameMap map,
                                         Function<BattleSummary, Double> combatScorer,
                                         BiFunction<TerrainType, StrikeParams, Double> demolishScorer)
  {
    double score = 0;
    MapLocation targetLoc = map.getLocation(action.getTargetLocation());
    Unit targetUnit = targetLoc.getResident();
    if( null != targetUnit )
    {
      BattleSummary results = CombatEngine.simulateBattleResults(unit, targetUnit, map, action.getMoveLocation());
      score = combatScorer.apply(results);
    }
    else
    {
      StrikeParams params = CombatEngine.calculateTerrainDamage(unit,
          Utils.findShortestPath(unit, action.getMoveLocation(), map), targetLoc, map);
      score = demolishScorer.apply(targetLoc.getEnvironment().terrainType, params);
    }

    return score;
  }

  /**
   * @return The area and severity of threat from the unit, against the specified target type
   */
  public static Map<XYCoord, Double> findThreatPower(GameMap gameMap, Unit unit, UnitModel target)
  {
    XYCoord origin = new XYCoord(unit.x, unit.y);
    return findThreatPower(gameMap, unit, origin, target);
  }
  public static Map<XYCoord, Double> findThreatPower(GameMap gameMap, Unit unit, XYCoord origin, UnitModel target)
  {
    Map<XYCoord, Double> shootableTiles = new HashMap<XYCoord, Double>();
    boolean includeOccupiedDestinations = true; // We assume the enemy knows how to manage positioning within his turn
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(origin, unit, gameMap, includeOccupiedDestinations);
    for( WeaponModel wep : unit.model.weapons )
    {
      double damage = (null == target)? 1 : wep.getDamage(target) * unit.getHPFactor();
      if( null == target || unit.canTarget(target) )
      {
        if( !wep.canFireAfterMoving )
        {
          UnitContext uc = new UnitContext(gameMap, unit, wep, origin.xCoord, origin.yCoord);
          for (XYCoord xyc : Utils.findLocationsInRange(gameMap, origin, uc))
          {
            double val = damage;
            if (shootableTiles.containsKey(xyc))
              val = Math.max(val, shootableTiles.get(xyc));
            shootableTiles.put(xyc, val);
          }
        }
        else
        {
          for( XYCoord dest : destinations )
          {
            UnitContext uc = new UnitContext(gameMap, unit, wep, Utils.findShortestPath(unit, dest, gameMap), dest);
            for (XYCoord xyc : Utils.findLocationsInRange(gameMap, dest, uc))
            {
              double val = damage;
              if (shootableTiles.containsKey(xyc))
                val = Math.max(val, shootableTiles.get(xyc));
              shootableTiles.put(xyc, val);
            }
          }
        }
      }
    }
    return shootableTiles;
  }

  /**
   * @return The range at which the CO in question might be able to attack after moving.
   */
  public static int findMaxMobileWeaponRange(Commander co)
  {
    int range = 0;
    for( UnitModel um : co.unitModels )
    {
      for( WeaponModel wm : um.weapons )
      {
        // Consider refining this?
        // I REALLY don't want this to become a loop over all units for all possible move locations;
        //   that would likely defeat the main purpose of this function (saving computation power)
        if( wm.canFireAfterMoving )
          range = Math.max(range, wm.rangeMax);
      }
    }
    return range;
  }

  /** Return the set of locations with enemies or terrain that `unit` could attack in one turn from `start` */
  public static Set<XYCoord> findPossibleTargets(GameMap gameMap, Unit unit, XYCoord start, boolean includeTerrain)
  {
    Set<XYCoord> targetLocs = new HashSet<XYCoord>();
    boolean allowEndingOnUnits = false; // We can't attack from on top of another unit.
    ArrayList<XYCoord> moves = Utils.findPossibleDestinations(start, unit, gameMap, allowEndingOnUnits);
    for( XYCoord move : moves )
    {
      boolean moved = !move.equals(start);

      for( WeaponModel wpn : unit.model.weapons )
      {
        // Evaluate this weapon for targets if it has ammo, and if either the weapon
        // is mobile or we don't care if it's mobile (because we aren't moving).
        if( wpn.loaded(unit) && (!moved || wpn.canFireAfterMoving) )
        {
          UnitContext uc = new UnitContext(gameMap, unit, wpn, move.xCoord, move.yCoord);
          ArrayList<XYCoord> locations = Utils.findTargetsInRange(gameMap, uc, includeTerrain);
          targetLocs.addAll(locations);
        }
      } // ~Weapon loop
    }
    targetLocs.remove(start); // No attacking your own position.
    return targetLocs;
  }

  /**
   * Finds a kill on the designated unit, if available; considers the cheapest units first.
   * <p>Does not consider attacks from fogged tiles.
   * <p>Prunes any unnecessary attacks and empty spaces from the return value.
   * <p>Does not consider launchable units
   * @param target Your victim
   * @param attackCandidates The units you are willing and able to commit
   * @param excludedSpaces Any tiles you don't want to consider attacks from
   * @return The lethal combination of units organized by strike location, or null on failure.
   */
  public static HashMap<XYCoord, Unit> findMultiHitKill(
                                   GameMap gameMap, Unit target,
                                   Collection<Unit> attackCandidates,
                                   Collection<XYCoord> excludedSpaces)
  {
    if( target.getHP() < 1 ) // Try not to pick fights with zombies
      return null;
    if( attackCandidates.size() < 1 )
      return null;

    Commander co = attackCandidates.iterator().next().CO;

    final int minRange = 1;
    final XYCoord targetCoord = new XYCoord(target);
    HashSet<XYCoord> coordsToCheck = new HashSet<XYCoord>(
        Utils.findLocationsInRange(gameMap,
                                   targetCoord,
                                   minRange, findMaxMobileWeaponRange(co))
        );

    // Consider the cheapest units first.
    PriorityQueue<Unit> attackers = new PriorityQueue<Unit>(11, new AIUtils.UnitCostComparator(true));
    attackers.addAll(attackCandidates);

    // Add the current space of any siege units in range
    for( Unit u : attackCandidates )
    {
      if( !u.model.hasImmobileWeapon() )
        continue;

      final XYCoord attackerCoord = new XYCoord(u);
      boolean requiresMoving = false;
      int dist = targetCoord.getDistance(attackerCoord);
      if( !u.canAttack(gameMap, target.model, dist, requiresMoving) )
        continue;

      coordsToCheck.add(attackerCoord);
    }

    // Cull spaces we can't use
    for( XYCoord xyc : coordsToCheck.toArray(new XYCoord[0]) )
    {
      if( gameMap.isLocationFogged(xyc) )
      {
        coordsToCheck.remove(xyc);
        continue;
      }

      MapLocation loc = gameMap.getLocation(xyc);
      Unit resident = loc.getResident();
      if( null != resident && (resident.CO != co || resident.isTurnOver) )
      {
        coordsToCheck.remove(xyc);
        continue;
      }

      if( excludedSpaces.contains(xyc) )
      {
        coordsToCheck.remove(xyc);
        continue;
      }

      boolean canReach = false;
      for( Unit u : attackCandidates )
      {
        if( !u.model.hasMobileWeapon() )
          continue;

        canReach |= Utils.findShortestPath(u, xyc, gameMap).getPathLength() > 1;
        if( canReach )
          break;
      }
      if( !canReach )
      {
        coordsToCheck.remove(xyc);
        continue;
      }
    }

    HashMap<XYCoord, Unit> neededAttacks = new HashMap<XYCoord, Unit>();
    // Figure out where we can attack from
    for( XYCoord xyc : coordsToCheck )
    {
      neededAttacks.put(xyc, null);
    }

    double damage = findMultiHitKill(gameMap, target, attackers, neededAttacks, 0);
    if( damage >= target.getHP() )
    {
      // Prune excess attacks and empty attacking spaces
      for( XYCoord space : new ArrayList<XYCoord>(neededAttacks.keySet()) )
      {
        Unit attacker = neededAttacks.get(space);
        if( null == attacker )
        {
          neededAttacks.remove(space);
          continue;
        }
        double thisShot =
            CombatEngine.simulateBattleResults(attacker, target, gameMap,
                                               space.xCoord, space.yCoord).defender.getPreciseHPDamage();
        if( target.getHP() <= damage - thisShot )
        {
          neededAttacks.remove(space);
          damage -= thisShot;
        }
      }

      return neededAttacks;
    }

    return null;
  }

  /**
   * Attempts to find a combination of attacks that will create a kill.
   * Considers units in the order provided.
   * Recursive.
   * @param attackCandidates The set of potential attackers
   * @param neededAttacks The set of locations to consider, pre-populated with any mandatory attacks, to be populated
   * @param pDamage The cumulative base damage done by those mandatory attacks
   * @return The cumulative base damage of all attacks already in the neededAttacks
   */
  public static double findMultiHitKill(
                                GameMap gameMap, Unit target,
                                Collection<Unit> attackCandidates,
                                Map<XYCoord, Unit> neededAttacks,
                                double pDamage)
  {
    // Base case; we found a kill
    if( pDamage >= target.getPreciseHP() )
    {
      return pDamage;
    }

    double damage = pDamage;
    // Iterate through the attack spaces, and try filling all spaces recursively from each one
    for( XYCoord xyc : neededAttacks.keySet() )
    {
      // Don't try to attack from the same space twice.
      if( null != neededAttacks.get(xyc) )
        continue;

      Queue<Unit> assaultQueue = new ArrayDeque<Unit>(attackCandidates);
      while (!assaultQueue.isEmpty())
      {
        Unit unit = assaultQueue.poll();
        boolean requiresMoving = !xyc.equals(target.x, target.y);
        int dist = xyc.getDistance(target.x, target.y);
        if( !unit.canAttack(gameMap, target.model, dist, requiresMoving) )
          continue; // Consider only units that can attack from here
        if( neededAttacks.containsValue(unit) )
          continue; // Consider each unit only once

        // Figure out how to get here.
        GamePath movePath = Utils.findShortestPath(unit, xyc, gameMap);

        if( movePath.getPathLength() > 0 )
        {
          neededAttacks.put(xyc, unit);
          double thisDamage = CombatEngine.simulateBattleResults(unit, target, gameMap, xyc.xCoord, xyc.yCoord).defender.getPreciseHPDamage();

          thisDamage = findMultiHitKill(gameMap, target, attackCandidates, neededAttacks, damage + thisDamage);

          // If we've found a kill, we're done
          if( thisDamage >= target.getPreciseHP() )
          {
            damage = thisDamage;
            break;
          }
          else // Otherwise, remove the attacker from the slot to make room for the next calculation
          {
            neededAttacks.put(xyc, null);
          }
        }
      }
    }

    return damage;
  }



}
