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

public class Hachi extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Hachi");
      infoPages.add(new InfoPage(
          "Hachi\r\n" + 
          "  Units cost -10% less to build\r\n" + 
          "Barter -- Units cost -50% to build\r\n" + 
          "Merchant Union -- Units cost -50% and ground units can deploy from cities"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Hachi(rules);
    }
  }

  public Hachi(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new Barter(this));
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

  private static class Barter extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Barter";
    private static final int COST = 3;
    private static final double VALUE = 0.5;

    Barter(Commander commander)
    {
      super(commander, NAME, COST);
      AIFlags |= PHASE_BUY;
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Merchant Union";
    private static final int COST = 5;
    private static final double VALUE = 0.5;

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
        um.COcost = VALUE;
        if( um.isLandUnit() )
          upm.addProductionPair(TerrainType.CITY, um);
      }

      myCommander.addCOModifier(upm);
    }
  }
}

