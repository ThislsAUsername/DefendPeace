package Engine.Combat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import Engine.Combat.StrikeParams.BattleParams;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.WeaponModel;

/**
 * CombatEngine serves as the general-purpose interface into the combat calculation logic.
 */
public class CombatEngine
{
  /**
   * Static function to calculate the outcome of a battle between two units. It builds an
   * object to represent the specific combat instance and returns the result it calculates.
   * Requires perfect map info just in case the COs need to get weird.
   */
  public static BattleSummary calculateBattleResults( Unit attacker, Unit defender, MapMaster map, int attackerX, int attackerY )
  {
    return calculateBattleResults(attacker, defender, map, attackerX, attackerY, false);
  }

  /**
   * Assuming Commanders get weird, this allows for you to check the results of combat without perfect map info.
   * This also provides un-capped damage estimates, so perfect HP info isn't revealed by the map.
   */
  public static BattleSummary simulateBattleResults( Unit attacker, Unit defender, GameMap map, int attackerX, int attackerY )
  {
    return calculateBattleResults(attacker, defender, map, attackerX, attackerY, true);
  }

  /**
   * Calculate and return the results of a battle.
   * This will not actually apply the damage taken; this is done later in BattleEvent.
   * Note that we assume the defender is where he thinks he is, but the attacker may move before attacking, so we take that coordinate explicitly.
   * @param isSim Determines whether to cap damage at the HP of the victim in question
   * @return A BattleSummary object containing all relevant details from this combat instance.
   */
  public static BattleSummary calculateBattleResults(Unit attacker, Unit defender, GameMap map, int attackerX, int attackerY, boolean isSim)
  {
    int defenderX = defender.x; // This variable is technically not necessary, but provides consistent names with attackerX/Y.
    int defenderY = defender.y;
    boolean attackerMoved = attacker.x != attackerX || attacker.y != attackerY;
    int battleRange = Math.abs(attackerX - defenderX) + Math.abs(attackerY - defenderY);
    WeaponModel attackerWeapon = attacker.chooseWeapon(defender.model, battleRange, attackerMoved);
    WeaponModel defenderWeapon = null;
    // Only attacks at point-blank range can be countered
    if( 1 == battleRange )
      defenderWeapon = defender.chooseWeapon(attacker.model, battleRange, false);

    CombatContext context = new CombatContext(map, attacker, attackerWeapon, defender, defenderWeapon, battleRange, attackerX, attackerY);
    
    // If the attacker and defender get swapped, we want to still give a coherent BattleSummary.
    Map<Unit, Entry<WeaponModel,Double>> unitDamageMap = new HashMap<Unit, Entry<WeaponModel,Double>>();
    unitDamageMap.put(attacker, new AbstractMap.SimpleEntry<WeaponModel,Double>(attackerWeapon, 0.0));
    unitDamageMap.put(defender, new AbstractMap.SimpleEntry<WeaponModel,Double>(defenderWeapon, 0.0));

    // From here on in, use context variables only

    double attackerHPLoss = 0;

    // Set up our scenario.
    BattleParams attackInstance = StrikeParams.getAttack(context);

    double defenderHPLoss = attackInstance.calculateDamage();
    unitDamageMap.put(context.attacker, new AbstractMap.SimpleEntry<WeaponModel,Double>(context.attackerWeapon, defenderHPLoss));
    if( !isSim && defenderHPLoss > context.defender.getPreciseHP() ) defenderHPLoss = context.defender.getPreciseHP();

    // If the unit can counter, and wasn't killed in the initial volley, calculate return damage.
    if( context.canCounter && (context.defender.getPreciseHP() > defenderHPLoss) )
    {
      // New battle instance with defender counter-attacking.
      double counterHP = Math.ceil(context.defender.getPreciseHP() - defenderHPLoss); // Account for the first attack's damage to the now-attacker.
      BattleParams defendInstance = StrikeParams.getCounterAttack(context, counterHP);

      attackerHPLoss = defendInstance.calculateDamage();
      unitDamageMap.put(context.defender, new AbstractMap.SimpleEntry<WeaponModel,Double>(context.defenderWeapon, attackerHPLoss));
      if( !isSim && attackerHPLoss > context.attacker.getPreciseHP() ) attackerHPLoss = context.attacker.getPreciseHP();
    }
    
    // Calculations complete. We are setting up our BattleSummary, so go back to using known-accurate info
    return new BattleSummary(attacker, unitDamageMap.get(attacker).getKey(),
                             defender, unitDamageMap.get(defender).getKey(),
                             map.getEnvironment(attackerX, attackerY).terrainType,
                             map.getEnvironment(defenderX, defenderY).terrainType,
                             unitDamageMap.get(defender).getValue(), unitDamageMap.get(attacker).getValue());
  }

  public static double calculateOneStrikeDamage( Unit attacker, int battleRange, Unit defender, GameMap map, int terrainStars, boolean attackerMoved )
  {
    return new BattleParams(
        new Combatant(attacker, attacker.chooseWeapon(defender.model, battleRange, attackerMoved), attacker.x, attacker.y),
        new Combatant(defender, null, defender.x, defender.y),
        map, battleRange,
        attacker.model.getDamageRatio(), attacker.getHP(),
        defender.model.getDefenseRatio(), terrainStars,
        false).calculateDamage();
  }
}
