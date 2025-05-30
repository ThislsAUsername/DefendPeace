package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.DefendPeace.CyanOcean.Patch;
import CommandingOfficers.DefendPeace.RoseThorn.Strong;
import Engine.Army;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

public class TestCapture extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Strong(scn.rules);
    testCo2 = new Patch(scn.rules);
    Army[] cos = { new Army(scn, testCo1), new Army(scn, testCo2) };

    testMap = new MapMaster(cos, Terrain.Maps.FiringRange.getMapInfo());
    testGame = new GameInstance(cos, testMap);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testCapture(), "  Capture test failed.");
    testPassed &= validate(testFriendlyCapture(), "  Allied capture test failed.");
    testPassed &= validate(testCaptureHQ(), "  HQ-Capture test failed.");
    return testPassed;
  }

  private boolean testCapture()
  {
    // No problems yet.
    boolean testPassed = true;

    // Get a reference to a capturable property.
    MapLocation prop = testMap.getLocation(2, 2);

    // Make sure this location is capturable.
    testPassed &= validate( prop.isCaptureable(), "    Unexpected terrain found! Test will be invalid." );

    // Add a unit to help run the tests.
    Unit infA = addUnit(testMap, testCo1, UnitModel.TROOP, 2, 2); // On the city.

    // Start capturing the city.
    infA.initTurn(testMap);
    GameAction captureAction = new CaptureLifecycle.CaptureAction(testMap, infA, Utils.findShortestPath(infA, 2, 2, testMap));
    performGameAction( captureAction, testGame );

    // Verify that we can start and stop property capture.
    testPassed &= validate( infA.getCaptureProgress() == 10, "    Infantry does not register capture progress.");
    infA.stopCapturing();
    testPassed &= validate( infA.getCaptureProgress() == 0, "    Infantry should not register capture progress.");
    infA.initTurn(testMap);
    performGameAction( captureAction, testGame );
    testPassed &= validate( infA.getCaptureProgress() == 10, "    Infantry is not capturing, but should be.");

    // Ensure that moving resets the capture counter.
    infA.initTurn(testMap);
    performGameAction(new WaitLifecycle.WaitAction(infA, Utils.findShortestPath(infA, 2, 3, testMap)), testGame);
    testPassed &= validate( infA.getCaptureProgress() == 0, "    Infantry is still capturing after moving.");

    // Make sure we can WAIT, and resume capturing.
    infA.initTurn(testMap);
    performGameAction(new WaitLifecycle.WaitAction(infA, Utils.findShortestPath(infA, 2, 2, testMap)), testGame); // Move back onto the city.
    infA.initTurn(testMap);
    performGameAction( captureAction, testGame );
    infA.initTurn(testMap);
    performGameAction(new WaitLifecycle.WaitAction(infA, Utils.findShortestPath(infA, 2, 2, testMap)), testGame); // Wait on the city.
    testPassed &= validate( infA.getCaptureProgress() == 10, "    Infantry should not lose capture progress due to WAIT.");

    // Make sure that attacking someone else does not reset capture progress.
    Unit infB = addUnit(testMap, testCo2, UnitModel.TROOP, 1, 2); // Make an enemy adjacent to the city.
    infB.alterHealth( -80 ); // Make sure he will die without retaliating.
    infA.initTurn(testMap);
    performGameAction(new BattleLifecycle.BattleAction(testMap, infA, Utils.findShortestPath(infA, 2, 2, testMap), 1, 2), testGame); // Bop him on the head.
    testPassed &= validate( infA.getCaptureProgress() == 10, "    Infantry should not stop capturing after stationary ATTACK.");

    // See if we can actually capture this thing.
    infA.alterHealth( -60 ); // Make it take three attempts to capture the property.
    infA.initTurn(testMap);
    performGameAction( captureAction, testGame );
    testPassed &= validate( infA.getCaptureProgress() == 14, "    Infantry has wrong capture progress (" +
        infA.getCaptureProgress() + " instead of 14)." );
    infA.initTurn(testMap);
    performGameAction( captureAction, testGame );
    testPassed &= validate( infA.getCaptureProgress() == 18, "    Infantry has wrong capture progress (" +
        infA.getCaptureProgress() + " instead of 18)." );

    infA.initTurn(testMap);
    performGameAction( captureAction, testGame );
    // Verify that we now own the property, and that capture progress is reset.
    testPassed &= validate( prop.getOwner() == infA.CO, "    Infantry failed to capture the property.");
    testPassed &= validate( infA.getCaptureProgress() == 0, "    Infantry capture progress did not reset after capture." );

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);

    return testPassed;
  }

  private boolean testFriendlyCapture()
  {
    // No problems yet.
    boolean testPassed = true;

    // Get a reference to a capturable property.
    MapLocation prop = testMap.getLocation(2, 2);

    // Make sure this location is capturable.
    testPassed &= validate( prop.isCaptureable(), "    Unexpected terrain found! Test will be invalid." );

    // Make the participating COs friends
    testCo1.army.team = 0;
    testCo2.army.team = 0;
    
    // Set up for the test.
    Unit infA = addUnit(testMap, testCo1, UnitModel.TROOP, 2, 2); // On the city.
    testMap.setOwner(testCo2, prop.getCoordinates());

    // Start capturing the city.
    infA.initTurn(testMap);
    GameAction captureAction = new CaptureLifecycle.CaptureAction(testMap, infA, Utils.findShortestPath(infA, 2, 2, testMap));
    performGameAction( captureAction, testGame );

    for( int i = 0; i < 5; i++ )
    {
      performGameAction(captureAction, testGame);
      testPassed &= validate(infA.getCaptureProgress() == 0,
          "    Infantry has wrong capture progress (" + infA.getCaptureProgress() + " instead of 0).");
    }
    
    // Verify that our friend still owns the property.
    testPassed &= validate( prop.getOwner() == testCo2, "    Infantry captured allied territory.");

    // Clean up
    testMap.removeUnit(infA);
    testCo1.army.team = -1;
    testCo2.army.team = -1;
    testMap.setOwner(null, prop.getCoordinates());

    return testPassed;
  }

  private boolean testCaptureHQ()
  {
    boolean testPassed = true;

    setupTest(); // Reset test parameters to ensure it's all set up hunky-dory.

    // We loaded Firing Range, so we expect an HQ for testCo2 at location (13, 1)
    Terrain.MapLocation hq = testMap.getLocation(13, 1);
    testPassed &= validate( hq.getOwner() == testCo2, "    HQ at (13, 1) is not owned by testCo2, but should be.");
    testPassed &= validate( hq.getEnvironment().terrainType == TerrainType.HEADQUARTERS, "    HQ for testCo2 is not where expected.");

    // Add a unit to help run the tests.
    Unit mech = addUnit(testMap, testCo1, UnitModel.MECH, 13, 1); // On the HQ, just to make this easy.

    // Start capturing the HQ.
    mech.initTurn(testMap);
    GameAction captureAction = new CaptureLifecycle.CaptureAction(testMap, mech, Utils.findShortestPath(mech, 13, 1, testMap));
    performGameAction(captureAction, testGame);

    // Re-create the event so we can predict the HQ capture, but don't execute; we just want to see the resulting GameEventQueue.
    mech.initTurn(testMap);
    captureAction = new CaptureLifecycle.CaptureAction(testMap, mech, Utils.findShortestPath(mech, 13, 1, testMap));
    GameEventQueue events = captureAction.getEvents(testMap);

    // Make sure an ArmyDefeatEvent was generated as a result (the actual event test is in TestGameEvent.java).
    boolean hasDefeatEvent = false;
    for( GameEvent event : events )
    {
      if( event instanceof ArmyDefeatEvent )
      {
        hasDefeatEvent = true;
        break;
      }
    }
    testPassed &= validate( hasDefeatEvent, "    No ArmyDefeatEvent generated on HQ capture!");

    return testPassed;
  }
}
