package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.DirectDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitContext;
import Units.UnitModel;

public class Max extends AWBWCommander
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
      super("Max", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Max (AWBW)\n"
          + "Direct units gain +20% attack. Indirect units lose -10% attack and have -1 range.\n"));
      infoPages.add(new InfoPage(
            "Max Force (3):\n"
          + "Direct units gain +1 movement and their attack increases to +30% (140/110 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Max Blast (6):\n"
          + "Direct units gain +2 movement and their attack increases to +50% (160/110 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Max(rules);
    }
  }

  public Max(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new MaxPower(this, cb, "Max Force", 3, 1, 10)); // -10 from AW2, since the generic bonus came out of his direct bonus
    addCommanderAbility(new MaxPower(this, cb, "Max Blast", 6, 2, 30));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      return;
    if( params.battleRange < 2 )
      params.attackPower += 20;
    else
      params.attackPower -= 10;
  }
  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.weapon != null && uc.weapon.rangeMax() > 1 )
      uc.rangeMax -= 1;
  }

  private static class MaxPower extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod, atkMod;

    MaxPower(Max commander, CostBasis basis, String name, int cost, int move, int power)
    {
      super(commander, name, cost, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(move));
      moveMod.noneOf = UnitModel.TROOP;
      moveMod.allOf  = UnitModel.DIRECT;
      atkMod = new UnitTypeFilter(new DirectDamageModifier(power));
      atkMod.noneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(atkMod);
    }
  }

}
