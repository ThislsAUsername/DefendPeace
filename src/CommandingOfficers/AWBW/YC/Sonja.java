package CommandingOfficers.AWBW.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.DefendPeace.misc.Venge;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.VisionModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Sonja extends AWBWCommander
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
      super("Sonja", UIUtils.SourceGames.AWBW, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Sonja (AWBW)\n"
          + "Units gain +1 vision in Fog of War, and counterattacks do 1.5x more damage. Luck is reduced to -9% to +9%.\n"));
      infoPages.add(new InfoPage(new EnhancedVision(null, null),
            "All units gain +1 vision, and can see into forests and reefs.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new CounterBreak(null, null),
            "All units gain +1 vision, and can see into forests and reefs. A unit being attacked will attack first (even if it would be destroyed by the attack).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sonja(rules);
    }
  }

  public Sonja(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new EnhancedVision(this, cb));
    addCommanderAbility(new CounterBreak(this, cb));
  }

  @Override
  public void modifyVision(UnitContext uc)
  {
    uc.visionRange += 1;
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.luckRolledBad += 10;
    if( params.isCounter )
    {
      params.attackerDamageMultiplier *= 150;
      params.attackerDamageMultiplier /= 100;
    }
  }

  private static class EnhancedVision extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Enhanced Vision";
    private static final int COST = 3;
    UnitModifier sightMod;

    EnhancedVision(Sonja commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      sightMod = new VisionModifier(1);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
    }
  }

  private static class CounterBreak extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Counter Break";
    private static final int COST = 5;
    UnitModifier sightMod, counterMod;

    CounterBreak(Sonja commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      sightMod = new VisionModifier(1);
      counterMod = new Venge.PreEmptiveCounterMod();
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
      modList.add(counterMod);
    }
  }

}
