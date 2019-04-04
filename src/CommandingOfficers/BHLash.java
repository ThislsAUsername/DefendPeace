package CommandingOfficers;

import CommandingOfficers.Modifiers.PerfectMoveModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.GameMap;
import Terrain.Location;
import Units.UnitModel.ChassisEnum;

public class BHLash extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Lash", new instantiator());

  private static class instantiator extends COMaker
  {
    @Override
    public Commander create()
    {
      return new BHLash();
    }
  }

  private final int starBuff = 10;
  private int starMult = 1;

  public BHLash()
  {
    super(coInfo);

    addCommanderAbility(new TerrainTactics(this));
    addCommanderAbility(new PrimeTactics(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    starMult = 1;
    return super.initTurn(map);
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( params.attacker.CO == this && params.attacker.model.chassis != ChassisEnum.AIR_HIGH
        && params.attacker.model.chassis != ChassisEnum.AIR_LOW )
    {
      Location loc = params.combatRef.gameMap.getLocation(params.combatRef.attackerX, params.combatRef.attackerY);
      if( loc != null && loc.isCaptureable() )
      {
        params.attackFactor += starBuff * loc.getEnvironment().terrainType.getDefLevel() * starMult;
      }
    }
    if( params.defender.CO == this && params.defender.model.chassis != ChassisEnum.AIR_HIGH
        && params.defender.model.chassis != ChassisEnum.AIR_LOW )
    {
      params.terrainDefense *= starMult;
    }
  }

  private static class TerrainTactics extends CommanderAbility
  {
    private static final String NAME = "Terrain Tactics";
    private static final int COST = 4;
    BHLash COcast;
    PerfectMoveModifier moveMod;

    TerrainTactics(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BHLash) commander;
      moveMod = new PerfectMoveModifier();
      moveMod.init(COcast);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(moveMod);
    }
  }

  private static class PrimeTactics extends CommanderAbility
  {
    private static final String NAME = "Prime Tactics";
    private static final int COST = 7;
    BHLash COcast;
    PerfectMoveModifier moveMod;

    PrimeTactics(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BHLash) commander;
      moveMod = new PerfectMoveModifier();
      moveMod.init(COcast);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(moveMod);
      COcast.starMult = 2;
    }
  }
}
