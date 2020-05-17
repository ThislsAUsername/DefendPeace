package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;

public class Koal extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Koal");
      infoPages.add(new InfoPage(
          "Koal\r\n" +
          "  Units (even aircraft) gain +10% attack power on roads\r" +
          "xxxXX\n" +
          "Forced March -- All units gain +1 move; Road bonus increased by +10%\r\n" +
          "Trail of Woe -- All units gain +2 move; Road bonus increased by +20%\r\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Koal(rules);
    }
  }
  
  private int roadBuff = 10;

  public Koal(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new RoadRage(this, "Forced March", 3, 1, 10));
    addCommanderAbility(new RoadRage(this, "Trail of Woe", 5, 2, 20));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.roadBuff = 10;
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
      if( loc != null && loc.getEnvironment().terrainType == TerrainType.ROAD )
      {
        params.attackFactor += roadBuff;
      }
    }
  }

  private static class RoadRage extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private final int ROAD_POWER;
    private final int MOVE_BUFF;
    Koal COcast;

    RoadRage(Commander commander, String name, int cost, int move, int roadPower)
    {
      super(commander, name, cost);
      COcast = (Koal) commander;
      MOVE_BUFF = move;
      ROAD_POWER = roadPower;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.roadBuff += ROAD_POWER;
      COcast.addCOModifier(new COMovementModifier(MOVE_BUFF));
    }
  }

}

