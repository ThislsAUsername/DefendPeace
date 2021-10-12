package Engine.Combat;

import java.util.ArrayList;
import java.util.List;

import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitContext;
import Units.WeaponModel;

/**
 * CombatContext exists to allow COs to modify the fundamental parameters of an instance of combat.
 */
public class CombatContext
{
  public UnitContext attacker, defender;
  public final GameMap gameMap; // for reference, not weirdness
  public boolean canCounter = false;
  public int battleRange;

  public static CombatContext build(GameMap map,
      Unit pAttacker, WeaponModel attackerWep, List<UnitModifier> attackerMods,
      Unit pDefender, WeaponModel defenderWep, List<UnitModifier> defenderMods,
      int pBattleRange, int attackerX, int attackerY)
  {
    CombatContext c = new CombatContext(map,
        new UnitContext(map, pAttacker, attackerWep, attackerX, attackerY),
        new UnitContext(map, pDefender, defenderWep, pDefender.x, pDefender.y),
        pBattleRange);
    c.attacker.mods.clear();
    c.attacker.mods.addAll(attackerMods);
    c.defender.mods.clear();
    c.defender.mods.addAll(defenderMods);

    c.applyModifiers();
    return c;
  }

  public static CombatContext build(GameMap map,
                                    UnitContext pAttacker,
                                    UnitContext pDefender,
                                    int pBattleRange)
  {
    CombatContext c = new CombatContext(map, pAttacker, pDefender, pBattleRange);

    c.applyModifiers();
    return c;
  }

  private CombatContext(GameMap map,
      UnitContext pAttacker,
      UnitContext pDefender,
      int pBattleRange)
  {
    attacker = pAttacker;
    defender = pDefender;

    gameMap = map;
    battleRange = pBattleRange;

    if ( map.isLocationValid(attacker.coord))
    {
      attacker.setEnvironment(map.getEnvironment(attacker.coord));
    }
    if ( map.isLocationValid(defender.coord))
    {
      defender.setEnvironment(map.getEnvironment(defender.coord));
    }

    if( null == attacker.weapon )
    {
      attacker.chooseWeapon(defender.model, battleRange);
    }
    if( null == defender.weapon )
    {
      defender.chooseWeapon(attacker.model, battleRange);
    }

    // Only attacks at point-blank range can be countered
    if( (1 == battleRange) && (null != defender.weapon) )
    {
      canCounter = true;
    }

    if( attacker.mods.isEmpty() )
      attacker.mods.addAll(attacker.unit.getModifiers());
    if( defender.mods.isEmpty() )
      defender.mods.addAll(defender.unit.getModifiers());
  }

  private void applyModifiers()
  {
    // Make local shallow copies to avoid funny business
    List<UnitModifier> aMods = new ArrayList<>(attacker.mods);
    List<UnitModifier> dMods = new ArrayList<>(defender.mods);
    // apply modifiers...
    for( UnitModifier mod : aMods )
      mod.changeCombatContext(this);
    for( UnitModifier mod : dMods )
      mod.changeCombatContext(this);
  }

  public BattleParams getAttack()
  {
    UnitContext aClone = new UnitContext(attacker);
    UnitContext dClone = new UnitContext(defender);

    return buildBattleParams(aClone, dClone, false);
  }

  public BattleParams getCounterAttack(double damageDealt, boolean isSim)
  {
    if( !canCounter )
      return null;

    UnitContext aClone = new UnitContext(defender);
    aClone.damageHP(damageDealt, isSim);

    // If the counterattacker is dead, there's no counterattack
    if( 1 > aClone.getHP() )
      return null;

    UnitContext dClone = new UnitContext(attacker);

    return buildBattleParams(aClone, dClone, true);
  }

  private BattleParams buildBattleParams(UnitContext aClone, UnitContext dClone, boolean isCounter)
  {
    return StrikeParams.buildBattleParams(aClone, dClone, gameMap, battleRange, isCounter);
  }
}
