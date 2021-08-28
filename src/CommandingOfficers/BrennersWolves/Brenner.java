package CommandingOfficers.BrennersWolves;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Terrain.MapMaster;
import Units.Unit;

public class Brenner extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Brenner");
      infoPages.add(new InfoPage(
          "--BRENNER--\r\n" +
          "Units gain +10% defense.\r\n" +
          "xxxXXX\r\n" +
          "REINFORCE: All units gain +3 HP.\r\n" +
          "LIFELINE: All units gain +6 HP and +10 defense."));
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Reinforce";
    private static final int COST = 3;
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lifeline";
    private static final int COST = 6;
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
      myCommander.addCOModifier(new CODefenseModifier(10));
    }
  }
}

