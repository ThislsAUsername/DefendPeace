package CommandingOfficers;

import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.CaptureUnitValueFinder;
import Engine.Combat.CostValueFinder;
import Engine.Combat.HPValueFinder;
import Engine.Combat.MassStrikeUtils;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class OSRachel extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Rachel", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new OSRachel();
    }
  }

  private int luckMax = 10;

  public OSRachel()
  {
    super(coInfo);

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
    OSRachel COcast;

    LuckyLass(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (OSRachel) commander;
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
