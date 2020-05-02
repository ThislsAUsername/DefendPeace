package Engine.Combat;

import Terrain.Environment;
import Units.Unit;
import Units.WeaponModel;

/**
 * Utility struct used to facilitate calculating battle results.
 * Parameters are public to allow modification in Commander.applyCombatModifiers()
 * One BattleParams is a single attack from a single unit; any counterattack is a second instance. 
 */
public class BattleParams
{
  public final Unit attacker, defender; 
  public final CombatContext combatRef; // strictly for reference
  public double baseDamage;
  public double attackerHP;
  public double attackFactor;
  public double defenderHP;
  public double defenseFactor;
  public double terrainDefense;
  public final boolean isCounter;

  public static BattleParams getAttack(final CombatContext ref)
  {
    return new BattleParams(ref.attacker, ref.attackerWeapon, ref.defender, ref.gameMap.getEnvironment(ref.defenderX, ref.defenderY), false, ref);
  }
  public static BattleParams getCounterAttack(final CombatContext ref)
  {
    return new BattleParams(ref.defender, ref.defenderWeapon, ref.attacker, ref.gameMap.getEnvironment(ref.attackerX, ref.attackerY), true, ref);
  }

  public BattleParams(Unit attacker, WeaponModel attackerWeapon, Unit defender, Environment battleground, boolean isCounter, final CombatContext ref)
  {
    this.attacker = attacker;
    this.defender = defender;
    baseDamage = attackerWeapon.getDamage(defender.model);
    attackFactor = attacker.model.getDamageRatio();
    attackerHP = attacker.getHP();
    defenseFactor = defender.model.getDefenseRatio();
    defenderHP = defender.getHP();
    terrainDefense = 0;
    this.isCounter = isCounter;
    combatRef = ref;
    // Air units shouldn't get terrain defense
    if( defender.model.isAirUnit() )
    {
      // getDefLevel returns the number of terrain stars. Since we're using %Def, we need to multiply by 10. However, we do that when we multiply by HP in calculateDamage.
      terrainDefense = battleground.terrainType.getDefLevel();
    }
  }

  public double calculateDamage()
  {
    //    [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
    double overallPower = (baseDamage * attackFactor / 100/*+Random factor?*/) * attackerHP / 10;
    double overallDefense = ((200 - (defenseFactor + terrainDefense * defenderHP)) / 100);
    return overallPower * overallDefense / 10; // original formula was % damage, now it must be HP of damage
  }
}
