package CommandingOfficers.SFW;

import Engine.GameScenario;
import Engine.Combat.StrikeParams.BattleParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;

public class Caroline extends YuanDelta
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
      super("Caroline", UIUtils.SourceGames.SFW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Caroline\n"
          + "+5 luck, and -5% raw damage taken.\n"
          + "Gains triple experience.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Caroline(rules);
    }
  }

  public Caroline(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    luck += 5;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    super.modifyUnitDefenseAgainstUnit(params);
    params.trueDamage -= 5;
  }

}
