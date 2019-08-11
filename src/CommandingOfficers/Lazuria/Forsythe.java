package CommandingOfficers.Lazuria;

import Engine.GameScenario;
import java.util.HashMap;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.BattleSummary;
import Engine.Combat.BattleInstance.BattleParams;
import Units.Unit;

public class Forsythe extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Forsythe");
      infoPages.add(new InfoPage(
          "--FORSYTHE--\r\n" + 
          "Units gain +10% firepower and +10% defense.\r\n" + 
          "On making a kill, units gain a level" +
          "LEVEL 1: +5% firepower\r\n" +
          "LEVEL 2: +10% firepower\r\n" +
          "LEVEL V: +20% firepower & +15% defense\r\n" +
          "NO CO POWERS"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Forsythe(rules);
    }
  }

  public Forsythe(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    new CODamageModifier(10).applyChanges(this);
    new CODefenseModifier(10).applyChanges(this);
  }

  private HashMap<Unit, Integer> killCounts = new HashMap<>();
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
      return 15;
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

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
