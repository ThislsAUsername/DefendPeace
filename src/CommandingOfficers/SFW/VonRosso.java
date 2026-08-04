package CommandingOfficers.SFW;

import Engine.GameScenario;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;
import Units.UnitDelta;

public class VonRosso extends AncientCommander
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
      super("Von Rosso", UIUtils.SourceGames.SFW, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Von Rosso\n"
          + "Adds 6 minus his own unit's EXP/% value to his EXP rate.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new VonRosso(rules);
    }
  }

  public VonRosso(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
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
    int expPerPercent = 6 - vetTracker.getExperienceRate(attacker);

    int profit = expPerPercent * defender.getPreciseHealthDamage();
    vetTracker.addExperience(attacker.unit, profit);
  }

}
