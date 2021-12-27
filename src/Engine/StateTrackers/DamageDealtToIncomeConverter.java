package Engine.StateTrackers;

import java.util.ArrayList;
import java.util.HashMap;
import CommandingOfficers.Commander;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import Units.UnitDelta;
import Units.UnitModel;

/**
 * Commanders can sign up to receive money based on damage done to their opponents using this class.
 * <p>The amount of income is a fraction of value of the damage that was done;
 * thus, damaging more expensive units will grant more income.
 */
public class DamageDealtToIncomeConverter extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public HashMap<Commander, ArrayList<Double>> incomeRatios = new HashMap<>();

  public void startTracking(Commander co, double value)
  {
    ArrayList<Double> valueList = incomeRatios.getOrDefault(co, new ArrayList<Double>());
    valueList.add(value);
    incomeRatios.put(co, valueList);
  }
  public void stopTracking(Commander co, double value)
  {
    ArrayList<Double> valueList = incomeRatios.getOrDefault(co, new ArrayList<Double>());
    if( !valueList.contains(value) )
      return;
    valueList.remove(value);
    if( valueList.isEmpty() )
      incomeRatios.remove(co); // Removes the first occurrence, which means stacking should work correctly
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
    if( incomeRatios.containsKey(attacker.CO) )
    {
      double myIncomeRatio = 0;
      for( double r : incomeRatios.get(attacker.CO) )
        myIncomeRatio += r;
      attacker.CO.money += calculateProfit(defender, myIncomeRatio);
    }
  }

  private static int calculateProfit(UnitDelta delta, double myIncomeRatio)
  {
    double hpLoss = delta.getHPDamage();
    double unitCost = delta.unit.getCost();
    // Do the necessary math, then round to the nearest int.
    int income = (int) (hpLoss * (unitCost / UnitModel.MAXIMUM_HP) * myIncomeRatio + 0.5);
    return income;
  }
}
