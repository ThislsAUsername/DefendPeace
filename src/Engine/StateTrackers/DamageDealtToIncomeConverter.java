package Engine.StateTrackers;

import java.util.ArrayList;
import java.util.HashMap;
import Engine.Army;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import Units.UnitDelta;

/**
 * Commanders can sign up to receive money based on damage done to their opponents using this class.
 * <p>The amount of income is a fraction of value of the damage that was done;
 * thus, damaging more expensive units will grant more income.
 */
public class DamageDealtToIncomeConverter extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public HashMap<Army, ArrayList<Integer>> incomeRatios = new HashMap<>();

  /**
   * @param value in percent
   */
  public void startTracking(Army army, Integer value)
  {
    ArrayList<Integer> valueList = incomeRatios.getOrDefault(army, new ArrayList<>());
    valueList.add(value);
    incomeRatios.put(army, valueList);
  }
  public void stopTracking(Army army, Integer value) // Note: has to be the boxed type, since the unboxed type will change the remove() call.
  {
    ArrayList<Integer> valueList = incomeRatios.getOrDefault(army, new ArrayList<>());
    if( !valueList.contains(value) )
      return;
    valueList.remove(value);
    if( valueList.isEmpty() )
      incomeRatios.remove(army); // Removes the first occurrence, which means stacking should work correctly
  }

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
  {
    profitize(battleInfo.attacker, battleInfo.defender);
    profitize(battleInfo.defender, battleInfo.attacker);
    return null;
  }
  private void profitize(UnitDelta attacker, UnitDelta defender)
  {
    // Determine if the attacker should profit from this fight. If so, cash in on any damage done to the other guy.
    final Army profiteer = attacker.CO.army;
    if( incomeRatios.containsKey(profiteer) )
    {
      int myIncomeRatio = 0;
      for( Integer r : incomeRatios.get(profiteer) )
        myIncomeRatio += r;
      profiteer.money += calculateProfit(defender, myIncomeRatio);
    }
  }

  private static int calculateProfit(UnitDelta delta, int myIncomeRatio)
  {
    int healthLoss = delta.getHPDamage();
    int unitCost = delta.unit.getCost();
    // Do the necessary math, then round to the nearest int.
    int income = healthLoss * (unitCost / 10) * myIncomeRatio / 100;
    return income;
  }
}
