package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.CommanderAbility.CostBasis;
import Engine.GameScenario;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import UI.UIUtils;
import Terrain.MapMaster;

public class Lash extends AWBWCommander
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
      super("Lash", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Lash (AWBW)\n"
          + "Units gain +10% attack for every terrain star (note: air units are unaffected by terrain).\n"));
      infoPages.add(new InfoPage(new TerrainTactics(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Movement cost for all terrain is reduced to 1, except in cold weather.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new PrimeTactics(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Terrain stars are doubled (attack and defense). Movement cost over all terrain is reduced to 1, except in cold weather.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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
    params.attackPower += params.attacker.terrainStars * 10;
  }

  private static class TerrainTactics extends AWBWAbility
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
    public void changeCombatContext(CombatContext instance)
    {
      if( instance.attacker.mods.contains(this) )
        instance.attacker.terrainStars *= 2;
      if( instance.defender.mods.contains(this) )
        instance.defender.terrainStars *= 2;
    }
  }
  private static class PrimeTactics extends AWBWAbility
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
