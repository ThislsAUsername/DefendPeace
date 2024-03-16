package CommandingOfficers.AW1.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.DirectDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitContext;
import Units.UnitModel;

public class Max extends AW1Commander
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
      super("Max_1", UIUtils.SourceGames.AW1, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Max (AW1)\n"
          + "Dependable and brave. Over-protective of Sami and Andy.\n"
          + "Direct combat units have high firepower. Distance units are weak and have small attack ranges.\n"
          + "(+50/0 vehicle direct combat, indirects -1 range and 0.9x/1.1x damage dealt/taken.)\n"));
      infoPages.add(new InfoPage(new MaxForce(null),
            "Increases all abilities of direct combat units.\n"
          + "(+20/0 (187/110) vehicle direct combat, +1 move to non-indirect vehicles)\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: Weight training\n"
          + "Miss: Studying"));
      infoPages.add(AW1_MECHANICS_BLURB);
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

    addCommanderAbility(new MaxForce(this));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 && params.attacker.model.isNone(UnitModel.TROOP) )
      params.attackPower += 50;
    if( params.attacker.model.isAny(UnitModel.INDIRECT) )
    {
      params.attackerDamageMultiplier *=  90;
      params.attackerDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isAny(UnitModel.INDIRECT) )
    {
      params.defenderDamageMultiplier *= 110;
      params.defenderDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.weapon != null && uc.weapon.rangeMax > 1 )
      uc.weapon.rangeMax -= 1;
  }

  private static class MaxForce extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Max Force";
    private static final int COST = 6;
    UnitTypeFilter moveMod;
    UnitTypeFilter atkMod;

    MaxForce(Max commander)
    {
      super(commander, NAME, COST);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(1));
      moveMod.noneOf = UnitModel.INDIRECT | UnitModel.TROOP;
      atkMod = new UnitTypeFilter(new DirectDamageModifier(20));
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
