package Engine.Combat;

import Units.UnitContext;
import Units.UnitDelta;

/**
 * This class simply provides information describing a battle, and is used more like a C-style struct than an object.
 * <p>Giving the start and end states allows consuming code to be very specific on what it cares about.
 */
public class BattleSummary
{
  public final UnitDelta attacker;
  public final UnitDelta defender;

  public BattleSummary(UnitContext attackerStart, UnitContext attackerEnd, UnitContext defenderStart, UnitContext defenderEnd)
  {
    this.attacker = new UnitDelta(attackerStart, attackerEnd);
    this.defender = new UnitDelta(defenderStart, defenderEnd);
  }
}
