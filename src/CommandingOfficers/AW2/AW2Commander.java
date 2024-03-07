package CommandingOfficers.AW2;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.AW2And3CommanderBase;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameScenario;
import Units.Unit;
import Units.UnitDelta;

public abstract class AW2Commander extends AW2And3CommanderBase
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage AW2_MECHANICS_BLURB = new InfoPage(
      "COP charge gained from combat is funds damage taken and half of funds damage dealt.\n"
    + TRILOGY_MECHANICS_BLURB.info
    );

  public AW2Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
  }

  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    if( minion == null || enemy == null )
      return 0;

    int guiHPLoss  = minion.getHPDamage();
    int guiHPDealt =  enemy.getHPDamage();

    int power = 0; // value in funds of the charge we're getting

    power += guiHPLoss * minion.unit.getCost() / 10;
    power += guiHPDealt * enemy.unit.getCost() / 10 / 2;

    return power;
  }
  @Override
  public int calculateMassDamageCharge(Unit minion, int lostHP)
  {
    return 0;
  }

  protected abstract static class AW2Ability extends TrilogyAbility
  {
    private static final long serialVersionUID = 1L;

    protected AW2Ability(Commander commander, String name, int cost, CostBasis basis)
    {
      super(0, 10, commander, name, cost, basis);
    }
  }
}
