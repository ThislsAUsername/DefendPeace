package CommandingOfficers.AW3.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.CounterMultiplierModifier;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Kanbei extends AW3Commander
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
      super("Kanbei", UIUtils.SourceGames.AW3, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Kanbei (AWDS)\n"
          + "\n"
          + "\n"
          + "(+20/20 stats for 1.2x prices)\n"));
      infoPages.add(new InfoPage(new MoraleBoost(null, null),
            "Increases attack strength of all units.\n"
          + "(+40/10 stats, total 160/130)\n"));
      infoPages.add(new InfoPage(new SamuraiSpirit(null, null),
            "Strengthens offensive and defensive abilities of all units. Damage inflicted when counter attacking is multiplied by 1.5.\n"
          + "(+40/40 stats, total 160/160; 2.0x damage on counterattack)\n"));
      infoPages.add(new InfoPage(
            "Hit: Sonja\n"
          + "Miss: Computers"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new MoraleBoost(this, cb));
    addCommanderAbility(new SamuraiSpirit(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 20;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 20;
  }
  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costRatio += 20;
  }

  private static class MoraleBoost extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Morale Boost";
    private static final int COST = 4;
    UnitModifier statMod;

    MoraleBoost(Kanbei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDamageModifier(30);
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
    }
  }

  private static class SamuraiSpirit extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Samurai Spirit";
    private static final int COST = 7;
    UnitModifier statMod, counterMod;

    SamuraiSpirit(Kanbei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitFightStatModifier(30);
      counterMod = new CounterMultiplierModifier(200);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(counterMod);
    }
  }

}
