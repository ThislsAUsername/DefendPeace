package Engine.StateTrackers;

import java.util.ArrayList;
import java.util.HashMap;
import Engine.Army;
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

  public HashMap<Army, ArrayList<Double>> incomeRatios = new HashMap<>();

  public void startTracking(Army army, double value)
  {
    ArrayList<Double> valueList = incomeRatios.getOrDefault(army, new ArrayList<Double>());
    valueList.add(value);
    incomeRatios.put(army, valueList);
  }
  public void stopTracking(Army army, double value)
  {
    ArrayList<Double> valueList = incomeRatios.getOrDefault(army, new ArrayList<Double>());
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
      double myIncomeRatio = 0;
      for( double r : incomeRatios.get(profiteer) )
        myIncomeRatio += r;
      profiteer.money += calculateProfit(defender, myIncomeRatio);
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
