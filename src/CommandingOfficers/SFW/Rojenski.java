package CommandingOfficers.SFW;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;

public class Rojenski extends AncientCommander
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
      super("Rojenski", UIUtils.SourceGames.SFW, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Rojenski\n"
          + "No special effects.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Rojenski(rules);
    }
  }

  public Rojenski(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

}
