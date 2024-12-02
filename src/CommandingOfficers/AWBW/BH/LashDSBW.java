package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
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

public class LashDSBW extends AWBWCommander
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
      super("Lash", UIUtils.SourceGames.AWBW, UIUtils.BH, "3BW");
      infoPages.add(new InfoPage(
            "AWDS Lash for AWBW\n"
          + "Lash version with a weaker D2D, that isn't slowed by snow during powers.\n"
          + "+5 attack per terrain star for non-air units"));
      infoPages.add(new InfoPage(new TerrainTactics(null, null),
            "Drops movement cost for all units to 1 (in all weather).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new PrimeTactics(null, null),
            "Terrain effects are doubled. Additionally, movement cost for all units drops to 1 (in all weather).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new LashDSBW(rules);
    }
  }

  public LashDSBW(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
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
  private static class TerrainTactics extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Terrain Tactics";
    private static final int COST = 4;
    UnitModifier moveMod;

    TerrainTactics(LashDSBW commander, CostBasis basis)
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

  private static class PrimeTactics extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Prime Tactics";
    private static final int COST = 7;
    UnitModifier moveMod, defMod;

    PrimeTactics(LashDSBW commander, CostBasis basis)
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
