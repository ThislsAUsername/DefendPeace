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
  public boolean attackerMoved;
  public int battleRange;
  
  public CombatContext(GameMap map,
      Unit pAttacker, WeaponModel attackerWep, List<UnitModifier> attackerMods,
      Unit pDefender, WeaponModel defenderWep, List<UnitModifier> defenderMods,
      int pBattleRange, int attackerX, int attackerY)
  {
    attacker = new UnitContext(map, pAttacker, attackerWep, attackerX, attackerY);
    attacker.mods.clear();
    attacker.mods.addAll(attackerMods);

    defender = new UnitContext(map, pDefender, defenderWep, pDefender.x, pDefender.y);
    defender.mods.clear();
    defender.mods.addAll(defenderMods);

    gameMap = map;
    attackerMoved = pAttacker.x != attackerX || pAttacker.y != attackerY;
    battleRange = pBattleRange;
    if( null != defenderWep )
    {
      canCounter = true;
    }

    // Make local shallow copies to avoid funny business
    List<UnitModifier> aMods = new ArrayList<UnitModifier>(attackerMods);
    List<UnitModifier> dMods = new ArrayList<UnitModifier>(defenderMods);
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

  public BattleParams getCounterAttack(double damageDealt)
  {
    if( !canCounter )
      return null;

    UnitContext aClone = new UnitContext(defender);
    aClone.damageHP(damageDealt);

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
