package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import java.util.Map;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
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

public class Jake extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Jake");
      infoPages.add(new InfoPage(
          "Jake\r\n" + 
          "  Units (even aircraft) gain +10% attack power on plains\r\n" + 
          "Beat Down -- Land indirects gain +1 Range; Plains bonus increased by +10%\r\n" + 
          "Block Rock -- Land indirects gain +1 Range; Plains bonus increased by +30%; Vehicles gain +2 Movement\r\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jake(rules);
    }
  }
  
  private int plainsBuff = 10;

  public Jake(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

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
    Jake COcast;

    BeatDown(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Jake) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.plainsBuff += VALUE;
      COcast.addCOModifier(new IndirectRangeBoostModifier(1));
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
    protected void perform(MapMaster gameMap)
    {
      COcast.plainsBuff += VALUE;

      COMovementModifier moveMod = new COMovementModifier(2);

      for( UnitModel um : COcast.unitModels.values() )
      {
        if( um.chassis == ChassisEnum.TANK )
          moveMod.addApplicableUnitModel(um);
      }

      COcast.addCOModifier(new IndirectRangeBoostModifier(1));
      myCommander.addCOModifier(moveMod);
    }
  }
}

