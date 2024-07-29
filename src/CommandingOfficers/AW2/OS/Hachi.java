package CommandingOfficers.AW2.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
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

public class Hachi extends AW2Commander
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
      super("Hachi", UIUtils.SourceGames.AW2, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Hachi (AW2)\n"
          + "Rumoured to be Orange Star's former chief, he's closing up shop to hit the battlefield.\n"
          + "Uses secret trade routes to get lower deployment costs (90%) for all units.\n"));
      infoPages.add(new InfoPage(new Barter(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Speaks with such authority that he obtains even lower (50%) deployment costs.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new MerchantUnion(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Deployment costs drop (50%). Merchant pals gather from around the globe and help him deploy ground units from any allied property.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Tea\n"
          + "Miss: Medicine"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

  private static class Barter extends AW2Ability
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

  private static class MerchantUnion extends AW2Ability
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
