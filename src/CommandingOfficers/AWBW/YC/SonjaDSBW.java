package CommandingOfficers.AWBW.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.DefendPeace.misc.Venge;
import Engine.GameScenario;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitMods.VisionModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class SonjaDSBW extends AWBWCommander
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
      super("Sonja", UIUtils.SourceGames.AWBW, UIUtils.YC, "3BW");
      infoPages.add(new InfoPage(
            "AWDS Sonja for AWBW\n"
          + "VS AW2: Negates terrain stars, less bad luck, no D2D counter boost\n"
          + "Unlike DS Sonja, only negates terrain stars in fights with her units.\n"
          + "+1 vision, -1 terrain star to enemies, 5 bad luck\n"
          + "Calculation order vs Lash SCOP: combat initiator applies first.\n"));
      infoPages.add(new InfoPage(new EnhancedVision(null, new CostBasis(CHARGERATIO_FUNDS)),
            "+1 vision (total +2), piercing vision, -1 terrain star (total -2) to enemies.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new CounterBreak(null, new CostBasis(CHARGERATIO_FUNDS)),
            "+1 vision (total +2), piercing vision, -2 terrain star (total -3) to enemies.\n"
          + "Counterattacks happen before the attacker's attack.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new SonjaDSBW(rules);
    }
  }

  public SonjaDSBW(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new EnhancedVision(this, cb));
    addCommanderAbility(new CounterBreak(this, cb));
  }

  @Override
  public void changeCombatContext(CombatContext instance, UnitContext buffOwner)
  {
    debuffTerrain(instance, buffOwner, 1);
  }
  private static void debuffTerrain(CombatContext instance, UnitContext buffOwner, int terrainDebuff)
  {
    if( instance.attacker == buffOwner )
    {
      instance.defender.terrainStars -= terrainDebuff;
      instance.defender.terrainStars = Math.max(0, instance.defender.terrainStars);
    }
    else // Defender owns the buff; debuff attacker
    {
      instance.attacker.terrainStars -= terrainDebuff;
      instance.attacker.terrainStars = Math.max(0, instance.attacker.terrainStars);
    }
  }

  @Override
  public void modifyVision(UnitContext uc)
  {
    uc.visionRange += 1;
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.luckRolledBad += 5;
  }

  public static class TerrainReductionModifier implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;
    private final int debuffPower;
    public TerrainReductionModifier(int debuffPower)
    {
      this.debuffPower = debuffPower;
    }

    @Override
    public void changeCombatContext(CombatContext instance, UnitContext buffOwner)
    {
      debuffTerrain(instance, buffOwner, debuffPower);
    }
  }

  private static class EnhancedVision extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Enhanced Vision";
    private static final int COST = 3;
    UnitModifier sightMod, fightMod;

    EnhancedVision(SonjaDSBW commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      sightMod = new VisionModifier(1);
      fightMod = new TerrainReductionModifier(1);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
      modList.add(fightMod);
    }
  }

  private static class CounterBreak extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Counter Break";
    private static final int COST = 5;
    UnitModifier sightMod, fightMod, counterMod;

    CounterBreak(SonjaDSBW commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      sightMod = new VisionModifier(1);
      fightMod = new TerrainReductionModifier(2);
      counterMod = new Venge.PreEmptiveCounterMod();
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
      modList.add(fightMod);
      modList.add(counterMod);
    }
  }

}
