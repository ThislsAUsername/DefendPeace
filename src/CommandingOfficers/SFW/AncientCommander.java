package CommandingOfficers.SFW;

import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.StateTrackers.SFWExperienceTracker;
import Engine.StateTrackers.SFWExperienceTracker.SFWRank;
import Engine.StateTrackers.StateTracker;
import Units.Unit;

public abstract class AncientCommander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage SFW_MECHANICS_BLURB = new InfoPage(
            "Super Famicom Wars mechanics:\n"
          + "COs do not have powers, but they do have veterancy up to level 4.\n"
          + "Leveling is extremely slow, but the bonuses are also large:\n"
          + "+20/0, +40/0, +60/20, +80/40 attack/defense\n"
          + "Defense works like AW1.\n"
          + "Veterancy bonuses do not apply if this CO is tagged out, but EXP is always tracked.\n"
          + "\n"
          + "The levels require a total of 500, 1000, 2000, 4000 EXP.\n"
          + "EXP is gained based on damage dealt, scaled from (originally) 1-5 EXP per % damage.\n"
          + "EXP/% is 1 if cost <=  5k\n"
          + "EXP/% is 2 if cost <=  8k\n"
          + "EXP/% is 3 if cost <= 13k\n"
          + "EXP/% is 4 if cost <= 18k\n"
          + "EXP/% is 5 if cost <= 30k\n"
          + "EXP/% is 6 otherwise\n"
          + "-1 EXP/% for air/sea.\n"
          );

  public AncientCommander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
  }

  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    vetTracker = StateTracker.instance(game, SFWExperienceTracker.class);
  }
  @Override
  public void deInitForGame(GameInstance game)
  {
    super.deInitForGame(game);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    SFWRank rank = vetTracker.getRank(params.attacker.unit);
    params.attackPower += rank.attack;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    SFWRank rank = vetTracker.getRank(params.defender.unit);
    params.defenderDamageMultiplier *= (100 - rank.defense);
    params.defenderDamageMultiplier /= 100;
  }

  SFWExperienceTracker vetTracker;
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    char mark = super.getUnitMarking(unit, activeArmy);
    // Prefer non-veterancy marks, like "COU"
    if( '\0' != mark )
      return mark;
    if( this != unit.CO )
      return mark;

    return vetTracker.getRank(unit).mark;
  }

}
