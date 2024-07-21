package CommandingOfficers.AW3.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW2.BH.Lash.DoubleTerrainModifier;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import UI.UIUtils;
import Units.UnitContext;
import Units.MoveTypes.MoveType;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;

public class Lash extends AW3Commander
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
      super("Lash", UIUtils.SourceGames.AW3, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Lash (AW3)\n"
          + "The wunderkind of the Black Hole forces. She's short, but fierce. Invented most of Black Hole's weapons.\n"
          + "Skilled at taking advantage of terrain features. Can turn terrain effects into firepower bonuses.\n"
          + "(+5 attack per terrain star for non-air units)"));
      infoPages.add(new InfoPage(new TerrainTactics(null, new CostBasis(CHARGERATIO_AW3)),
            "In addition to using terrain effects to increase firepower, drops movement cost for all units to 1 (in all weather).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new PrimeTactics(null, new CostBasis(CHARGERATIO_AW3)),
            "Terrain effects are doubled and are used to increase attack strength. Additionally, movement cost for all units drops to 1 (in all weather).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Getting her way\n"
          + "Miss: Not getting it"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new TerrainTactics(this, cb));
    addCommanderAbility(new PrimeTactics(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAirUnit() )
      return;
    params.attackPower += params.attacker.terrainStars * 5;
  }

  // DS Lash isn't slowed in weather
  public static class PerfectMoveModifier implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void modifyMoveType(UnitContext uc)
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        final int moveCost = uc.moveType.getMoveCost(Weathers.CLEAR, terrain);
        // Non-impassable, non-teleport tiles
        if( MoveType.IMPASSABLE > moveCost && moveCost > 0 )
          uc.moveType.setMoveCost(terrain, 1);
      }
    }
  }
  private static class TerrainTactics extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Terrain Tactics";
    private static final int COST = 4;
    UnitModifier moveMod;

    TerrainTactics(Lash commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new PerfectMoveModifier();
      AIFlags = 0; // Whyyyyy does this cost 4?
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

  private static class PrimeTactics extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Prime Tactics";
    private static final int COST = 7;
    UnitModifier moveMod, defMod;

    PrimeTactics(Lash commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new PerfectMoveModifier();
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
