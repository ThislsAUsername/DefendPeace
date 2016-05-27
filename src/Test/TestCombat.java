package Test;

import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Engine.GameAction;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestCombat extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static GameMap testMap;

  /** Make two COs and a GameMap to use with this test case. */
  private void setupTest()
  {
    testCo1 = new CmdrStrong();
    testCo2 = new Commander();
    Commander[] cos = { testCo1, testCo2 };

    // TODO: This will have to change once GameMap doesn't build a default map.
    testMap = new GameMap(cos);

    // Remove the default units. TODO: Remove this once there isn't a default map.
    testMap.getLocation(6, 5).setResident(null);
    testMap.getLocation(8, 4).setResident(null);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testUnitDeath(), "  Unit death test failed.");
    return testPassed;
  }

  /** Test that units actually die, and don't counter-attack when they are killed. */
  private boolean testUnitDeath()
  {
    // Add our combatants
    Unit mechA = addUnit(testMap, testCo1, UnitEnum.MECH, 1, 1);
    Unit infB = addUnit(testMap, testCo2, UnitEnum.INFANTRY, 1, 2);

    // Make sure the infantry will die with one attack
    infB.damageHP(7);

    // Execute inf- I mean, the action.
    new GameAction(mechA, 1, 1, GameAction.ActionType.ATTACK, 1, 2).execute(testMap);

    // Check that the mech is undamaged, and that the infantry is no longer with us.
    boolean testPassed = validate(mechA.getPreciseHP() == 10, "    Attacker lost or gained health.");
    testPassed &= validate(testMap.getLocation(1, 2).getResident() == null, "    Defender is still on the map.");

    // Clean up
    testMap.removeUnit(mechA);

    return testPassed;
  }
}
