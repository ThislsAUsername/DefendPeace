package CommandingOfficers.BrennersWolves;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.Unit;

public class Brenner extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Brenner");
      infoPages.add(new InfoPage(
          "--BRENNER--\r\n" + 
          "Units gain +10% defense.\r\n" + 
          "xxxxXXXX\r\n" + 
          "REINFORCE: All units gain +3 HP.\r\n" + 
          "LIFELINE: All units gain +6 HP."));
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

    new CODefenseModifier(10).applyChanges(this);

    addCommanderAbility(new Reinforce(this));
    addCommanderAbility(new Lifeline(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Reinforce extends CommanderAbility
  {
    private static final String NAME = "Reinforce";
    private static final int COST = 4;
    private static final int VALUE = 3;

    Reinforce(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        unit.alterHP(VALUE);
      }
    }
  }

  private static class Lifeline extends CommanderAbility
  {
    private static final String NAME = "Lifeline";
    private static final int COST = 8;
    private static final int VALUE = 6;

    Lifeline(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        unit.alterHP(VALUE);
      }
    }
  }
}

