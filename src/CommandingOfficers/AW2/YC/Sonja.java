package CommandingOfficers.AW2.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
import CommandingOfficers.DefendPeace.misc.Venge;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.VisionModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Sonja extends AW2Commander
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
      super("Sonja", UIUtils.SourceGames.AW2, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Sonja (AW2)\n"
          + "Kanbei's daughter. Cool and collected, she likes to plan before making a move.\n"
          + "All units have an extended vision range in Fog of War. Counterattacks are slightly stronger.\n"
          + "(+1 vision, 10 bad luck, 1.5x counter damage)\n"));
      infoPages.add(new InfoPage(new EnhancedVision(null, null),
            "Increases the vision range of all units by 1 space and allows them to see into woods and reefs.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new CounterBreak(null, null),
            "Increases the vision range of all units by 1 space and allows them to see into woods and reefs. Counterattacks are stronger.\n"
          + "(Counterattacks happen before the attacker's attack)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Computers\n"
          + "Miss: Bugs"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

  private static class EnhancedVision extends AW2Ability
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

  private static class CounterBreak extends AW2Ability
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
