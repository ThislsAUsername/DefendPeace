package CommandingOfficers.AWBW.BW;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassHealEvent;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import lombok.var;

public class Brenner extends AWBWCommander
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
      super("Brenner", UIUtils.SourceGames.AWBW, UIUtils.BW);
      infoPages.add(new InfoPage(
            "Brenner (AWBW)\n"
          + "Units gain +10% defense\n"));
      infoPages.add(new InfoPage(new Reinforce(null, null),
            "All units gain +3 HP.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new Lifeline(null, null),
            "All units gain +"+Lifeline.HEAL+"HP and +"+Lifeline.BUFF+"% defense.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Brenner(rules);
    }
  }

  public Brenner(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Reinforce(this, cb));
    addCommanderAbility(new Lifeline(this, cb));
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 10;
  }

  private static class Reinforce extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Reinforce";
    private static final int COST = 3;
    private static final int HEAL = 3;

    Reinforce(Brenner commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
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

  private static class Lifeline extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lifeline";
    private static final int COST = 6;
    private static final int BUFF = 10;
    private static final int HEAL = 6;
    UnitModifier statMod;

    Lifeline(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDefenseModifier(BUFF);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
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
