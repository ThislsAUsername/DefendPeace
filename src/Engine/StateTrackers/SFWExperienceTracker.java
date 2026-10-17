package Engine.StateTrackers;

import java.awt.Color;
import java.util.HashMap;

import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Units.Unit;
import Units.UnitDelta;
import Units.UnitModel;
import lombok.var;

/**
 * See documentation in AncientCommander
 */
public class SFWExperienceTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public static final int MAX_EXP = 4000;
  public static enum SFWRank
  {
    LEVEL4('V', 80, 40, MAX_EXP, Color.green),
    LEVEL3('3', 60, 20, 2000, Color.yellow),
    LEVEL2('Ⅱ', 40,  0, 1000, Color.red),
    LEVEL1('I', 20,  0,  500, Color.pink),
    NONE('\0',   0,  0,    0, Color.white);
    public final char mark;
    public final int attack, defense, exp;
    public final Color expColor;
    private SFWRank(char mark, int attack, int defense, int exp, Color expColor)
    {
      this.mark     = mark;
      this.attack   = attack;
      this.defense  = defense;
      this.exp      = exp;
      this.expColor = expColor;
    }
  }
  public SFWRank getRank(Unit unit)
  {
    int exp = getExperience(unit);
    for( SFWRank rank : SFWRank.values() )
      if( exp >= rank.exp )
        return rank;
    return SFWRank.NONE; // shouldn't be hit
  }

  public HashMap<Unit, Integer> experience = new HashMap<>();

  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent event)
  {
    int donorXP = 0;
    if( experience.containsKey(event.unitDonor) )
      donorXP = experience.remove(event.unitDonor);
    if( donorXP > getExperience(event.unitRecipient) )
      experience.put(event.unitRecipient, donorXP);
    return null;
  };
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
  {
    experience.remove(victim);
    return null;
  }

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
  {
    experiencize(battleInfo.attacker, battleInfo.defender);
    experiencize(battleInfo.defender, battleInfo.attacker);
    return null;
  }
  private void experiencize(UnitDelta attacker, UnitDelta defender)
  {
    int expPerPercent = getExperienceRate(defender);
    int healthDamage = defender.getPreciseHealthDamage();

    int profit = expPerPercent * healthDamage;
    addExperience(attacker.unit, profit);
  }

  public int getExperienceRate(UnitDelta defender)
  {
    int defenderCost = defender.CO.getCost(defender.model);
    int expPerPercent = 1;
    if( defenderCost > 30000 )
      expPerPercent = 6;
    else if( defenderCost > 18000 )
      expPerPercent = 5;
    else if( defenderCost > 13000 )
      expPerPercent = 4;
    else if( defenderCost >  8000 )
      expPerPercent = 3;
    else if( defenderCost >  5000 )
      expPerPercent = 2;
    if( defender.model.isAny(UnitModel.AIR | UnitModel.SEA) )
      expPerPercent -= 1;
    return expPerPercent;
  }

  public int getExperience(Unit profiteer)
  {
    return experience.getOrDefault(profiteer, 0);
  }
  public int addExperience(Unit profiteer, int profit)
  {
    int xp = experience.getOrDefault(profiteer, 0);
    int finalVal = xp + profit;
    if( finalVal > MAX_EXP )
      finalVal = MAX_EXP;
    experience.put(profiteer, finalVal);

    return finalVal;
  }
  @Override
  public CustomStatData getCustomStat(Unit unit)
  {
    var rank = getRank(unit);
    int exp  = getExperience(unit) - rank.exp; // should be positive
    exp  /= 10; // Hide the lowest digit since it would reveal HP digits
    String text = "" + exp ;
    return new CustomStatData('E', rank.expColor, Color.white, text);
  }

}
