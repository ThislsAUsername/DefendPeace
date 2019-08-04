package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import Engine.Combat.BattleInstance.BattleParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Combat.CaptureUnitValueFinder;
import Engine.Combat.CostValueFinder;
import Engine.Combat.HPValueFinder;
import Engine.Combat.MassStrikeUtils;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class Rachel extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Rachel");
      infoPages.add(new InfoPage(
          "Rachel\r\n" + 
          "  +1 HP for repairs (liable for costs)\r\n" + 
          "Lucky Lass -- Improves Luck (0-40%)\r\n" + 
          "Covering Fire -- Three 2-range missiles strike the opponents' greatest accumulation of footsoldier HP, unit funds value, and unit HP (in that order)."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Rachel(rules);
    }
  }

  private int luckMax = 10;

  public Rachel(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new LuckyLass(this));
    addCommanderAbility(new CoveringFire(this));
  }

  @Override
  public int getRepairPower()
  {
    return 3;
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    this.luckMax = 10;
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
      params.luckMax = luckMax;
    }
  }

  private static class LuckyLass extends CommanderAbility
  {
    private static final String NAME = "Lucky Lass";
    private static final int COST = 3;
    private static final int VALUE = 40;
    Rachel COcast;

    LuckyLass(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Rachel) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.luckMax = VALUE;
    }
  }

  private static class CoveringFire extends CommanderAbility
  {
    private static final String NAME = "Covering Fire";
    private static final int COST = 6;

    CoveringFire(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // inf, cost, HP in order
      // deets: https://discordapp.com/channels/313453805150928906/314370192098459649/392908214913597442
      MassStrikeUtils.missileStrike(gameMap, MassStrikeUtils.findValueConcentration(gameMap, 2, new CaptureUnitValueFinder(myCommander,false)));
      MassStrikeUtils.missileStrike(gameMap, MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander,true)));
      MassStrikeUtils.missileStrike(gameMap, MassStrikeUtils.findValueConcentration(gameMap, 2, new HPValueFinder(myCommander,true)));
    }
  }
}

