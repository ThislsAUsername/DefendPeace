package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitModel;

public class Jake extends AWBWCommander
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
      super("Jake", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Jake (AWBW)\n"
          + "Units (even air units) gain +10% attack power on plains.\n"));
      infoPages.add(new InfoPage(new BeatDown(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Land indirects gain +1 range, and plains bonus is increased to +20% (130 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new BlockRock(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Land indirects gain +1 range, plains bonus is increased to +40% (150 total), and (land) vehicles gain +2 movement.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jake(rules);
    }
  }

  public Jake(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new BeatDown(this, cb));
    addCommanderAbility(new BlockRock(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    CommandingOfficers.AW3.OS.Jake.applyPlainsBoost(params, 10);
  }

  private static class BeatDown extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Beat Down";
    private static final int COST = 3;
    private static final int BUFF = 10;
    UnitModifier statMod;
    UnitTypeFilter rangeMod;

    BeatDown(Jake commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new CommandingOfficers.AW3.OS.Jake.PlainsDamageBoost(BUFF);
      rangeMod = new UnitTypeFilter(new UnitIndirectRangeModifier(1));
      rangeMod.allOf  = UnitModel.TANK | UnitModel.INDIRECT;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(rangeMod);
    }
  }

  private static class BlockRock extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Block Rock";
    private static final int COST = 6;
    private static final int BUFF = 30;
    UnitModifier statMod;
    UnitTypeFilter rangeMod;
    UnitTypeFilter moveMod;

    BlockRock(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new CommandingOfficers.AW3.OS.Jake.PlainsDamageBoost(BUFF);
      rangeMod = new UnitTypeFilter(new UnitIndirectRangeModifier(1));
      rangeMod.allOf  = UnitModel.TANK | UnitModel.INDIRECT;
      moveMod = new UnitTypeFilter(new UnitMovementModifier(2));
      moveMod.allOf  = UnitModel.TANK;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(rangeMod);
      modList.add(moveMod);
    }
  }

}
