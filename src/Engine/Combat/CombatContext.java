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
  public double attackerTerrainStars, defenderTerrainStars;
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

    // Air units shouldn't get terrain defense
    // getDefLevel returns the number of terrain stars. Since we're using %Def, we need to multiply by 10. However, we do that when we multiply by HP in calculateDamage.
    if( !attacker.model.isAirUnit() )
      attackerTerrainStars = map.getEnvironment(attackerX, attackerY).terrainType.getDefLevel();
    if( !defender.model.isAirUnit() )
      defenderTerrainStars = map.getEnvironment(defenderX, defenderY).terrainType.getDefLevel();

    // let the COs fool around with anything they want...
    pAttacker.CO.changeCombatContext(this);
    pDefender.CO.changeCombatContext(this);
  }
}
