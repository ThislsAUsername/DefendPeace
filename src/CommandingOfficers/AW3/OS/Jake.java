package CommandingOfficers.AW3.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.UnitModel;

public class Jake extends AW3Commander
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
      super("Jake", UIUtils.SourceGames.AW3, UIUtils.OS, "");
      infoPages.add(new InfoPage(
            "Jake (AWDS)\n"
          + "A young, energetic Orange Star CO who is a top notch tank commander.\n"
          + "Fights well in the open. Firepower of all units is increased (+10) on plains.\n"));
      infoPages.add(new InfoPage(new BeatDown(null, new CostBasis(CHARGERATIO_AW3)),
            "Firing range of (land) vehicles is increased by one. Firepower of all units is further increased (+10, 130 total) on plains.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new BlockRock(null, new CostBasis(CHARGERATIO_AW3)),
            "Firing range of (land) vehicles is increased by one, and movement range by two. Firepower of all units is greatly increased (+30, 150 total) on plains.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Clubbin'\n"
          + "Miss: Easy Listening"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new BeatDown(this, cb));
    addCommanderAbility(new BlockRock(this, cb));
  }

  public static void applyPlainsBoost(StrikeParams params, int boost)
  {
    if( params.attacker.env != null && params.attacker.env.terrainType == TerrainType.GRASS )
      params.attackPower += boost;
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    applyPlainsBoost(params, 10);
  }
  public static class PlainsDamageBoost implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;
    private int boost;

    public PlainsDamageBoost(int percent)
    {
      boost = percent;
    }
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      applyPlainsBoost(params, boost);
    }
  }

  private static class BeatDown extends AW3Ability
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
      statMod = new PlainsDamageBoost(BUFF);
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

  private static class BlockRock extends AW3Ability
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
      statMod = new PlainsDamageBoost(BUFF);
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
