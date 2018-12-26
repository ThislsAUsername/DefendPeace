package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.XYCoord;
import Engine.Combat.BattleInstance;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class Jake extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Jake", new instantiator());

  private int plainsBuff = 10;

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new Jake();
    }
  }

  public Jake()
  {
    super(coInfo);

    addCommanderAbility(new BeatDown(this));
    addCommanderAbility(new BlockRock(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public void initTurn(GameMap map)
  {
    this.plainsBuff = 10;
    super.initTurn(map);
  }

  public void applyCombatModifiers(BattleParams params, GameMap map)
  {
    Unit minion = null;
    if( params.attacker.CO == this )
    {
      minion = params.attacker;
    }

    if( null != minion )
    {
      Location loc = map.getLocation(params.combatRef.attackerX, params.combatRef.attackerY);
      if( loc != null && loc.getEnvironment().terrainType == TerrainType.GRASS )
      {
        params.attackFactor += plainsBuff;
      }
    }
  }

  private static class BeatDown extends CommanderAbility
  {
    private static final String NAME = "Beat Down";
    private static final int COST = 3;
    private static final int VALUE = 10;
    Jake COcast;

    BeatDown(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Jake) commander;
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      COcast.plainsBuff += VALUE;
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(1);
      rangeBoost.init(COcast);
      COcast.addCOModifier(rangeBoost);
    }
  }

  private static class BlockRock extends CommanderAbility
  {
    private static final String NAME = "Block Rock";
    private static final int COST = 6;
    private static final int VALUE = 30;
    Jake COcast;

    BlockRock(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Jake) commander;
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      COcast.plainsBuff += VALUE;
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(1);

      Map<UnitModel, UnitModel> indirects = rangeBoost.init(COcast);

      COMovementModifier moveMod = new COMovementModifier(2);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.chassis == ChassisEnum.TANK )
          moveMod.addApplicableUnitModel(um);
      }
      for( UnitModel um : indirects.values() )
      {
        if( um.chassis == ChassisEnum.TANK )
          moveMod.addApplicableUnitModel(um);
      }

      COcast.addCOModifier(rangeBoost);
      myCommander.addCOModifier(moveMod);
    }
  }
}
