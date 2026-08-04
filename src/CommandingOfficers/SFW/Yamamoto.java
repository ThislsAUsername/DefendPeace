package CommandingOfficers.SFW;

import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Engine.StateTrackers.SFWExperienceTracker.SFWRank;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils;
import Units.Unit;

public class Yamamoto extends YuanDelta
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
      super("Yamamoto", UIUtils.SourceGames.SFW, UIUtils.MISC);
      infoPages.add(new InfoPage(
            "Mr. Yamamoto\n"
          + "Units begin at level 2.\n"
          + "Gains triple experience.\n"));
      infoPages.add(SFW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Yamamoto(rules);
    }
  }

  public Yamamoto(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    if( this == unit.CO )
      vetTracker.addExperience(unit, SFWRank.LEVEL2.exp + 1);
    return null;
  }

}
