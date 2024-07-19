package Engine.Combat;

import java.util.ArrayList;
import java.util.List;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.UnitContext;

/**
 * CombatContext exists to allow COs to modify the fundamental parameters of an instance of combat.
 */
public class CombatContext
{
  public static enum CalcType
  {
    NO_LUCK, PESSIMISTIC, OPTIMISTIC, COMBAT, DEMOLITION;

    public boolean isSim()
    {
      return this == NO_LUCK || this == PESSIMISTIC || this == OPTIMISTIC;
    };
  };

  public UnitContext attacker, defender;
  public final GameInstance gameInstance; // For randomness; only needed when doing true combat calcs
  public final GameMap gameMap; // for reference, not weirdness
  public boolean canCounter = false;
  public int battleRange;
  public CalcType calcType;

  public CombatContext(GameInstance gi, GameMap map,
                        UnitContext pAttacker, UnitContext pDefender,
                        CalcType pCalcType)
  {
    this(gi, map, pAttacker, pDefender, calcBattleRange(pAttacker, pDefender), pCalcType);
  }
  public CombatContext(GameInstance gi, GameMap map,
                        UnitContext pAttacker, UnitContext pDefender, int battleRange,
                        CalcType pCalcType)
  {
    attacker = pAttacker;
    defender = pDefender;

    gameInstance = gi;
    gameMap = map;
    calcType = pCalcType;
    if (null == gameInstance && !calcType.isSim())
      throw new IllegalArgumentException("Caller requires true game results but did not provide a GameInstance.");

    this.battleRange = battleRange;
    setTowerCounts(map, attacker);
    setTowerCounts(map, defender);

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
  }
  public CombatContext(CombatContext other)
  {
    attacker = other.attacker;
    defender = other.defender;
    gameInstance = other.gameInstance;
    gameMap = other.gameMap;
    canCounter = other.canCounter;
    battleRange = other.battleRange;
    calcType = other.calcType;
  }
  private static int calcBattleRange(UnitContext attacker, UnitContext defender)
  {
    int battleRange;
    int attackerX = attacker.coord.x;
    int attackerY = attacker.coord.y;
    int defenderX = defender.coord.x;
    int defenderY = defender.coord.y;

    battleRange = Math.abs(attackerX - defenderX) + Math.abs(attackerY - defenderY);
    return battleRange;
  }

  /**
   * Call during combat calculations
   */
  public CombatContext applyModifiers()
  {
    // Make local shallow copies to avoid funny business
    List<UnitModifier> aMods = new ArrayList<>(attacker.mods);
    List<UnitModifier> dMods = new ArrayList<>(defender.mods);
    // apply modifiers...
    for( UnitModifier mod : aMods )
      mod.changeCombatContext(this, attacker);
    for( UnitModifier mod : dMods )
      mod.changeCombatContext(this, defender);
    return this;
  }

  /**
   * For when you want to confuse who started the fight
   */
  public void swapCombatants()
  {
    UnitContext minion = defender;

    defender = attacker;
    attacker = minion;

    // Since we're swapping the combatants, we also need to swap the prediction polarity
    if( calcType == CalcType.PESSIMISTIC )
      calcType = CalcType.OPTIMISTIC;
    else if( calcType == CalcType.OPTIMISTIC )
      calcType = CalcType.PESSIMISTIC;
  }

  public static void setTowerCounts(GameMap map, UnitContext uc)
  {
    // Count number of towers, and apply new modifiers
    final int minX = 0;
    final int minY = 0;
    final int maxX = map.mapWidth  - 1;
    final int maxY = map.mapHeight - 1;

    uc.towerCountDoR = 0;
    uc.towerCountDS  = 0;
    for( int y = minY; y <= maxY; y++ ) // Top to bottom, left to right
    {
      for( int x = minX; x <= maxX; x++ )
      {
        MapLocation loc = map.getLocation(x, y);
        Commander owner = loc.getOwner();
        if( null == owner || uc.CO.army != owner.army )
          continue;
        if( loc.getEnvironment().terrainType.equals(TerrainType.DOR_TOWER) )
          uc.towerCountDoR += 1;
        if( loc.getEnvironment().terrainType.equals(TerrainType.DS_TOWER) )
          uc.towerCountDS += 1;
      }
    }
  }

  public BattleParams getAttack()
  {
    UnitContext aClone = new UnitContext(attacker);
    UnitContext dClone = new UnitContext(defender);

    return buildBattleParams(aClone, dClone, false);
  }

  public BattleParams getCounterAttack(int damageDealt, boolean isSim)
  {
    if( !canCounter )
      return null;

    UnitContext aClone = new UnitContext(defender);
    aClone.damageHealth(damageDealt, isSim);

    // If the counterattacker is dead, there's no counterattack
    if( 1 > aClone.getHealth() )
      return null;

    UnitContext dClone = new UnitContext(attacker);

    return buildBattleParams(aClone, dClone, true);
  }

  private BattleParams buildBattleParams(UnitContext aClone, UnitContext dClone, boolean isCounter)
  {
    return StrikeParams.buildBattleParams(aClone, dClone, this, isCounter);
  }
}
