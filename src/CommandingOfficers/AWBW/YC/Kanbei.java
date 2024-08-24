package CommandingOfficers.AWBW.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Kanbei extends AWBWCommander
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
      super("Kanbei", UIUtils.SourceGames.AWBW, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Kanbei (AWBW)\n"
          + "Units cost +20% more to build, but gain +30% attack and defense.\n"));
      infoPages.add(new InfoPage(new MoraleBoost(null, null),
            "All units' attack is increased to +40%.\n"
          + "+10 attack and defense.\n"
          + "(+20/10 stats, total 150/140)\n"));
      infoPages.add(new InfoPage(new SamuraiSpirit(null, null),
            "All units' attack is increased to +40% and defense to +50%. Counterattacks do 1.5x more damage.\n"
          + "+10 attack and defense.\n"
          + "(+20/30 stats, total 150/160; +65 attack on counters)\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new MoraleBoost(this, cb));
    addCommanderAbility(new SamuraiSpirit(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 30;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 30;
  }
  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costRatio += 20;
  }

  private static class MoraleBoost extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Morale Boost";
    private static final int COST = 4;
    UnitModifier statMod;

    MoraleBoost(Kanbei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDamageModifier(10);
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
    }
  }

  public static class CounterBonus implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      if( params.isCounter )
      {
        // BW Kanbei's SCOP bonus is a "1.5x" multiplier like in AW2, but only applies to the D2D stats (i.e. not scaling with SCOP/Tower bonuses)
        // D2D times 1.5 (1.3*1.5=1.95)
        // "final" stat minus D2D (1.95-1.3=0.65)
        params.attackPower += 65;
      }
    }
  }

  private static class SamuraiSpirit extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Samurai Spirit";
    private static final int COST = 7;
    UnitModifier atkMod, defMod, counterMod;

    SamuraiSpirit(Kanbei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      atkMod = new UnitDamageModifier(10);
      defMod = new UnitDefenseModifier(20);
      counterMod = new CounterBonus();
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(atkMod);
      modList.add(defMod);
      modList.add(counterMod);
    }
  }

}
