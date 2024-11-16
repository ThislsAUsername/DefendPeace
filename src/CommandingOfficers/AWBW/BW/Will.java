package CommandingOfficers.AWBW.BW;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Units.UnitModel;
import Terrain.MapMaster;

public class Will extends AWBWCommander
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
      super("Will", UIUtils.SourceGames.AWBW, UIUtils.BW);
      infoPages.add(new InfoPage(
            "Will (AWBW)\n"
          + "Direct land units (including footsoldiers) gain +20% attack\n"));
      infoPages.add(new InfoPage(new RallyCry(null, null),
            "Direct land units gain +1 movement.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new ANewEra(null, null),
            "Direct land units gain +2 movement.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Will(rules);
    }
  }

  static final long BOOST_MASK_ALL = UnitModel.LAND | UnitModel.DIRECT;
  public Will(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new RallyCry(this, cb));
    addCommanderAbility(new ANewEra(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAll(BOOST_MASK_ALL) )
      params.attackPower += 20;
  }

  private static class RallyCry extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Rally Cry";
    private static final int COST = 2;
    UnitTypeFilter moveMod;
    
    RallyCry(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(1));
      moveMod.allOf = BOOST_MASK_ALL;
    }
    
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

  private static class ANewEra extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "A New Era";
    private static final int COST = 5;
    UnitTypeFilter moveMod;

    ANewEra(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(2));
      moveMod.allOf = BOOST_MASK_ALL;
    }
    
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

}
