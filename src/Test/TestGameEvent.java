package Test;

import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLibrary;
import Units.Unit;
import Units.UnitModel.UnitEnum;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderPatch;
import CommandingOfficers.CommanderStrong;
import Engine.GameAction;
import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;

public class TestGameEvent extends TestCase
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
    testPassed &= validate( testBattleEvent(), "  BattleEvent test failed.");
    testPassed &= validate( testCaptureEvent(), "  CaptureEvent test failed.");
    
    return testPassed;
  }

  private boolean testBattleEvent()
  {
    boolean testPassed = true;

    // Add our combatants
    Unit infA = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 1, 1);
    Unit infB = addUnit(testMap, testCo2, UnitEnum.INFANTRY, 1, 2);

    BattleEvent event = new BattleEvent(infA, infB, 2, 2, testMap);
    event.performEvent(testMap);
    testPassed &= validate( infB.getHP() < 10, "    Defender Was not damaged" );
    testPassed &= validate( infA.getHP() < 10, "    Defender did not counter-attack" );

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);

    return testPassed;
  }
  
  private boolean testCaptureEvent()
  {
    boolean testPassed = true;

    // We loaded Firing Range, so we expect a city at location (2, 2)
    Terrain.Location city = testMap.getLocation(2, 2);
    testPassed &= validate( city.getEnvironment().terrainType == Environment.Terrains.CITY, "    No city at (2, 2).");
    testPassed &= validate( city.getOwner() == null, "    City should not be owned by any CO yet.");

    // Add a unit
    Unit infA = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 2, 2);
    testPassed &= validate( infA.getCaptureProgress() == 0, "    Infantry capture progress is not 0." );

    // Create a new event, and ensure it does not predict full capture in one turn.
    CaptureEvent captureEvent = new CaptureEvent(infA, city);
    testPassed &= validate( captureEvent.willCapture() == false, "    Event incorrectly predicts capture will succeed.");
    // NOTE: The prediction will be unreliable after performing the event. I'm re-using it here for convenience, but
    //       GameEvents are really designed to be single-use.

    captureEvent.performEvent(testMap);
    testPassed &= validate( infA.getCaptureProgress() == 10, "    Infantry capture progress is not 10." );

    // Hurt the unit so he won't captre as fast.
    infA.damageHP(5.0);
    captureEvent.performEvent(testMap);
    testPassed &= validate( infA.getCaptureProgress() == 15, "    Infantry capture progress is not 15." );

    // Move the unit; he should lose his capture progress.
    GameAction moveAction = new GameAction(infA, 1, 2, GameAction.ActionType.WAIT);
    performGameAction(moveAction, testMap);
    GameAction moveAction2 = new GameAction(infA, 2, 2, GameAction.ActionType.WAIT);
    performGameAction(moveAction2, testMap);

    // 5, 10, 15
    captureEvent.performEvent(testMap);
    testPassed &= validate( infA.getCaptureProgress() == 5, "    Infantry capture progress should be 5, not " + infA.getCaptureProgress() + "." );
    captureEvent.performEvent(testMap);
    testPassed &= validate( infA.getCaptureProgress() == 10, "    Infantry capture progress should be 10, not " + infA.getCaptureProgress() + "." );
    captureEvent.performEvent(testMap);
    testPassed &= validate( infA.getCaptureProgress() == 15, "    Infantry capture progress should be 15, not " + infA.getCaptureProgress() + "." );

    // Recreate the captureEvent so we can check the prediction again.
    captureEvent = new CaptureEvent( infA, city );
    testPassed &= validate( captureEvent.willCapture() == true, "    Event incorrectly predicts failure to capture.");
    captureEvent.performEvent(testMap);
    testPassed &= validate( infA.getCaptureProgress() == 0, "    Infantry capture progress should be 0 again." );
    testPassed &= validate( city.getOwner() == infA.CO, "    City is not owned by the infantry's CO, but should be.");

    // Clean up
    testMap.removeUnit(infA);

    return testPassed;
  }
}
