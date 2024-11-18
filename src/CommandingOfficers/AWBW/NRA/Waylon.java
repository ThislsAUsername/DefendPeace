package CommandingOfficers.AWBW.NRA;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.CommanderAbility.CostBasis;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitModel;

public class Waylon extends AWBWCommander
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
      super("Waylon", UIUtils.SourceGames.AWBW, UIUtils.NRA);
      infoPages.add(new InfoPage(
            "Waylon (AWBW)\n"
          + "Air units gain +20/30 stats\n"));
      infoPages.add(new InfoPage(Wingman(null, null),
            "Air units +20 (160) defense.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(BadCompany(null, null),
            "Air units +45 (185) defense.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Waylon(rules);
    }
  }
  private static CommanderAbility Wingman(Commander commander, CostBasis cb)
  {
    return new WaylonAbility(commander, cb, "Wingman", 3, 20);
  }
  private static CommanderAbility BadCompany(Commander commander, CostBasis cb)
  {
    return new WaylonAbility(commander, cb, "Bad Company", 6, 45);
  }

  static final long BOOST_MASK_ANY = UnitModel.AIR;
  public Waylon(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(Wingman(this, cb));
    addCommanderAbility(BadCompany(this, cb));
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
      params.defenseSubtraction += 30;
  }

  private static class WaylonAbility extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter defMod;

    WaylonAbility(Commander commander, CostBasis basis, String name, int cost, int defBoost)
    {
      super(commander, name, cost, basis);
      defMod = new UnitTypeFilter(new UnitDefenseModifier(defBoost));
      defMod.oneOf = BOOST_MASK_ANY;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(defMod);
    }
  }

}
