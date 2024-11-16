package CommandingOfficers.AWBW.LA;

import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.AW4.RuinedCommander.VeteranRank;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.StateTrackers.KillCountsTracker;
import Engine.StateTrackers.StateTracker;
import UI.UIUtils;
import Units.Unit;

public class Forsythe extends AWBWCommander
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
      super("Forsythe", UIUtils.SourceGames.AWBW, UIUtils.LA);
      infoPages.add(new InfoPage(
            "Forsythe (AWBW)\n"
          + "+10/10 stats.\n"
          + "Each unit can also level up after destroying an enemy unit, for a total of 3 levels.\n"
          + "Level 1: +5 attack; Level 2: +10 attack; Level 3: +20 attack and defense.\n"
          + "No powers.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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
    VeteranRank rank = getRank(params.attacker.unit);
    params.attackPower += rank.attack;
    params.attackPower += 10;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    VeteranRank rank = getRank(params.defender.unit);
    params.defenseSubtraction += rank.defense;
    params.defenseSubtraction += 10;
  }

  KillCountsTracker killCounts;
  public VeteranRank getRank(Unit unit)
  {
    VeteranRank rank = VeteranRank.NONE;
    int level = killCounts.getCountFor(unit);
    if( level > 2 )
      rank = VeteranRank.LEVEL3;
    else if( level > 1 )
      rank = VeteranRank.LEVEL2;
    else if( level > 0 )
      rank = VeteranRank.LEVEL1;

    return rank;
  }
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    char mark = super.getUnitMarking(unit, activeArmy);
    // Prefer non-veterancy marks
    if( '\0' != mark || this != unit.CO )
      return mark;

    return getRank(unit).mark;
  }

}
