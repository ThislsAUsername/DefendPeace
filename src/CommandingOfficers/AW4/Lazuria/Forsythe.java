package CommandingOfficers.AW4.Lazuria;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import UI.UIUtils;

public class Forsythe extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Forsythe", UIUtils.SourceGames.AW4, UIUtils.LA);
      infoPages.add(new InfoPage(
          "The Lazurian Army commander. He was called out of retirement to defend his nation.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: All units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(
          "No CO power (and cannot expand his zone)\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Forsythe(rules);
    }
  }

  public static final int RADIUS  = 5;
  public static final int POWER   = 10;
  public static final int DEFENSE = 10;

  public Forsythe(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

}
