package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import Engine.Army;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapInfo;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitState;

public class TestHealing extends TestCase
{
  private static Commander testCo1;
  private static MapMaster testMap;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Patch(scn.rules);
    Army[] cos = { new Army(scn, testCo1), };
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
    testPassed &= validate(testHealthChangeFunctions(), "  UnitState health change test failed.");
    return testPassed;
  }

  /** Make sure an Infantry unit heals correctly when passing time on an owned city. */
  private boolean testHealing()
  {
    Unit victim = addUnit(testMap, testCo1, UnitModel.TROOP, 0, 0);
    // Assuming 200/turn for healing infantry: 999\200 = 4, plus one healing after that = 5,
    //    plus another iteration to check poverty conditions = 6 iterations
    testCo1.army.money = 999;

    // Set up starting conditions.
    int prevMoney = 999;
    int prevHP = 10;
    int iteration = 0;
    boolean testPassed = true; // Start out optimistic.

    while (true)
    {
      iteration++;
      victim.damageHP(2.5); // Hurt the victim.
      prevMoney = testCo1.army.money; // Track money.
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
        testPassed &= validate(testCo1.army.money < prevMoney, "    Commander funds did not change despite healing unit.");
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

  /** Test the UnitState endpoints for health changes */
  private boolean testHealthChangeFunctions()
  {
    boolean testPassed = true;

    {
      UnitState victim = new Unit(testCo1, testCo1.getUnitModel(UnitModel.TROOP, false));
      testPassed &= validate(victim.getHP() == 10, "    Unexpected starting HP value.");

      testPassed &= validate(victim.alterHealthPercent(-7) == 0, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.getHP() == 10, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.getPreciseHP() < 10, "    Fractional damage did nothing.");

      testPassed &= validate(victim.alterHealthPercent(3) == 0, "    Fractional healing added a whole HP.");
      testPassed &= validate(victim.getHP() == 10, "    Fractional healing added a whole HP.");
      testPassed &= validate(victim.getPreciseHP() < 10, "    Fractional healing rounded up.");

      testPassed &= validate(victim.alterHealthPercent(42) == 0, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getHP() == 10, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getPreciseHP() == 10, "    Failed overhealing didn't fill up HP.");

      testPassed &= validate(victim.alterHealthPercent(42, true) == 5, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.getHP() == 15, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.getPreciseHP() < 15, "    alterHealthPercent rounded up when it shouldn't.");

      testPassed &= validate(victim.alterHealthPercent(42) == 0, "    Failed overhealing vs overhealed target did something.");
      testPassed &= validate(victim.getHP() == 15, "    Failed overhealing vs overhealed target did something.");
      // I'm not sure if this case is something we specifically want, but I figured I'd document the case.
      testPassed &= validate(victim.getPreciseHP() == 15, "    Failed overhealing vs overhealed target didn't round up.");

      testPassed &= validate(victim.alterHealthPercent(-420) == -14, "    Dropping HP didn't work.");
      testPassed &= validate(victim.getHP() == 1, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.getPreciseHP() == 0.1, "    Dropping HP produced unexpected value.");

      testPassed &= validate(victim.alterHealthPercent(-420) == 0, "    Dropping HP while at 1 worked.");
      testPassed &= validate(victim.getHP() == 1, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.getPreciseHP() == 0.1, "    Dropping HP produced unexpected value.");
    }

    {
      UnitState victim = new Unit(testCo1, testCo1.getUnitModel(UnitModel.TROOP, false));
      testPassed &= validate(victim.getHP() == 10, "    Unexpected starting HP value.");
      try
      {
        victim.damageHP(-0.7);
        testPassed = false;
        System.out.println("    damageHP() accepted a healing value.");
      }
      catch (Exception e) {} // expected case

      testPassed &= validate(victim.damageHP(0.7) == 0, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.getHP() == 10, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.getPreciseHP() < 10, "    Fractional damage did nothing.");

      testPassed &= validate(victim.alterHP(-3) == -3, "    Map damage didn't deal damage.");
      testPassed &= validate(victim.getHP() == 7, "    Map damage did the wrong damage.");
      testPassed &= validate(victim.getPreciseHP() < 7, "    Map damage rounded HP up.");

      testPassed &= validate(victim.damageHP(420) == -7, "    Lethal damage did the wrong amount of damage.");
      testPassed &= validate(victim.getHP() == 0, "    Lethal damage didn't kill.");
      testPassed &= validate(victim.getPreciseHP() == 0, "    Lethal damage didn't kill.");

      testPassed &= validate(victim.damageHP(1, true) == -1, "    Overkill didn't overkill.");
      testPassed &= validate(victim.getHP() == -1, "    Overkill didn't overkill right.");
      testPassed &= validate(victim.getPreciseHP() == -1, "    Overkill didn't overkill right.");

      testPassed &= validate(victim.alterHP(42) == 11, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getHP() == 10, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getPreciseHP() == 10, "    Failed overhealing didn't fill up HP.");

      victim.damageHP(0.7);
      testPassed &= validate(victim.alterHP(42) == 0, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getHP() == 10, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getPreciseHP() == 10, "    Failed overhealing didn't fill up HP.");

      victim.damageHP(0.7);
      testPassed &= validate(victim.alterHP(42, true) == 42, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.getHP() == 52, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.getPreciseHP() == 52, "    Overhealing didn't round up?");

      victim.damageHP(0.7);
      testPassed &= validate(victim.alterHP(42) == 0, "    Failed overhealing vs overhealed target did something.");
      testPassed &= validate(victim.getHP() == 52, "    Failed overhealing vs overhealed target did something.");
      // I'm not sure if this case is something we specifically want, but I figured I'd document the case.
      testPassed &= validate(victim.getPreciseHP() == 52, "    Failed overhealing vs overhealed target didn't round up.");

      testPassed &= validate(victim.alterHP(-9000) == -51, "    Dropping HP didn't work.");
      testPassed &= validate(victim.getHP() == 1, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.getPreciseHP() == 0.1, "    Dropping HP produced unexpected value.");

      testPassed &= validate(victim.alterHP(-420) == 0, "    Dropping HP while at 1 worked.");
      testPassed &= validate(victim.getHP() == 1, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.getPreciseHP() == 0.1, "    Dropping HP produced unexpected value.");
    }

    return testPassed;
  }
}
