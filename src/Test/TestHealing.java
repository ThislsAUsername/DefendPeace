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
    int prevHP = UnitModel.MAXIMUM_HP;
    int iteration = 0;
    boolean testPassed = true; // Start out optimistic.

    while (true)
    {
      iteration++;
      victim.damageHP(25); // Hurt the victim.
      prevMoney = testCo1.army.money; // Track money.
      prevHP = victim.getHealth(); // Track HP.

      GameEventQueue events = victim.initTurn(testMap); // Make the unit try to heal itself.
      for( GameEvent event : events )
      {
        event.performEvent(testMap);
      }

      if( victim.getHealth() > prevHP )
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
      if( victim.getHealth() < 90 )
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
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Unexpected starting HP value.");

      testPassed &= validate(victim.alterHealthPercent(-7) == 0, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.health < UnitModel.MAXIMUM_HP, "    Fractional damage did nothing.");

      testPassed &= validate(victim.alterHealthPercent(3) == 0, "    Fractional healing added a whole HP.");
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Fractional healing added a whole HP.");
      testPassed &= validate(victim.health < UnitModel.MAXIMUM_HP, "    Fractional healing rounded up.");

      testPassed &= validate(victim.alterHealthPercent(42) == 0, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.health == UnitModel.MAXIMUM_HP, "    Failed overhealing didn't fill up HP.");

      testPassed &= validate(victim.alterHealth(42, false, true) == 50, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.getHealth() == 150, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.health < 150, "    alterHealthPercent rounded up when it shouldn't.");

      testPassed &= validate(victim.alterHealthPercent(42) == 0, "    Failed overhealing vs overhealed target did something.");
      testPassed &= validate(victim.getHealth() == 150, "    Failed overhealing vs overhealed target did something.");
      // I'm not sure if this case is something we specifically want, but I figured I'd document the case.
      testPassed &= validate(victim.health == 150, "    Failed overhealing vs overhealed target didn't round up.");

      testPassed &= validate(victim.alterHealthPercent(-420) == -140, "    Dropping HP didn't work.");
      testPassed &= validate(victim.getHealth() == 10, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.health == 1, "    Dropping HP produced unexpected value.");

      testPassed &= validate(victim.alterHealthPercent(-420) == 0, "    Dropping HP while at 1 worked.");
      testPassed &= validate(victim.getHealth() == 10, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.health == 1, "    Dropping HP produced unexpected value.");
    }

    {
      UnitState victim = new Unit(testCo1, testCo1.getUnitModel(UnitModel.TROOP, false));
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Unexpected starting HP value.");
      try
      {
        victim.damageHP(-7);
        testPassed = false;
        System.out.println("    damageHP() accepted a healing value.");
      }
      catch (Exception e) {} // expected case

      testPassed &= validate(victim.damageHP(7) == 0, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Fractional damage removed a whole HP.");
      testPassed &= validate(victim.health < UnitModel.MAXIMUM_HP, "    Fractional damage did nothing.");

      testPassed &= validate(victim.alterHealth(-30) == -30, "    Map damage didn't deal damage.");
      testPassed &= validate(victim.getHealth() == 70, "    Map damage did the wrong damage.");
      testPassed &= validate(victim.health < 70, "    Map damage rounded HP up.");

      testPassed &= validate(victim.damageHP(420) == -70, "    Lethal damage did the wrong amount of damage.");
      testPassed &= validate(victim.getHealth() == 0, "    Lethal damage didn't kill.");
      testPassed &= validate(victim.health == 0, "    Lethal damage didn't kill.");

      testPassed &= validate(victim.damageHP(10, true) == -10, "    Overkill didn't overkill.");
      testPassed &= validate(victim.getHealth() == -10, "    Overkill didn't overkill right.");
      testPassed &= validate(victim.health == -10, "    Overkill didn't overkill right.");

      testPassed &= validate(victim.alterHealth(420) == 110, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.health == UnitModel.MAXIMUM_HP, "    Failed overhealing didn't fill up HP.");

      victim.damageHP(7);
      testPassed &= validate(victim.alterHealth(42) == 0, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.getHealth() == UnitModel.MAXIMUM_HP, "    Overhealing allowed when not enabled.");
      testPassed &= validate(victim.health == UnitModel.MAXIMUM_HP, "    Failed overhealing didn't fill up HP.");

      victim.damageHP(7);
      testPassed &= validate(victim.alterHealth(420, true) == 420, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.getHealth() == 520, "    Overhealing failed when enabled.");
      testPassed &= validate(victim.health == 520, "    Overhealing didn't round up?");

      victim.damageHP(7);
      testPassed &= validate(victim.alterHealth(42) == 0, "    Failed overhealing vs overhealed target did something.");
      testPassed &= validate(victim.getHealth() == 520, "    Failed overhealing vs overhealed target did something.");
      // I'm not sure if this case is something we specifically want, but I figured I'd document the case.
      testPassed &= validate(victim.health == 520, "    Failed overhealing vs overhealed target didn't round up.");

      testPassed &= validate(victim.alterHealth(-900) == -510, "    Dropping HP didn't work.");
      testPassed &= validate(victim.getHealth() == 10, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.health == 1, "    Dropping HP produced unexpected value.");

      testPassed &= validate(victim.alterHealth(-42) == 0, "    Dropping HP while at 1 worked.");
      testPassed &= validate(victim.getHealth() == 10, "    Dropping HP produced unexpected value.");
      testPassed &= validate(victim.health == 1, "    Dropping HP produced unexpected value.");
    }

    return testPassed;
  }
}
