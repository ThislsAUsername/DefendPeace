package Test;

import CommandingOfficers.CommanderPatch;
import CommandingOfficers.CommanderStrong;
import CommandingOfficers.Commander;
import Engine.GameAction;
import Terrain.GameMap;
import Terrain.MapLibrary;
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
    testCo1 = new CommanderStrong();
    testCo2 = new CommanderPatch();
    Commander[] cos = { testCo1, testCo2 };

    testMap = new GameMap(cos, MapLibrary.getByName("Firing Range"));
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testUnitDeath(), "  Unit death test failed.");
    testPassed &= validate(testIndirectAttacks(), "  Indirect combat test failed.");
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

  /** Test that weapon range works properly, and that immobile weapons cannot move and fire. */
  private boolean testIndirectAttacks()
  {
    // Add our combatants
    Unit mover = addUnit(testMap, testCo1, UnitEnum.ARTILLERY, 6, 5);
    Unit victim = addUnit(testMap, testCo2, UnitEnum.ARTILLERY, 6, 6);
    Unit shooter = addUnit(testMap, testCo2, UnitEnum.ARTILLERY, 6, 7);

    // mover will attempt to shoot point blank.
    new GameAction(mover, 6, 5, GameAction.ActionType.ATTACK, 6, 6).execute(testMap);
    
    // mover will attempt to move and fire.
    new GameAction(mover, 6, 4, GameAction.ActionType.ATTACK, 6, 6).execute(testMap);

    // shooter will shoot mover.
    new GameAction(shooter, 6, 7, GameAction.ActionType.ATTACK, 6, 5).execute(testMap);

    // Check that victim is undamaged, and mover *is* damaged.
    boolean testPassed = validate(victim.getPreciseHP() == 10, "    Artillery did a direct attack or move and fire.");
    testPassed &= validate(mover.getPreciseHP() != 10, "    Artillery cannot hit at range.");

    // Clean up
    testMap.removeUnit(mover);
    testMap.removeUnit(victim);
    testMap.removeUnit(shooter);

    return testPassed;
  }
}
