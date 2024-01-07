package Engine.Combat;

import java.util.ArrayList;
import java.util.List;

import Engine.XYCoord;
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
      CombatContext combatContext,
      boolean isCounter)
  {
    List<UnitModifier> aMods = new ArrayList<>(attacker.mods);
    List<UnitModifier> dMods = new ArrayList<>(defender.mods);

    BattleParams params = new BattleParams(
        buildStrikeParams(attacker, defender.model,
                          combatContext.gameMap, combatContext.battleRange,
                          defender.coord,
                          isCounter),
        combatContext,
        defender);

    for( UnitModifier mod : aMods )
      mod.modifyUnitAttackOnUnit(params);
    for( UnitModifier mod : dMods )
      mod.modifyUnitDefenseAgainstUnit(params);

    return params;
  }

  public static StrikeParams buildStrikeParams(
      UnitContext attacker, ITargetable defender,
      GameMap gameMap, int battleRange, XYCoord target,
      boolean isCounter)
  {
    List<UnitModifier> aMods = new ArrayList<>(attacker.mods);

    StrikeParams params = new StrikeParams(
        attacker, gameMap, battleRange,
        target,
        ( null == attacker.weapon ) ? 0 : attacker.weapon.getDamage(defender),
        isCounter);

    for( UnitModifier mod : aMods )
      mod.modifyUnitAttack(params);

    return params;
  }


  public final UnitContext attacker;
  public final GameMap map; // for reference, not weirdness

  // Stuff inherited for reference from CombatContext
  public final int battleRange;

  public int baseDamage;
  public int attackerHealth;
  public int attackPower;
// These three variables are needed to simulate different games' luck implementations - 1 RN vs 2 RNs
  public int luckBase = 0; // Luck value if you roll 0
  public int luckRolled = 0; // The number we plug into the RNG for luck damage
  public int luckRolledBad = 0; // The number we plug into the RNG for negative luck damage
  public final boolean isCounter;
  /**
   * Implies luck doesn't scale down with HP and isn't reduced by CO-based defense.<p>
   * It's worth noting this is based on testing/observation, not reverse engineering.<p>
   * However, it's negated if damage*CO defense == 0<p>
   * It's implemented here by assuming all AW1 CO-based defense is multiplier-based defense.<p>
   * Relevant link: https://forums.warsworldnews.com/viewtopic.php?p=417292&sid=a877b0305a8af6d63956bb893d11cc88#p417292
   */
  public final boolean aw1Luck;

  public int attackerDamageMultiplier = 100;
  public int defenderDamageMultiplier = 100;

  public int defenderHealth = 0;
  public final XYCoord targetCoord;
  public int defenseSubtraction = 100;
  public int defenseDivision    = 100;
  public int terrainStars = 0;
  public boolean terrainGivesSubtraction = true;

  protected StrikeParams(
      UnitContext attacker,
      GameMap map, int battleRange, XYCoord target,
      int baseDamage,
      boolean isCounter)
  {
    this.attacker = attacker;
    this.map = map;

    this.battleRange = battleRange;

    this.baseDamage = baseDamage;
    this.attackerHealth = attacker.getHealth();
    this.attackPower = attacker.attackPower;
    this.luckRolled = attacker.CO.luck;
    this.isCounter = isCounter;
    aw1Luck = attacker.CO.aw1Combat;
    if( aw1Luck && isCounter ) // Intended special case; AW1 counters don't round HP and don't get luck damage.
      this.attackerHealth = attacker.health;

    this.targetCoord = target;
  }
  protected StrikeParams(StrikeParams other)
  {
    this.attacker = other.attacker;
    this.map = other.map;

    this.battleRange = other.battleRange;

    this.baseDamage = other.baseDamage;
    this.attackerHealth = other.attackerHealth;
    this.attackPower = other.attackPower;
    this.luckRolled = other.luckRolled;
    this.isCounter = other.isCounter;
    aw1Luck = attacker.CO.aw1Combat;
    this.attackerDamageMultiplier = other.attackerDamageMultiplier;
    this.defenderDamageMultiplier = other.defenderDamageMultiplier;

    this.targetCoord = other.targetCoord;
  }

  public int calculateDamage()
  {
    int luckDamage = getLuck();
    if( aw1Luck && isCounter )
      luckDamage = 0;
    final int rawDamage = baseDamage * attackPower * attackerDamageMultiplier / 100 / 100;
    int hpScalingDamage = rawDamage;
    if( !aw1Luck )
      hpScalingDamage += luckDamage;

    // Apply terrain defense to the correct defense number
    int finalDefenseSubtraction = defenseSubtraction;
    int finalDefenseDivision    = defenseDivision;
    if( terrainGivesSubtraction )
      finalDefenseSubtraction += terrainStars * defenderHealth / 10;
    else
      finalDefenseDivision    += terrainStars * defenderHealth / 10;
    final int subtractionMultiplier = 200 - finalDefenseSubtraction;

    int overallPower = hpScalingDamage * attackerHealth / 100;
    overallPower = overallPower * defenderDamageMultiplier / 100;
    if( aw1Luck && overallPower > 0 )
      overallPower += luckDamage;
    overallPower = overallPower * subtractionMultiplier /        100;
    overallPower = overallPower *          100          / finalDefenseDivision;

    return overallPower; // % damage
  }

  protected int getLuck()
  {
    return 0;
  }

  public static class BattleParams extends StrikeParams
  {
    public final CombatContext combatContext;
    public final UnitContext defender;

    protected BattleParams(
        StrikeParams base,
        CombatContext combatContext,
        UnitContext defender)
    {
      super(base);
      this.combatContext = combatContext;
      this.defender = defender;

      defenderHealth = defender.getHealth();
      this.defenseSubtraction = defender.defensePower;
      this.terrainStars = defender.terrainStars;
    }

    @Override
    protected int getLuck()
    {
      int luckDamage = 0;
      switch (combatContext.calcType)
      {
        case NO_LUCK:
        case DEMOLITION:
          break;
        case COMBAT:
          luckDamage = getLuckReal();
          break;
        case OPTIMISTIC:
          if( !isCounter )
            luckDamage = getLuckOptimistic();
          else // If the attacker is being optimistic, the defender (countering) should be pessimistic
            luckDamage = getLuckPessimistic();
          break;
        case PESSIMISTIC:
          if( !isCounter ) // Vice versa
            luckDamage = getLuckPessimistic();
          else
            luckDamage = getLuckOptimistic();
          break;
      }
      return luckDamage;
    }
    private int getLuckOptimistic()
    {
      int luckDamage = luckBase;
      luckDamage += luckRolled-1;
      return luckDamage;
    }
    private int getLuckPessimistic()
    {
      int luckDamage = luckBase;
      luckDamage -= luckRolledBad-1;
      return luckDamage;
    }
    private int getLuckReal()
    {
      int luckDamage = luckBase;
      if( 0 != luckRolled )
        luckDamage += combatContext.gameInstance.getRN(luckRolled);
      if( 0 != luckRolledBad )
        luckDamage -= combatContext.gameInstance.getRN(luckRolledBad);
      return luckDamage;
    }
  }
}
