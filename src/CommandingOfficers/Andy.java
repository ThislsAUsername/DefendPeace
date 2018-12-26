package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;

public class Andy extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Andy", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new Andy();
    }
  }

  public Andy()
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
    protected void perform(GameMap gameMap)
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
    protected void perform(GameMap gameMap)
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
