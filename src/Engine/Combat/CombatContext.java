package Engine.Combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.UnitContext;
import lombok.var;

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

  /**
   * Since the simple attacker/defender handling logic is inflexible, let's get a real time/initiative system in here.
   * <p>Damage from attacks will apply at the end of the *next* timepoint, though it could be amusing to have slower/faster attacks.
   */
  public static enum InitiativeType
  {
    // Negative initiative is dumb
    COUNTER_BREAK,     // 0: Counter Break
    WEIRD_1, WEIRD_2,  // 1,2: unused, since they only exist to interact weirdly with Counter Break/init 4
    FIRSTSTRIKE,       // 3: normal firststrike
    FIRSTSTRIKE_SIMUL, // 4: simultaneous with normal firststrike, but hits before a normal counterattack fires
    COUNTER_SIMUL,     // 5: fires after normal firststrike hits, but simultaneous with a normal counterattack
    COUNTER,           // 6: normal counterattack
    // 7: "why"
    // 8+: fires after a normal counterattack hits
  };

  public UnitContext attacker, defender;
  // We cannot assume that the attacker and defender contexts have a real unit to act as their ID, so we have to just track the attacks separately.
  // Each UC needs to be an independent clone, so that they can have their HP and ammo counts modified separately
  public HashMap<Integer, UnitContext> timeStepToAttack = new HashMap<>(), timeStepToCounter = new HashMap<>();
  public final GameInstance gameInstance; // For randomness; only needed when doing true combat calcs
  public final GameMap gameMap; // for reference, not weirdness
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

    timeStepToAttack.put(InitiativeType.FIRSTSTRIKE.ordinal(), new UnitContext(attacker));

    // Only attacks at point-blank range can be countered
    if( (1 == battleRange) && (null != defender.weapon) )
    {
      timeStepToCounter.put(InitiativeType.COUNTER.ordinal(), new UnitContext(defender));
    }
  }
  public CombatContext(CombatContext other)
  {
    attacker = other.attacker;
    defender = other.defender;
    gameInstance = other.gameInstance;
    gameMap = other.gameMap;
    for( var tsToUC : other.timeStepToAttack.entrySet() )
      timeStepToAttack.put(tsToUC.getKey(), new UnitContext(tsToUC.getValue()));
    for( var tsToUC : other.timeStepToCounter.entrySet() )
      timeStepToCounter.put(tsToUC.getKey(), new UnitContext(tsToUC.getValue()));
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

  private BattleParams buildBattleParams(UnitContext aClone, UnitContext dClone, boolean isCounter)
  {
    return StrikeParams.buildBattleParams(aClone, dClone, this, isCounter);
  }
}
