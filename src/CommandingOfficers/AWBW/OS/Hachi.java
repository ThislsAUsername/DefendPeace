package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import Engine.GameScenario;
import Engine.UnitMods.UnitDiscount;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.UIUtils;
import Units.UnitContext;
import Units.UnitModel;

public class Hachi extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Hachi", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Hachi (AWBW)\n"
          + "Units cost -10% less to build.\n"));
      infoPages.add(new InfoPage(new Barter(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Units cost -50% to build.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new MerchantUnion(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Units cost -50% and ground units can deploy from cities.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Barter(this, cb));
    addCommanderAbility(new MerchantUnion(this, cb));
  }

  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costRatio -= 10;
  }

  private static class Barter extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Barter";
    private static final int COST = 3;
    UnitModifier costMod;

    Barter(Hachi commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      costMod = new UnitDiscount(40);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(costMod);
    }
  }

  private static class MerchantUnion extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Merchant Union";
    private static final int COST = 5;
    UnitModifier costMod;

    MerchantUnion(Hachi commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      costMod = new UnitDiscount(40);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(costMod);
    }

    private UnitProductionModifier upmApplied = null;
    @Override
    protected void perform(MapMaster gameMap)
    {
      if( upmApplied == null )
      {
        // Make factory-buildables buildable from cities, for all COs in my army
        UnitProductionModifier upm = new UnitProductionModifier();
        for( UnitModel model : myCommander.unitProductionByTerrain.get(TerrainType.FACTORY) )
          upm.addProductionPair(TerrainType.CITY, model);
        for( Commander co : myCommander.army.cos )
          upm.applyChanges(co);
        upmApplied = upm;
      }
    }
    @Override
    protected void revert(MapMaster gameMap)
    {
      if(upmApplied != null)
      {
        for( Commander co : myCommander.army.cos )
          upmApplied.revertChanges(co);
        upmApplied = null;
      }
    }
  } // ~MerchantUnion
}
