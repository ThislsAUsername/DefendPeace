package CommandingOfficers.AW2;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.TrilogyCommander;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameScenario;

public abstract class AW2Commander extends TrilogyCommander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage AW2_MECHANICS_BLURB = new InfoPage(
      "Gain +10 defense on activating a power.\n"
    + "COP charge gained from combat is funds damage taken and half of funds damage dealt.\n"
    + TRILOGY_MECHANICS_BLURB.info
    );

  public AW2Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
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
