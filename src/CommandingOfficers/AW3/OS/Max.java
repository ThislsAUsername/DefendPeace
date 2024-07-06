package CommandingOfficers.AW3.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.DirectDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitContext;
import Units.UnitModel;

public class Max extends AW3Commander
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
      super("Max", UIUtils.SourceGames.AW3, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Max (AW3)\n"
          + "A brave and loyal friend, not to mention a strong fighter. Despises treachery.\n"
          + "Non-infantry direct-combat units are tops. Indirect-combat units are reduced in range and firepower.\n"
          + "(Vehicles +20/0 in direct combat, bland else; indirects -1 range)\n"));
      infoPages.add(new InfoPage(
            "Max Force (3):\n"
          + "Firepower of non-infantry direct combat units rises (+30, 160 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Max Blast (6):\n"
          + "Firepower of non-infantry direct combat units rises greatly (+60, 190 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Weight Training\n"
          + "Miss: Studying"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new MaxPower(this, cb, "Max Force", 3, 30));
    addCommanderAbility(new MaxPower(this, cb, "Max Blast", 6, 60));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      return;
    if( params.battleRange < 2 )
      params.attackPower += 20;
  }
  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.weapon != null && uc.weapon.rangeMax() > 1 )
      uc.rangeMax -= 1;
  }

  private static class MaxPower extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter atkMod;

    MaxPower(Max commander, CostBasis basis, String name, int cost, int power)
    {
      super(commander, name, cost, basis);
      atkMod = new UnitTypeFilter(new DirectDamageModifier(power));
      atkMod.noneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(atkMod);
    }
  }

}
