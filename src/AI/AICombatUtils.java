package AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.Combat.StrikeParams;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
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
    Location targetLoc = map.getLocation(action.getTargetLocation());
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
    Map<XYCoord, Double> shootableTiles = new HashMap<XYCoord, Double>();
    boolean includeOccupiedDestinations = true; // We assume the enemy knows how to manage positioning within his turn
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedDestinations);
    for( WeaponModel wep : unit.model.weapons )
    {
      double damage = (null == target)? 1 : wep.getDamage(target) * unit.getHPFactor();
      if( damage > 0 )
      {
        if( !wep.canFireAfterMoving )
        {
          for (XYCoord xyc : Utils.findLocationsInRange(gameMap, origin, wep.minRange, wep.maxRange))
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
            for (XYCoord xyc : Utils.findLocationsInRange(gameMap, dest, wep.minRange, wep.maxRange))
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
  public static int findMaxStrikeWeaponRange(Commander co)
  {
    int range = 0;
    for( UnitModel um : co.unitModels )
    {
      for( WeaponModel wm : um.weapons )
      {
        if( wm.canFireAfterMoving )
          range = Math.max(range, wm.maxRange);
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
          ArrayList<XYCoord> locations = Utils.findTargetsInRange(gameMap, unit.CO, move, wpn, includeTerrain);
          targetLocs.addAll(locations);
        }
      } // ~Weapon loop
    }
    targetLocs.remove(start); // No attacking your own position.
    return targetLocs;
  }

}
