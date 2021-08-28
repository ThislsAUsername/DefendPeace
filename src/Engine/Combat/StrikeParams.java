package Engine.Combat;

import Terrain.GameMap;

/**
 * Utility struct used to facilitate calculating battle results.
 * Parameters are public to allow modification in Commander.applyCombatModifiers()
 * One BattleParams is a single attack from a single unit; any counterattack is a second instance. 
 */
public class StrikeParams
{
  public final Combatant attacker;
  public final GameMap map; // for reference, not weirdness

  // Stuff inherited for reference from CombatContext
  public final int battleRange;

  public double baseDamage;
  public double attackerHP;
  public double attackPower;

  public final boolean isCounter;
  protected boolean isSim; // Don't really want COs acting differently between sim and "real life"

  public double luckMax = 0;
  public double dispersion = 0;

  public double attackerTerrainStars = 0;
  public double terrainStars = 0;

  public double defenderHP = 0;
  public double defensePower = 100;
  public boolean applyTerrain = false;

  public static BattleParams getAttack(final CombatContext ref, boolean isSim)
  {
    return new BattleParams(
        new Combatant(ref.attacker, ref.attackerWeapon, ref.attackerX, ref.attackerY),
        new Combatant(ref.defender, ref.defenderWeapon, ref.defenderX, ref.defenderY),
        ref.gameMap, ref.battleRange,
        ref.attacker.model.getDamageRatio(), ref.attacker.getHP(),
        ref.defender.model.getDefenseRatio(), ref.defenderTerrainStars, ref.attackerTerrainStars,
        false, isSim);
  }
  public static BattleParams getCounterAttack(final CombatContext ref, double counterHP, boolean isSim)
  {
    return new BattleParams(
        new Combatant(ref.defender, ref.defenderWeapon, ref.defenderX, ref.defenderY),
        new Combatant(ref.attacker, ref.attackerWeapon, ref.attackerX, ref.attackerY),
        ref.gameMap, ref.battleRange,
        ref.defender.model.getDamageRatio(), counterHP,
        ref.attacker.model.getDefenseRatio(), ref.attackerTerrainStars, ref.defenderTerrainStars,
        true, isSim);
  }

  public StrikeParams(
      Combatant attacker,
      GameMap map, int battleRange,
      double attackPower, double attackerHP,
      double baseDamage, double attackerTerrainStars,
      boolean isCounter, boolean isSim)
  {
    this.attacker = attacker;
    this.map = map;

    this.battleRange = battleRange;
    this.attackPower = attackPower + attacker.body.CO.getTowerBoost();
    this.isCounter = isCounter;
    this.isSim = isSim;
    this.baseDamage = baseDamage;

    this.attackerTerrainStars = attackerTerrainStars;
    this.attackerHP = attackerHP;

    // Apply any last-minute adjustments.
    attacker.body.CO.modifyUnitAttack(this);
  }

  public double calculateDamage()
  {
    double stars = 0;
    if (applyTerrain)
      stars = terrainStars;
    //    [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
    double overallPower = (baseDamage * attackPower / 100) * attackerHP / 10;
    double overallDefense = ((200 - (defensePower + stars * defenderHP)) / 100);
    if( ! isSim )
    {
      overallPower += (int) (Math.random() * luckMax) * (attackerHP / 10);
      overallPower -= (int) (Math.random() * dispersion) * (attackerHP / 10);
      if( overallPower < 0 )
        overallPower = 0.000000000001;
    }
    return overallPower * overallDefense / 10 ; // original formula was % damage, now it must be HP of damage
  }

  public static class BattleParams extends StrikeParams
  {
    public final Combatant defender;

    public BattleParams(
        Combatant attacker, Combatant defender,
        GameMap map, int battleRange,
        double attackPower, double attackerHP,
        double defensePower, double terrainStars, double attackerTerrainStars,
        boolean isCounter, boolean isSim)
    {
      super(attacker,
          map, battleRange,
          attackPower, attackerHP,
          (null == attacker.gun)? 0 : attacker.gun.getDamage(defender.body.model), attackerTerrainStars,
          isCounter, isSim);
      this.defender = defender;

      this.defensePower = defensePower + defender.body.CO.getTowerDefBoost();
      this.terrainStars = terrainStars;
      defenderHP = defender.body.getHP();
      applyTerrain = !defender.body.model.isAirUnit();

      luckMax = 10;

      // Apply any last-minute adjustments.
      attacker.body.CO.modifyUnitAttackOnUnit(this);
      defender.body.CO.modifyUnitDefenseAgainstUnit(this);
    }
  }
}
