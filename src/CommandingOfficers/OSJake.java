package CommandingOfficers;

import java.util.Map;

import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class OSJake extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Jake", new instantiator());
  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "Jake\r\n" + 
          "  Units (even aircraft) gain +10% attack power on plains\r\n" + 
          "Beat Down -- Land indirects gain +1 Range; Plains bonus increased by +10%\r\n" + 
          "Block Rock -- Land indirects gain +1 Range; Plains bonus increased by +30%; Vehicles gain +2 Movement\r\n"));
    }
    @Override
    public Commander create()
    {
      return new OSJake();
    }
  }
  
  private int plainsBuff = 10;

  public OSJake()
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
  public GameEventQueue initTurn(GameMap map)
  {
    this.plainsBuff = 10;
    return super.initTurn(map);
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    Unit minion = null;
    if( params.attacker.CO == this )
    {
      minion = params.attacker;
    }

    if( null != minion )
    {
      Location loc = params.combatRef.gameMap.getLocation(params.combatRef.attackerX, params.combatRef.attackerY);
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
    OSJake COcast;

    BeatDown(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (OSJake) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
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
    OSJake COcast;

    BlockRock(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (OSJake) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.plainsBuff += VALUE;
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(1);

      rangeBoost.init(COcast);

      COMovementModifier moveMod = new COMovementModifier(2);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.chassis == ChassisEnum.TANK )
          moveMod.addApplicableUnitModel(um);
      }

      COcast.addCOModifier(rangeBoost);
      myCommander.addCOModifier(moveMod);
    }
  }
}
