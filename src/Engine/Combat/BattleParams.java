package Engine.Combat;

import Terrain.GameMap;

/**
 * Utility struct used to facilitate calculating battle results.
 * Parameters are public to allow modification in Commander.applyCombatModifiers()
 * One BattleParams is a single attack from a single unit; any counterattack is a second instance. 
 */
public class BattleParams
{
  public final Combatant attacker;
  public final Combatant defender;

  // Stuff inherited for reference from CombatContext
  public final GameMap map; // for reference, not weirdness
  public final int battleRange;

  public double baseDamage;
  public double attackerHP;
  public double attackPower;
  public double defenderHP;
  public double defensePower;
  public double terrainStars;
  public final boolean isCounter;

  public static BattleParams getAttack(final CombatContext ref)
  {
    return new BattleParams(
        new Combatant(ref.attacker, ref.attackerWeapon, ref.attackerX, ref.attackerY),
        new Combatant(ref.defender, ref.defenderWeapon, ref.defenderX, ref.defenderY),
        ref.gameMap, ref.battleRange,
        ref.attacker.model.getDamageRatio(),
        ref.defender.model.getDefenseRatio(), ref.defenderTerrainStars,
        false);
  }
  public static BattleParams getCounterAttack(final CombatContext ref)
  {
    return new BattleParams(
        new Combatant(ref.defender, ref.defenderWeapon, ref.defenderX, ref.defenderY),
        new Combatant(ref.attacker, ref.attackerWeapon, ref.attackerX, ref.attackerY),
        ref.gameMap, ref.battleRange,
        ref.defender.model.getDamageRatio(),
        ref.attacker.model.getDefenseRatio(), ref.attackerTerrainStars,
        true);
  }

  public BattleParams(
      Combatant attacker, Combatant defender,
      GameMap map, int battleRange,
      double attackPower,
      double defensePower, double terrainStars,
      boolean isCounter)
  {
    this.attacker = attacker;
    this.defender = defender;
    this.map = map;

    this.battleRange = battleRange;
    this.attackPower = attackPower;
    this.isCounter = isCounter;
    baseDamage = attacker.gun.getDamage(defender.body.model);

    this.defensePower = defensePower;
    this.terrainStars = terrainStars;
    attackerHP = attacker.body.getHP();
    defenderHP = defender.body.getHP();

    // Apply any last-minute adjustments.
    attacker.body.CO.buffAttack(this);
    defender.body.CO.buffDefense(this);
  }

  public double calculateDamage()
  {
    //    [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
    double overallPower = (baseDamage * attackPower / 100/*+Random factor?*/) * attackerHP / 10;
    double overallDefense = ((200 - (defensePower + terrainStars * defenderHP)) / 100);
    return overallPower * overallDefense / 10; // original formula was % damage, now it must be HP of damage
  }
}
