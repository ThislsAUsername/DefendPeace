package CommandingOfficers.AW3.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW3.AW3Commander;
import CommandingOfficers.DefendPeace.misc.Venge;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.StateTrackers.DSSonjaDebuffTracker;
import Engine.StateTrackers.StateTracker;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.VisionModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Sonja extends AW3Commander
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
      super("Sonja", UIUtils.SourceGames.AW3, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Sonja (AWDS)\n"
          + "Kanbei’s calm and collected daughter who plans before acting. Excels in information warfare.\n"
          + "All units have extended vision ranges in Fog of War. Manipulates information to reduce enemy terrain effects by one.\n"
          + "(+1 vision, -1 terrain star to enemies even when not fighting her, 5 bad luck)\n"));
      infoPages.add(new InfoPage(new EnhancedVision(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Increases the vision of units by one space and allows them to see into woods and reefs. Reduces enemy terrain effects by two.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new CounterBreak(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Increases vision of units by one space and allows them to see into woods and reefs. Counter-attacks are stronger. Reduces enemy terrain effects by three.\n"
          + "(Counterattacks happen before the attacker's attack)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Computers\n"
          + "Miss: Bugs"));
      infoPages.add(AW3_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sonja(rules);
    }
  }

  public int terrainDebuff = 1;
  private DSSonjaDebuffTracker debuffMod;

  public Sonja(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new EnhancedVision(this, cb));
    addCommanderAbility(new CounterBreak(this, cb));
  }
  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    debuffMod = StateTracker.instance(game, DSSonjaDebuffTracker.class);
    debuffMod.debuffers.add(this);
  }

  @Override
  public void modifyVision(UnitContext uc)
  {
    uc.visionRange += 1;
  }

  private static class EnhancedVision extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Enhanced Vision";
    private static final int COST = 3;
    private final Sonja COcast;
    UnitModifier sightMod;

    EnhancedVision(Sonja commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      COcast = commander;
      sightMod = new VisionModifier(1);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      COcast.terrainDebuff = 2;
      COcast.debuffMod.recalcDebuffs();
    }
    @Override
    protected void revert(MapMaster gameMap)
    {
      COcast.terrainDebuff = 1;
      COcast.debuffMod.recalcDebuffs();
    }
  }

  private static class CounterBreak extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Counter Break";
    private static final int COST = 5;
    private final Sonja COcast;
    UnitModifier sightMod, counterMod;

    CounterBreak(Sonja commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      COcast = commander;
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

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      COcast.terrainDebuff = 3;
      COcast.debuffMod.recalcDebuffs();
    }
    @Override
    protected void revert(MapMaster gameMap)
    {
      COcast.terrainDebuff = 1;
      COcast.debuffMod.recalcDebuffs();
    }
  }

}
