package CommandingOfficers;

import java.util.HashMap;

import Engine.Combat.BattleSummary;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class DavisLevels extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Davis", new instantiator());
  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "--DAVIS--\r\n" + 
          "A unit will level up upon killing an enemy unit, for a total of three levels.\r\n" + 
          "LEVEL 1: +5% firepower\r\n" + 
          "LEVEL 2: +10% firepower\r\n" + 
          "LEVEL V: +20% firepower & +20% defense\r\n" + 
          "xxxXXXXX\r\n" + 
          "BOOT CAMP: Any units produced will start at Level 2.\r\n" + 
          "HANGING THREATS: All units gain one level."));
    }
    @Override
    public Commander create()
    {
      return new DavisLevels();
    }
  }

  private HashMap<Unit, Integer> killCounts = new HashMap<>();
  public boolean buildNoobs = true;

  public DavisLevels()
  {
    super(coInfo);

    addCommanderAbility(new LevelProduction(this));
    addCommanderAbility(new LevelAll(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }


  @Override
  public char getUnitMarking(Unit unit)
  {
    if (killCounts.containsKey(unit))
    {
      int level = killCounts.get(unit);
      if( level > 2 )
        return 'V';
      if( level > 1 )
        return '2';
      if( level > 0 )
        return 'I';
    }
    return super.getUnitMarking(unit);
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
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
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
    super.receiveBattleEvent(battleInfo);
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
  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    buildNoobs = true;
    return super.initTurn(map);
  }

  @Override
  public void receiveCreateUnitEvent(Unit unit)
  {
    if( this == unit.CO && !buildNoobs)
    {
      killCounts.put(unit, 2);
    }
  }
  
  private static class LevelProduction extends CommanderAbility
  {
    private static final String NAME = "Boot Camp";
    private static final int COST = 3;
    DavisLevels COcast;

    LevelProduction(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (DavisLevels) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.buildNoobs = false;
    }
  }
  
  private static class LevelAll extends CommanderAbility
  {
    private static final String NAME = "Hanging Threats";
    private static final int COST = 8;
    DavisLevels COcast;

    LevelAll(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (DavisLevels) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for (Unit unit : COcast.units)
      {
        COcast.levelUnit(unit);
      }
    }
  }
}
