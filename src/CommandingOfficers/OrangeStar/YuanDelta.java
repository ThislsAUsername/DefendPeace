package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.UnitModel;

public class YuanDelta extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Yuan Delta");
      infoPages.add(new InfoPage(
          "Yuan Delta (rebalanced Hachi)\r\n" + 
          "  Units cost -10% less to build\r\n" + 
          "Merchant Union (8): Ground vehicles can deploy from cities"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new YuanDelta(rules);
    }
  }

  public YuanDelta(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new MerchantUnion(this));
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
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

  private static class MerchantUnion extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
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
      UnitProductionModifier upm = new UnitProductionModifier();

      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isLandUnit() && um.isNone(UnitModel.TROOP) )
          upm.addProductionPair(TerrainType.CITY, um);
      }

      myCommander.addCOModifier(upm);
    }
  }
}

