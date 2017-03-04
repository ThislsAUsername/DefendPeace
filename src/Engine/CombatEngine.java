package Engine;

import Engine.Combat.BattleSummary;
import Terrain.GameMap;
import Units.Unit;

public class CombatEngine
{
  /**
   * Static function to calculate the outcome of a battle between two units. It builds an
   * object to represent the specific combat instance and returns the result it calculates.
   */
  public static BattleSummary calculateBattleResults( Unit attacker, Unit defender, GameMap map, int attackerX, int attackerY )
  {
    // Set up our combat scenario.
    BattleInstance params = new BattleInstance(attacker, defender, map, attackerX, attackerY);

    return params.calculateBattleResults();
  }

}