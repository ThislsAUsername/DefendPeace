package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class OSAndy extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Andy", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new OSAndy();
    }
  }

  public OSAndy()
  {
    super(coInfo);

    addCommanderAbility(new HyperRepair(this));
    addCommanderAbility(new HyperUpgrade(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class HyperRepair extends CommanderAbility
  {
    private static final String NAME = "Hyper Repair";
    private static final int COST = 3;
    private static final int VALUE = 2;

    HyperRepair(Commander commander)
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

  private static class HyperUpgrade extends CommanderAbility
  {
    private static final String NAME = "Hyper Upgrade";
    private static final int COST = 6;
    private static final int VALUE = 5;

    HyperUpgrade(Commander commander)
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
      myCommander.addCOModifier(new CODamageModifier(10));
      COMovementModifier moveMod = new COMovementModifier(1);
      for(UnitModel um : myCommander.unitModels)
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }
}
