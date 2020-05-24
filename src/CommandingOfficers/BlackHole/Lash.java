package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.PerfectMoveModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.Environment;

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
  public void modifyUnitAttack(StrikeParams params)
  {
    Environment env = params.map.getEnvironment(params.attacker.x, params.attacker.y);
    params.attackPower += starBuff * env.terrainType.getDefLevel() * starMult;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.terrainStars *= starMult;
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

