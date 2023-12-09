package Engine.Combat;

import java.util.ArrayList;
import java.util.List;

import Engine.GameInstance;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
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
    attacker = pAttacker;
    defender = pDefender;

    gameInstance = gi;
    gameMap = map;
    calcType = pCalcType;
    if (null == gameInstance && !calcType.isSim())
      throw new IllegalArgumentException("Caller requires true game results but did not provide a GameInstance.");

    int attackerX = attacker.coord.x;
    int attackerY = attacker.coord.y;
    int defenderX = defender.coord.x;
    int defenderY = defender.coord.y;

    battleRange = Math.abs(attackerX - defenderX) + Math.abs(attackerY - defenderY);

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

  /**
   * Call during combat calculations
   */
  public void applyModifiers()
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
    aClone.damageHP(damageDealt, isSim);

    // If the counterattacker is dead, there's no counterattack
    if( 1 > aClone.getHP() )
      return null;

    UnitContext dClone = new UnitContext(attacker);

    return buildBattleParams(aClone, dClone, true);
  }

  private BattleParams buildBattleParams(UnitContext aClone, UnitContext dClone, boolean isCounter)
  {
    return StrikeParams.buildBattleParams(aClone, dClone, this, isCounter);
  }
}
