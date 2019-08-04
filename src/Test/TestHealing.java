package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapInfo;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestHealing extends TestCase
{
  private static Commander testCo1;
  private static MapMaster testMap;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Patch(scn.rules);
    Commander[] cos = { testCo1 };
    // Create a small map with a city to provide healing.
    TerrainType[][] testLoc = {
        {TerrainType.CITY, TerrainType.GRASS},
        {TerrainType.GRASS, TerrainType.GRASS}};
    XYCoord[] co1Props = { new XYCoord(0, 0) }; // Assign the city to our CO.
    XYCoord[][] properties = { co1Props }; // Wrap with an array to match MapInfo interface.

    testMap = new MapMaster(cos, new MapInfo("Healing Test", testLoc, properties));
  }

  @Override
  public boolean runTest()
  {
    setupTest();
    boolean testPassed = validate(testHealing(), "  Healing test failed.");
    return testPassed;
  }

  /** Make sure an Infantry unit heals correctly when passing time on an owned city. */
  private boolean testHealing()
  {
    Unit victim = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 0, 0);
    // Assuming 200/turn for healing infantry: 999\200 = 4, plus one healing after that = 5,
    //    plus another iteration to check poverty conditions = 6 iterations
    testCo1.money = 999;

    // Set up starting conditions.
    int prevMoney = 999;
    int prevHP = 10;
    int iteration = 0;
    boolean testPassed = true; // Start out optimistic.

    while (true)
    {
      iteration++;
      victim.damageHP(2.5); // Hurt the victim.
      prevMoney = testCo1.money; // Track money.
      prevHP = victim.getHP(); // Track HP.

      GameEventQueue events = victim.initTurn(testMap); // Make the unit try to heal itself.
      for( GameEvent event : events )
      {
        event.performEvent(testMap);
      }

      if( victim.getHP() > prevHP )
      { // If the unit was healed...
        //... then we better have had enough money to do the job 
        testPassed &= validate(prevMoney >= 100, "    Unit was not healed when he should have been.");
        //... and we had better have less money now than we did before.
        testPassed &= validate(testCo1.money < prevMoney, "    Commander funds did not change despite healing unit.");
      }
      else
      { // If the unit was not healed...
        //... then it had better be because we are poor.
        testPassed &= validate(prevMoney < 100, "    Unit was not healed despite having sufficient funds.");
      }

      // The test continues until the unit fails to heal itself due to monetary drain.
      if( victim.getHP() < 9 )
        break;
    }

    // If the loop iterated the wrong number of times, something unexpected happened.
    testPassed &= validate(iteration == 6, "    Heal test iterated an incorrect (" + iteration + ") number of times.");

    // Clean up.
    testMap.removeUnit(victim);

    return testPassed;
  }
}
