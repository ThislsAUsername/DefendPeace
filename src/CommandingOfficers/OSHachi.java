package CommandingOfficers;

import CommandingOfficers.Modifiers.UnitProductionModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class OSHachi extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Hachi", new instantiator());

  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "Hachi\r\n" + 
          "  Units cost -10% less to build\r\n" + 
          "Barter -- Units cost -50% to build\r\n" + 
          "Merchant Union -- Units cost -50% and ground units can deploy from cities"));
    }
    @Override
    public Commander create()
    {
      return new OSHachi();
    }
  }

  public OSHachi()
  {
    super(coInfo);

    addCommanderAbility(new Barter(this));
    addCommanderAbility(new MerchantUnion(this));
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    for( UnitModel um : unitModels )
    {
      um.COcost = 0.9;
    }
    return super.initTurn(map);
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
    protected void perform(MapMaster gameMap)
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
    protected void perform(MapMaster gameMap)
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
