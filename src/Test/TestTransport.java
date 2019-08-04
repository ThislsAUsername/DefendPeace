package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestTransport extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static MapMaster testMap;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Strong(scn.rules);
    testCo2 = new Patch(scn.rules);
    Commander[] cos = { testCo1, testCo2 };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    for( Commander co : cos )
    {
      co.myView = new MapWindow(testMap, co);
    }
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
    cargo.initTurn(testMap); // Get him ready.
    performGameAction(new GameAction.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 4, 2, testMap)), testMap);
    testPassed &= validate(testMap.getLocation(4, 2).getResident() != cargo, "    Cargo is still on the map.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC is not holding a unit.");
    apc.initTurn(testMap); // Get him ready.
    performGameAction(new GameAction.UnloadAction(testMap, apc, Utils.findShortestPath(apc, 7, 3, testMap), cargo, 7, 4), testMap);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() == cargo, "    Cargo was not dropped off correctly.");
    testPassed &= validate(apc.heldUnits.isEmpty(), "    APC is not empty when it should be.");

    // Make sure the unit knows it can unload to its own position.
    ArrayList<XYCoord> unloadLocs = Utils.findUnloadLocations( testMap, apc, new XYCoord(7, 4), cargo);
    testPassed &= validate(unloadLocs.contains(new XYCoord(apc.x, apc.y) ), "    APC doesn't know it can unload to its own position.");

    // Make sure we can unload a unit on the apc's current location.
    cargo.initTurn(testMap);
    apc.initTurn(testMap);
    performGameAction(new GameAction.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 7, 3, testMap)), testMap);
    performGameAction(new GameAction.UnloadAction(testMap, apc, Utils.findShortestPath(apc, 7, 4, testMap), cargo, 7, 3), testMap);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() == apc, "    APC is not where it belongs.");
    testPassed &= validate(testMap.getLocation(7, 3).getResident() == cargo, "    Cargo is not at dropoff location");

    // Try to init a damaged unit inside the transport.
    cargo.alterHP(-5);
    testPassed &= validate( cargo.getHP() == 5, "    Cargo has the wrong amount of HP(" + cargo.getHP() + ")");
    cargo.initTurn(testMap);
    performGameAction(new GameAction.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 7, 4, testMap)), testMap);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() != cargo, "    Cargo is not in the APC.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC has the wrong cargo size (");

    // Calling init on the cargo caused a NPE before, so let's test that case.
    try
    {
      apc.initTurn(testMap);
      cargo.initTurn(testMap);
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
