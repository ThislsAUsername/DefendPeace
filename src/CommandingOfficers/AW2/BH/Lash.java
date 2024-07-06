package CommandingOfficers.AW2.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
import CommandingOfficers.CommanderAbility.CostBasis;
import Engine.GameScenario;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import UI.UIUtils;
import Units.UnitContext;
import Terrain.MapMaster;

public class Lash extends AW2Commander
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
      super("Lash", UIUtils.SourceGames.AW2, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Lash (AW2)\n"
          + "The wunderkind of the Black Hole forces. A small, fierce, and brilliant inventor.\n"
          + "Skilled at taking advantage of terrain features. Can turn terrain effects into firepower bonuses.\n"
          + "(1.Yx damage for non-air units, where Y is terrain stars)"));
      infoPages.add(new InfoPage(new TerrainTactics(null, new CostBasis(CHARGERATIO_FUNDS)),
            "In addition to using terrain effects to increase firepower, drops movement cost for all units to 1.\n"
          + "(...except in cold weather)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new PrimeTactics(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Terrain effects are doubled and are used to increase attack strength. Additionally, movement cost for all units drops to 1.\n"
          + "(...except in cold weather)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Getting her way\n"
          + "Miss: Not getting it"));
      infoPages.add(AW2_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Lash(rules);
    }
  }

  public Lash(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new TerrainTactics(this, cb));
    addCommanderAbility(new PrimeTactics(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAirUnit() )
      return;
    final int multiplier = 100 + params.attacker.terrainStars * 10;
    params.attackerDamageMultiplier *= multiplier;
    params.attackerDamageMultiplier /= 100;
  }

  private static class TerrainTactics extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Terrain Tactics";
    private static final int COST = 4;
    UnitModifier moveMod;

    TerrainTactics(Lash commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new SturmValueFinders.PerfectMoveModifier();
      AIFlags = 0; // Whyyyyy does this cost 4?
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

  public static class DoubleTerrainModifier implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void changeCombatContext(CombatContext instance, UnitContext buffOwner)
    {
      buffOwner.terrainStars *= 2;
    }
  }
  private static class PrimeTactics extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Prime Tactics";
    private static final int COST = 7;
    UnitModifier moveMod, defMod;

    PrimeTactics(Lash commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new SturmValueFinders.PerfectMoveModifier();
      defMod  = new DoubleTerrainModifier();
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(defMod);
    }
  }

}
