package CommandingOfficers.AW1.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.DamageMultiplierDefense;
import Engine.UnitMods.DamageMultiplierOffense;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Units.UnitContext;
import Terrain.MapMaster;

public class Kanbei extends AW1Commander
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
      super("Kanbei_1", UIUtils.SourceGames.AW1, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Kanbei (AW1)\n"
          + "Fashions himself a modern samurai.\n"
          + "Strong offensive and defensive abilities. Deployment costs are very high.\n"
          + "(1.2x/0.8x damage dealt/taken while COP isn't active, +20% costs.)\n"));
      infoPages.add(new InfoPage(new MoraleBoost(null),
            "Increases offensive and defensive ratings for all units.\n"
          + "(1.4x/0.7x damage dealt/taken.)\n"));
      infoPages.add(new InfoPage(
            "Hit: Sonja\n"
          + "Miss: Computers"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Kanbei(rules);
    }
  }

  public Kanbei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new MoraleBoost(this));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( null != getActiveAbility() )
      return;
    params.attackerDamageMultiplier *= 120;
    params.attackerDamageMultiplier /= 100;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( null != getActiveAbility() )
      return;
    params.defenderDamageMultiplier *= 120;
    params.defenderDamageMultiplier /= 100;
  }
  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costRatio += 20;
  }

  private static class MoraleBoost extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Morale Boost";
    private static final int COST = 8;
    UnitModifier attMod;
    UnitModifier defMod;

    MoraleBoost(Kanbei commander)
    {
      super(commander, NAME, COST);
      attMod = new DamageMultiplierOffense(140);
      defMod = new DamageMultiplierDefense(70);
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(attMod);
      modList.add(defMod);
    }
  }

}
