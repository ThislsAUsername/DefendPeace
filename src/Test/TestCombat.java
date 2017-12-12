package Test;

import CommandingOfficers.CommanderPatch;
import CommandingOfficers.CommanderStrong;
import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
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
    testPassed &= validate(testMoveAttack(), "  Move-Attack test failed.");
    testPassed &= validate(testKillLastUnit(), "  Last-unit death test failed.");
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
    performGameAction( new GameAction(mechA, 1, 1, GameAction.ActionType.ATTACK, 1, 2), testMap );

    // Check that the mech is undamaged, and that the infantry is no longer with us.
    boolean testPassed = validate(mechA.getPreciseHP() == 10, "    Attacker lost or gained health.");
    testPassed &= validate(testMap.getLocation(1, 2).getResident() == null, "    Defender is still on the map.");

    // Clean up
    testMap.removeUnit(mechA);

    return testPassed;
  }

  /** Test that units can move and attack in one turn. */
  private boolean testMoveAttack()
  {
    // Add our combatants
    Unit mechA = addUnit(testMap, testCo1, UnitEnum.MECH, 1, 1);
    Unit infB = addUnit(testMap, testCo2, UnitEnum.INFANTRY, 1, 3);

    // Execute inf- I mean, the action.
    performGameAction( new GameAction(mechA, 1, 2, GameAction.ActionType.ATTACK, 1, 3), testMap );

    // Check that the mech is undamaged, and that the infantry is no longer with us.
    boolean testPassed = validate(infB.getHP() < 10, "    Defender took no damage.");
    testPassed &= validate(mechA.getHP() < 10, "    Attacker took no damage.");

    // Clean up
    testMap.removeUnit(mechA);
    testMap.removeUnit(infB);
    testCo1.units.clear();
    testCo2.units.clear();

    return testPassed;
  }

  /** Make sure we generate a CommanderDefeatEvent when killing the final unit for a CO. */
  private boolean testKillLastUnit()
  {
    boolean testPassed = true;

    // Add our combatants
    Unit mechA = addUnit(testMap, testCo1, UnitEnum.MECH, 1, 1);
    Unit infB = addUnit(testMap, testCo2, UnitEnum.INFANTRY, 1, 2);

    // Make sure the infantry will die with one attack
    infB.damageHP(7);

    // Create the attack action so we can predict the unit will die, and his CO will therefore be defeated.
    GameAction battleAction = new GameAction(mechA, 1, 1, GameAction.ActionType.ATTACK, 1, 2);

    // Extract the resulting GameEventQueue.
    GameEventQueue events = battleAction.getGameEvents(testMap);

    // Make sure a CommanderDefeatEvent was generated as a result (the actual event test is in TestGameEvent.java).
    boolean hasDefeatEvent = false;
    for( GameEvent event : events )
    {
      if( event instanceof CommanderDefeatEvent )
      {
        hasDefeatEvent = true;
        break;
      }
    }
    testPassed &= validate( hasDefeatEvent, "    No CommanderDefeatEvent generated when losing final unit!");

    return testPassed;
  }
}
