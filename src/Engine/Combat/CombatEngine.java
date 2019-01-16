package Engine.Combat;

import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class CombatEngine
{
  /**
   * Static function to calculate the outcome of a battle between two units. It builds an
   * object to represent the specific combat instance and returns the result it calculates.
   * Requires perfect map info just in case the COs need to get weird.
   */
  public static BattleSummary calculateBattleResults( Unit attacker, Unit defender, MapMaster map, int attackerX, int attackerY )
  {
    // Set up our combat scenario.
    BattleInstance params = new BattleInstance(attacker, defender, map, attackerX, attackerY, false);

    return params.calculateBattleResults();
  }

  /**
   * Assuming Commanders get weird, this allows for you to check the results of combat without perfect map info.
   * This also provides un-capped damage estimates, so perfect HP info isn't revealed by the map.
   */
  public static BattleSummary simulateBattleResults( Unit attacker, Unit defender, GameMap map, int attackerX, int attackerY )
  {
    // Set up our combat scenario.
    BattleInstance params = new BattleInstance(attacker, defender, map, attackerX, attackerY, true);

    return params.calculateBattleResults();
  }

}