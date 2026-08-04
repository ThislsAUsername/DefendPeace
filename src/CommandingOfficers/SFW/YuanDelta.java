package CommandingOfficers.SFW;

import Engine.GameScenario;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;
import Units.UnitDelta;

public class YuanDelta extends AncientCommander
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
      super("Yuan Delta", UIUtils.SourceGames.SFW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Yuan Delta\n"
          + "Gains triple experience.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new YuanDelta(coInfo, rules);
    }
  }

  public YuanDelta(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
  }

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
  {
    if( this == battleInfo.attacker.CO )
      experiencize(battleInfo.attacker, battleInfo.defender);
    if( this == battleInfo.defender.CO )
      experiencize(battleInfo.defender, battleInfo.attacker);
    return null;
  }
  private void experiencize(UnitDelta attacker, UnitDelta defender)
  {
    int expPerPercent = vetTracker.getExperienceRate(defender);

    int profit = 2 * expPerPercent * defender.getPreciseHealthDamage();
    vetTracker.addExperience(attacker.unit, profit);
  }

}
