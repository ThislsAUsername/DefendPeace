package CommandingOfficers.AW3;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.AW2And3CommanderBase;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameScenario;
import Units.Unit;
import Units.UnitDelta;

public abstract class AW3Commander extends AW2And3CommanderBase
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage AW3_MECHANICS_BLURB = new InfoPage(
      "Power charge is not based on funds, but is a separate stat.\n"
    + "Energy gain is still halved for damage dealt, though.\n"
    + TRILOGY_MECHANICS_BLURB.info
    );
  public static final int CHARGERATIO_AW3 = 100;

  public AW3Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
  }

  public CommanderAbility.CostBasis getGameBasis()
  {
    return new CommanderAbility.CostBasis(CHARGERATIO_AW3);
  }

  // Charge based on abilityPowerValue-scaled damage taken + 1/2 dealt
  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    int guiHPLoss  = minion.getHPDamage();
    int guiHPDealt =  enemy.getHPDamage();

    int power = 0;

    power += guiHPLoss  * minion.model.abilityPowerValue;
    power += guiHPDealt *  enemy.model.abilityPowerValue / 2;

    return power;
  }
  @Override
  public int calculateMassDamageCharge(Unit minion, int lostHP)
  {
    return 0;
  }

  protected abstract static class AW3Ability extends TrilogyAbility
  {
    private static final long serialVersionUID = 1L;

    protected AW3Ability(Commander commander, String name, int cost, CostBasis basis)
    {
      super(10, 10, commander, name, cost, basis);
    }
  }
}
