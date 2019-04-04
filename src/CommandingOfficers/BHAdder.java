package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class BHAdder extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Adder", new instantiator());

  private static class instantiator extends COMaker
  {
    @Override
    public Commander create()
    {
      return new BHAdder();
    }
  }

  public BHAdder()
  {
    super(coInfo);

    addCommanderAbility(new Sideslip(this));
    addCommanderAbility(new Sidewinder(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Sideslip extends CommanderAbility
  {
    private static final String NAME = "Sideslip";
    private static final int COST = 2;

    Sideslip(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(1);
      for(UnitModel um : myCommander.unitModels)
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }

  private static class Sidewinder extends CommanderAbility
  {
    private static final String NAME = "Sidewinder";
    private static final int COST = 5;

    Sidewinder(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(2);
      for(UnitModel um : myCommander.unitModels)
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }
}
