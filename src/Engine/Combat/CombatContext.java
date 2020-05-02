package Engine.Combat;

import Terrain.GameMap;
import Units.Unit;
import Units.WeaponModel;

/**
 * CombatContext exists to allow COs to modify the fundamental parameters of an instance of combat.
 */
public class CombatContext
{
  public Unit attacker, defender;
  public WeaponModel attackerWeapon = null, defenderWeapon = null;
  public int attackerX, attackerY, defenderX, defenderY;
  public final GameMap gameMap; // for reference, not weirdness
  public boolean canCounter = false;
  public boolean attackerMoved;
  public int battleRange;
  
  public CombatContext(GameMap map, Unit pAttacker, WeaponModel attackerWep, Unit pDefender, WeaponModel defenderWep, int pBattleRange, int attackerX, int attackerY)
  {
    attacker = pAttacker;
    defender = pDefender;
    this.attackerX = attackerX;
    this.attackerY = attackerY;
    defenderX = defender.x; // This variable is technically not necessary, but provides consistent names with attackerX/Y.
    defenderY = defender.y;
    gameMap = map;
    attackerMoved = pAttacker.x != attackerX || pAttacker.y != attackerY;
    battleRange = pBattleRange;
    attackerWeapon = attackerWep;
    defenderWeapon = defenderWep;
    if( null != defenderWeapon )
    {
      canCounter = true;
    }
  }
}
