package CommandingOfficers.AWBW.LA;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.CommanderAbility.CostBasis;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitModel;

public class Gage extends AWBWCommander
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
      super("Gage", UIUtils.SourceGames.AWBW, UIUtils.LA);
      infoPages.add(new InfoPage(
            "Gage (AWBW)\n"
          + "Naval units and indirects gain +20/10 stats\n"));
      infoPages.add(new InfoPage(LongShot(null, null),
            "Indirects gain +1 range.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(LongBarrel(null, null),
            "Indirects gain +2 range.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Gage(rules);
    }
  }
  private static CommanderAbility LongShot(Commander commander, CostBasis cb)
  {
    return new GageAbility(commander, cb, "Long Shot", 2, 1);
  }
  private static CommanderAbility LongBarrel(Commander commander, CostBasis cb)
  {
    return new GageAbility(commander, cb, "Long Barrel", 5, 2);
  }

  static final long BOOST_MASK_ANY = UnitModel.SEA | UnitModel.INDIRECT;
  public Gage(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(LongShot(this, cb));
    addCommanderAbility(LongBarrel(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(BOOST_MASK_ANY) )
      params.attackPower += 20;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.attacker.model.isAny(BOOST_MASK_ANY) )
      params.defenseSubtraction += 10;
  }

  private static class GageAbility extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitModifier rangeMod;

    GageAbility(Commander commander, CostBasis basis, String name, int cost, int rangeBoost)
    {
      super(commander, name, cost, basis);
      rangeMod = new UnitIndirectRangeModifier(rangeBoost);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(rangeMod);
    }
  }

}
