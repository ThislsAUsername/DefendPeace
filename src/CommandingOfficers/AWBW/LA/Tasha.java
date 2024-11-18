package CommandingOfficers.AWBW.LA;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.CommanderAbility.CostBasis;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitModel;

public class Tasha extends AWBWCommander
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
      super("Tasha", UIUtils.SourceGames.AWBW, UIUtils.LA);
      infoPages.add(new InfoPage(
            "Tasha (AWBW)\n"
          + "Air units gain +40/20 stats\n"));
      infoPages.add(new InfoPage(FoxOne(null, null),
            "Air units +2 move.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(SonicBoom(null, null),
            "Air units +4 move.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Tasha(rules);
    }
  }
  private static CommanderAbility FoxOne(Commander commander, CostBasis cb)
  {
    return new TashaAbility(commander, cb, "Fox One", 2, 2);
  }
  private static CommanderAbility SonicBoom(Commander commander, CostBasis cb)
  {
    return new TashaAbility(commander, cb, "Sonic Boom", 5, 4);
  }

  static final long BOOST_MASK_ANY = UnitModel.AIR;
  public Tasha(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(FoxOne(this, cb));
    addCommanderAbility(SonicBoom(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(BOOST_MASK_ANY) )
      params.attackPower += 40;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isAny(BOOST_MASK_ANY) )
      params.defenseSubtraction += 20;
  }

  private static class TashaAbility extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod;

    TashaAbility(Commander commander, CostBasis basis, String name, int cost, int moveBoost)
    {
      super(commander, name, cost, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(moveBoost));
      moveMod.oneOf = BOOST_MASK_ANY;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

}
