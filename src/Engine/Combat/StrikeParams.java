package Engine.Combat;

import java.util.ArrayList;
import java.util.List;

import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Units.ITargetable;
import Units.UnitContext;

/**
 * Utility struct used to facilitate calculating battle results.
 * Parameters are public to allow modification by UnitModifiers.
 * One BattleParams is a single attack from a single unit; any counterattack is a second instance. 
 */
public class StrikeParams
{
  public static BattleParams buildBattleParams(
      UnitContext attacker, UnitContext defender,
      GameMap gameMap, int battleRange,
      boolean isCounter)
  {
    List<UnitModifier> aMods = new ArrayList<UnitModifier>(attacker.mods);
    List<UnitModifier> dMods = new ArrayList<UnitModifier>(defender.mods);

    BattleParams params = new BattleParams(buildStrikeParams(attacker, defender.model, gameMap, battleRange, isCounter), defender);

    for( UnitModifier mod : aMods )
      mod.modifyUnitAttackOnUnit(params);
    for( UnitModifier mod : dMods )
      mod.modifyUnitDefenseAgainstUnit(params);

    return params;
  }

  public static StrikeParams buildStrikeParams(
      UnitContext attacker, ITargetable defender,
      GameMap gameMap, int battleRange,
      boolean isCounter)
  {
    List<UnitModifier> aMods = new ArrayList<UnitModifier>(attacker.mods);

    StrikeParams params = new StrikeParams(
        attacker, gameMap,
        battleRange, ( null == attacker.weapon ) ? 0 : attacker.weapon.getDamage(defender),
        isCounter);

    for( UnitModifier mod : aMods )
      mod.modifyUnitAttack(params);

    return params;
  }


  public final UnitContext attacker;
  public final GameMap map; // for reference, not weirdness

  // Stuff inherited for reference from CombatContext
  public final int battleRange;

  public double baseDamage;
  public double attackerHP;
  public double attackPower;
  public final boolean isCounter;

  public double defenderHP = 0;
  public double defensePower = 100;
  public double terrainStars = 0;

  protected StrikeParams(
      UnitContext attacker,
      GameMap map, int battleRange,
      double baseDamage,
      boolean isCounter)
  {
    this.attacker = attacker;
    this.map = map;

    this.battleRange = battleRange;

    this.baseDamage = baseDamage;
    this.attackerHP = attacker.getHP();
    this.attackPower = attacker.attackPower;
    this.isCounter = isCounter;
  }
  protected StrikeParams(StrikeParams other)
  {
    this.attacker = other.attacker;
    this.map = other.map;

    this.battleRange = other.battleRange;

    this.baseDamage = other.baseDamage;
    this.attackerHP = other.attackerHP;
    this.attackPower = other.attackPower;
    this.isCounter = other.isCounter;
  }

  public double calculateDamage()
  {
    //    [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
    double overallPower = (baseDamage * attackPower / 100/*+Random factor?*/) * attackerHP / 10;
    double overallDefense = ((200 - (defensePower + terrainStars * defenderHP)) / 100);
    return overallPower * overallDefense / 10; // original formula was % damage, now it must be HP of damage
  }

  public static class BattleParams extends StrikeParams
  {
    public final UnitContext defender;

    protected BattleParams(
        StrikeParams base,
        UnitContext defender)
    {
      super(base);
      this.defender = defender;

      defenderHP = defender.getHP();
      this.defensePower = defender.defensePower;
      this.terrainStars = defender.terrainStars;
    }
  }
}
