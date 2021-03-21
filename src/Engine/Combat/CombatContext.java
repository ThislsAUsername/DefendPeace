package Engine.Combat;

import java.util.List;

import Engine.UnitMods.UnitModifier;
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
  public final List<UnitModifier> attackerMods, defenderMods;
  
  public CombatContext(GameMap map,
      Unit pAttacker, WeaponModel attackerWep, List<UnitModifier> attackerMods,
      Unit pDefender, WeaponModel defenderWep, List<UnitModifier> defenderMods,
      int pBattleRange, int attackerX, int attackerY)
  {
    attacker = pAttacker;
    defender = pDefender;
    this.attackerX = attackerX;
    this.attackerY = attackerY;
    defenderX = defender.x; // This variable is technically not necessary, but provides consistent names with attackerX/Y.
    defenderY = defender.y;
    this.attackerMods = attackerMods;
    this.defenderMods = defenderMods;
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

    // apply modifiers...
    for(UnitModifier mod : attackerMods)
      mod.changeCombatContext(this);
    for(UnitModifier mod : defenderMods)
      mod.changeCombatContext(this);
  }
}
