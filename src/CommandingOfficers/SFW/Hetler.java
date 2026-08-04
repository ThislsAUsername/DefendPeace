package CommandingOfficers.SFW;

import Engine.GameScenario;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;
import Units.UnitDelta;

public class Hetler extends AncientCommander
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
      super("Hetler", UIUtils.SourceGames.SFW, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Hetler\n"
          + "When attacked, gain half the EXP of killing the attacker.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Hetler(rules);
    }
  }

  public Hetler(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
  {
    if( this == battleInfo.defender.CO )
      experiencize(battleInfo.attacker, battleInfo.defender);
    return null;
  }
  private void experiencize(UnitDelta attacker, UnitDelta defender)
  {
    int expPerPercent = vetTracker.getExperienceRate(attacker);

    int profit = expPerPercent * 50;
    vetTracker.addExperience(attacker.unit, profit);
  }

}
