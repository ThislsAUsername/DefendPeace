package CommandingOfficers;

import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.Unit;

public class BWBrennerComp extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Brenner", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BWBrennerComp();
    }
  }

  public BWBrennerComp()
  {
    super(coInfo);

    new CODefenseModifier(10).apply(this);

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
