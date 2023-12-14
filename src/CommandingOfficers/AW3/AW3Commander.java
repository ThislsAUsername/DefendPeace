package CommandingOfficers.AW3;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.TrilogyCommander;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameScenario;
import Units.UnitDelta;

public abstract class AW3Commander extends TrilogyCommander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage AW3_MECHANICS_BLURB = new InfoPage(
      "Gain +10 attack and defense on activating a power.\n"
    + "Power charge is not based on funds, but is a specific stat for each unit type.\n"
    + TRILOGY_MECHANICS_BLURB.info
    );
  public static final int CHARGERATIO_AW3 = 100;

  public AW3Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
  }

  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    if( null != getActiveAbility() )
      return 0;
    if( minion == null || enemy == null )
      return 0;

    int guiHPLoss  = minion.getHPDamage() / 10;
    int guiHPDealt =  enemy.getHPDamage() / 10;

    int power = 0;

    power += guiHPLoss  * minion.model.abilityPowerValue;
    // The damage we deal is worth half as much as the damage we take, to help powers be a comeback mechanic.
    power += guiHPDealt *  enemy.model.abilityPowerValue / 2;

    return power;
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
