package CommandingOfficers.AW2.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
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

public class Max extends AW2Commander
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
      super("Max", UIUtils.SourceGames.AW2, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Max (AW2)\n"
          + "A brave and loyal friend and an earnest warrior who hates deception and trickery.\n"
          + "Non-infantry direct combat units are tops. Indirect combat troops are limited in range and firepower.\n"
          + "(Vehicles +20/0 in direct combat, -10/0 else; indirects -1 range)\n"));
      infoPages.add(new InfoPage(
            "Max Force (3):\n"
          + "Firepower of direct-combat units rises slightly, and unit movement increases by 1 space.\n"
          + "(+20/0 (140/110) vehicle direct combat, +1 move to direct vehicles)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Max Blast (6):\n"
          + "Direct combat units receive firepower boost, and their movement increases by 2 spaces.\n"
          + "(+40/0 (160/110) vehicle direct combat, +2 move to direct vehicles)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Weight training\n"
          + "Miss: Studying"));
      infoPages.add(AW2_MECHANICS_BLURB);
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
    addCommanderAbility(new MaxPower(this, cb, "Max Force", 3, 1, 20));
    addCommanderAbility(new MaxPower(this, cb, "Max Blast", 6, 2, 40));
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

  private static class MaxPower extends AW2Ability
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
