package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderPatch;
import CommandingOfficers.CommanderStrong;
import Engine.GameAction;
import Engine.Utils;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapLibrary;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestTransport extends TestCase
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

    boolean testPassed = validate(testLoadUnloadAPC(), "  Transport test failed.");
    return testPassed;
  }

  /**
   * Basic load/unload APC transport test.
   * @return
   */
  private static boolean testLoadUnloadAPC()
  {
    // Add a couple of units to drive this test.
    Unit cargo = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 4, 1);
    Unit apc = addUnit(testMap, testCo1, UnitEnum.APC, 4, 2);

    boolean testPassed = true;

    // Try a basic load/move/unload order.
    performGameAction(new GameAction.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 4, 2, testMap)), testMap);
    testPassed &= validate(testMap.getLocation(4, 2).getResident() != cargo, "    Cargo is still on the map.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC is not holding a unit.");
    performGameAction(new GameAction.UnloadAction(testMap, apc, Utils.findShortestPath(apc, 7, 3, testMap), cargo, 7, 4), testMap);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() == cargo, "    Cargo was not dropped off correctly.");
    testPassed &= validate(apc.heldUnits.isEmpty(), "    APC is not empty when it should be.");

    // Make sure we can unload a unit on the apc's current location.
    performGameAction(new GameAction.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 7, 3, testMap)), testMap);
    performGameAction(new GameAction.UnloadAction(testMap, apc, Utils.findShortestPath(apc, 7, 4, testMap), cargo, 7, 3), testMap);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() == apc, "    APC is not where it belongs.");
    testPassed &= validate(testMap.getLocation(7, 3).getResident() == cargo, "    Cargo is not at dropoff location");

    // Try to init a damaged unit inside the transport.
    cargo.alterHP(-5);
    testPassed &= validate( cargo.getHP() == 5, "    Cargo has the wrong amount of HP(" + cargo.getHP() + ")");
    performGameAction(new GameAction.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 7, 4, testMap)), testMap);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() != cargo, "    Cargo is not in the APC.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC has the wrong cargo size (");

    // Calling init on the cargo caused a NPE before, so let's test that case.
    try
    {
      GameEventQueue events = new GameEventQueue();
      apc.initTurn(testMap, events);
      cargo.initTurn(testMap, events);
    }
    catch( NullPointerException npe )
    {
      testPassed = false;
      validate( testPassed, "    NPE encountered during unit init. Details:");
      npe.printStackTrace();
    }

    // Clean up
    testMap.removeUnit(cargo);
    testMap.removeUnit(apc);

    return testPassed;
  }
}
