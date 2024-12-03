package CommandingOfficers.AWBW.BH;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassHealEvent;
import UI.UIUtils;
import Terrain.MapMaster;
import lombok.var;

public class AndyClone extends AWBWCommander
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
      super("Andy", UIUtils.SourceGames.AWBW, UIUtils.BH, "BH");
      infoPages.add(new InfoPage(
            "Andy (Clone)\n"
          + "A port of the ReBoot-Camp CO.\n"
          + "No day-to-day abilities.\n"));
      infoPages.add(new InfoPage(new HyperRepair(null, new CostBasis(CHARGERATIO_FUNDS)),
            "All units gain +2 HP.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new AndyClone(rules);
    }
  }

  public AndyClone(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new HyperRepair(this, cb));
  }

  private static class HyperRepair extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Hyper Repair";
    private static final int COST = 3;
    private static final int HEAL = 2;

    HyperRepair(AndyClone commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      AIFlags = 0; // Why one would ever use this is beyond me
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      var heal = new MassHealEvent(null, myCommander.army.getUnits(), HEAL*10);
      heal.roundUp = myCommander.roundUpRepairs;

      GameEventQueue events = new GameEventQueue();
      events.add(heal);

      return events;
    }
  }

}
