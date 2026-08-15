package CommandingOfficers.Variants.BH;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.StateTrackers.KillCountsTracker;
import Engine.StateTrackers.StateTracker;
import UI.UIUtils;
import Units.Unit;

public class VonBoltLevel extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Von Bolt", UIUtils.SourceGames.VARIANTS, UIUtils.BH, "lvl");
      infoPages.add(new InfoPage(
            "Von Bolt (level down)\n"
          + "Units gain +25% attack and +25% defense.\n"
          + "Units lose 15% attack and defense on making a kill, up to 3 times.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new VonBoltLevel(rules);
    }
  }

  public VonBoltLevel(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }
  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    killCounts = StateTracker.instance(game, KillCountsTracker.class);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    int level = calcLevel(params.attacker.unit);
    params.attackPower += 25 - (15 * level);
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    int level = calcLevel(params.attacker.unit);
    params.defenseSubtraction += 25 - (15 * level);
  }

  KillCountsTracker killCounts;
  protected int calcLevel(Unit unit)
  {
    int level = killCounts.getCountFor(unit);
    if( level > 3 )
      level = 3;
    return level;
  }
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    char mark = super.getUnitMarking(unit, activeArmy);
    // Prefer non-veterancy marks, like "COU"
    if( '\0' != mark )
      return mark;
    if( this != unit.CO )
      return mark;

    int level = calcLevel(unit);
    if( level < 1 )
      return '\0';
    return (char) ('1' + level - 1);
  }

}
