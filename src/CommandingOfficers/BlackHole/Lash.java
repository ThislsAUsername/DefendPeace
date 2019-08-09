package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.PerfectMoveModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.Location;
import Units.UnitModel.ChassisEnum;

public class Lash extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Lash");
      infoPages.add(new InfoPage(
          "Lash\r\n" + 
          "  Attack power is boosted by +10% for every terrain star (note: aircraft are unaffected by terrain)\r\n" + 
          "Terrain Tactics -- Movement cost for all terrain is reduced to 1 (except in snow)\r\n" + 
          "Prime Tactics -- Doubles terrain stars; Movement cost over all terrain is reduced to 1 (except in snow)"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Lash(rules);
    }
  }

  private final int starBuff = 10;
  private int starMult = 1;

  public Lash(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new TerrainTactics(this));
    addCommanderAbility(new PrimeTactics(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Terrain Tactics";
    private static final int COST = 4;
    Lash COcast;
    PerfectMoveModifier moveMod;

    TerrainTactics(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Lash) commander;
      moveMod = new PerfectMoveModifier();
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(moveMod);
    }
  }

  private static class PrimeTactics extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Prime Tactics";
    private static final int COST = 7;
    Lash COcast;
    PerfectMoveModifier moveMod;

    PrimeTactics(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Lash) commander;
      moveMod = new PerfectMoveModifier();
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(moveMod);
      COcast.starMult = 2;
    }
  }
}

