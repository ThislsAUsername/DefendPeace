package CommandingOfficers;

import java.util.HashMap;

import Engine.Combat.BattleSummary;
import Engine.Combat.BattleInstance.BattleParams;
import Terrain.GameMap;
import Units.Unit;

/*
 * Cinder is based on getting an edge in the action economy, at the cost of unit health.
 */
public class TheBeast extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("The Beast", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new TheBeast();
    }
  }

  private HashMap<Unit, Integer> killCounts = new HashMap<>();

  public TheBeast()
  {
    super(coInfo);
    
    addCommanderAbility(new GwarHarHar(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  public int getVetPower(int level)
  {
    if( level > 2 )
      return 20;
    if( level > 1 )
      return 10;
    if( level > 0 )
      return 5;
    return 0;
  }
  public int getVetDef(int level)
  {
    if( level > 2 )
      return 20;
    return 0;
  }
  public void levelUnit(Unit minion)
  {
    if( killCounts.containsKey(minion) )
      killCounts.put(minion, killCounts.get(minion) + 1);
    else
      killCounts.put(minion, 1);
  }

  @Override
  public void applyCombatModifiers(BattleParams params, GameMap map)
  {
    if( params.attacker.CO == this )
    {
      Unit minion = params.attacker;

      if( null != minion && killCounts.containsKey(minion) )
        params.attackFactor += getVetPower(killCounts.get(minion));
    }

    if( params.defender.CO == this )
    {
      Unit minion = params.defender;

      if( null != minion && killCounts.containsKey(minion) )
        params.defenseFactor += getVetDef(killCounts.get(minion));
    }

  }

  @Override
  public void receiveBattleEvent(BattleSummary battleInfo)
  {
    // Determine if we were part of this fight. If so, refresh at our own expense
    Unit minion = null;
    Unit victim = null;
    if( battleInfo.attacker.CO == this )
    {
      minion = battleInfo.attacker;
      victim = battleInfo.defender;
    }
    else if( battleInfo.defender.CO == this )
    {
      minion = battleInfo.defender;
      victim = battleInfo.attacker;
    }

    if( null != minion && null != victim )
    {
      if( victim.getHP() < 1 )
      {
        levelUnit(minion);
      }
    }
  }
  
  private static class GwarHarHar extends CommanderAbility
  {
    private static final String NAME = "Gwar Har Har!";
    private static final int COST = 5;
    TheBeast COcast;

    GwarHarHar(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (TheBeast) commander;
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      for (Unit unit : COcast.units)
      {
        COcast.levelUnit(unit);
      }
    }
  }
}
