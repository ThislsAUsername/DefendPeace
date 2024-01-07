package CommandingOfficers.AW1;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import java.util.ArrayList;
import Engine.GameScenario;
import Engine.UnitMods.DamageMultiplierOffense;
import Engine.UnitMods.DamageMultiplierDefense;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitDelta;

public abstract class AW1Commander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage AW1_MECHANICS_BLURB = new InfoPage(
      "COP charge gained from combat is funds damage taken and 1/4 of funds damage dealt.\n"
    + "CO powers don't charge while a power is active.\n"
    + "On each power activation, each star in your meter costs 1/5 more of the base star cost.\n"
    + "This continues for 9 activations, after which the star cost settles at double the base star cost.\n"
    );
  public static final int AW1_STAR_VALUE = 5000;

  public AW1Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    aw1Combat = true;
  }

  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    if( null != getActiveAbility() )
      return 0;
    if( minion == null || enemy == null )
      return 0;

    int guiHPLoss  = minion.getHealthDamage() / 10;
    int guiHPDealt =  enemy.getHealthDamage() / 10;

    int power = 0; // value in funds of the charge we're getting

    // Add up the funds value of the damage done to both participants.
    power += guiHPLoss * minion.unit.getCost() / 10;
    power += guiHPDealt * enemy.unit.getCost() / 10 / 4;

    return power;
  }
  @Override
  public int calculateMassDamageCharge(Unit minion, int lostHP)
  {
    return 0;
  }

  protected abstract static class AW1Ability extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;

    protected AW1Ability(Commander commander, String name, int cost)
    {
      super(commander, name, cost, new CostBasis(AW1_STAR_VALUE));
      costBasis.maxStarRatio = AW1_STAR_VALUE * 2; // 10k per star
    }
  }

  /**
   * Provides the standard 1.1x/0.9x multipliers
   */
  protected abstract static class AW1BasicAbility extends AW1Ability
  {
    private static final long serialVersionUID = 1L;
    UnitModifier genericAttack;
    UnitModifier genericDefense;

    protected AW1BasicAbility(Commander commander, String name, int cost)
    {
      super(commander, name, cost);
      genericAttack  = new DamageMultiplierOffense(110);
      genericDefense = new DamageMultiplierDefense(90);
    }

    @Override
    protected final void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(genericAttack);
      modList.add(genericDefense);
      enqueueMods(gameMap, modList);
    }
    // Extra function to allow for stat mods so the above can be final.
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {}
  }
}
