package CommandingOfficers.OrangeStar;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class YuanDelta extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Yuan Delta");
      infoPages.add(new InfoPage(
          "Yuan Delta (rebalanced Hachi)\r\n" + 
          "  Units cost -10% less to build\r\n" + 
          "Merchant Union (8): Ground vehicles can deploy from cities"));
    }
    @Override
    public Commander create()
    {
      return new YuanDelta();
    }
  }

  public YuanDelta()
  {
    super(coInfo);

    addCommanderAbility(new MerchantUnion(this));
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    for( UnitModel um : unitModels.values() )
    {
      um.COcost = 0.9;
    }
    return super.initTurn(map);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class MerchantUnion extends CommanderAbility
  {
    private static final String NAME = "Merchant Union";
    private static final int COST = 8;

    MerchantUnion(Commander commander)
    {
      super(commander, NAME, COST);
      AIFlags |= PHASE_BUY;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.CITY,
          myCommander.getUnitModel(UnitModel.UnitEnum.TANK));

      for( UnitModel um : myCommander.unitModels.values() )
      {
        if( um.chassis == ChassisEnum.TANK )
          upm.addProductionPair(TerrainType.CITY, um);
      }

      myCommander.addCOModifier(upm);
    }
  }
}

