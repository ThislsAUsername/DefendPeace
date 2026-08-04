package CommandingOfficers.SFW;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;

public class BillyGates extends YuanDelta
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Billy Gates", UIUtils.SourceGames.SFW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Billy Gates\n"
          + "+10k income.\n"
          + "Gains triple experience.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new BillyGates(rules);
    }
  }

  public BillyGates(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

  public int getIncomePerTurn()
  {
    return super.getIncomePerTurn() + 10000;
  }

}
