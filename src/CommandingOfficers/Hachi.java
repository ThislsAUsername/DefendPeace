package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class Hachi extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Hachi", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new Hachi();
    }
  }

  public Hachi()
  {
    super(coInfo);

    addCommanderAbility(new Barter(this));
    addCommanderAbility(new MerchantUnion(this));
  }

  @Override
  public void initTurn(GameMap map)
  {
    for( UnitModel um : unitModels )
    {
      um.COcost = 0.9;
    }
    super.initTurn(map);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Barter extends CommanderAbility
  {
    private static final String NAME = "Barter";
    private static final int COST = 3;
    private static final double VALUE = 0.5;

    Barter(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      for( UnitModel um : myCommander.unitModels )
      {
        um.COcost = VALUE;
      }
    }
  }

  private static class MerchantUnion extends CommanderAbility
  {
    private static final String NAME = "Merchant Union";
    private static final int COST = 5;
    private static final int VALUE = 5;

    MerchantUnion(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.CITY,
          myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));

      for( UnitModel um : myCommander.unitModels )
      {
        um.COcost = VALUE;
        if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
          upm.addProductionPair(TerrainType.CITY, um);
      }

      myCommander.addCOModifier(upm);
    }
  }
}
