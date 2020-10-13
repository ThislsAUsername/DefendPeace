package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventListener;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitActionLifecycles.DeleteLifecycle;
import Engine.UnitActionLifecycles.LoadLifecycle;
import Engine.UnitActionLifecycles.UnloadLifecycle;
import Engine.UnitActionLifecycles.JoinLifecycle;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class TestTransport extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Patch(scn.rules);
    testCo2 = new Patch(scn.rules);
    Commander[] cos = { testCo1, testCo2 };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    testGame = new GameInstance(testMap);
  }

  private void cleanupTest()
  {
    testCo1 = null;
    testCo2 = null;
    testMap = null;
    testGame = null;
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = validate(testLoadUnloadAPC(), "  Transport test failed.");
    testPassed = validate(testJoinLoadedTransports(), "  Transport Join test failed.");
    testPassed = validate(testLoadedTransportDeath(), "  Transport death test failed.");
    return testPassed;
  }

  /**
   * Basic load/unload APC transport test.
   * @return
   */
  private boolean testLoadUnloadAPC()
  {
    setupTest();

    // Add a couple of units to drive this test.
    Unit cargo = addUnit(testMap, testCo1, UnitModel.TROOP, 4, 1);
    Unit apc = addUnit(testMap, testCo1, UnitModel.TRANSPORT, 4, 2);

    boolean testPassed = true;

    // Try a basic load/move/unload order.
    cargo.initTurn(testMap); // Get him ready.
    testPassed &= validate(Utils.findPossibleDestinations(cargo, testMap, true).contains(new XYCoord(apc.x, apc.y)), "    Cargo can't actually enter transport's square.");
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 4, 2, testMap)), testGame);
    testPassed &= validate(testMap.getLocation(4, 2).getResident() != cargo, "    Cargo is still on the map.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC is not holding a unit.");
    apc.initTurn(testMap); // Get him ready.
    performGameAction(new UnloadLifecycle.UnloadAction(testMap, apc, Utils.findShortestPath(apc, 7, 3, testMap), cargo, 7, 4), testGame);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() == cargo, "    Cargo was not dropped off correctly.");
    testPassed &= validate(apc.heldUnits.isEmpty(), "    APC is not empty when it should be.");

    // Make sure the unit knows it can unload to its own position.
    ArrayList<XYCoord> unloadLocs = Utils.findUnloadLocations( testMap, apc, new XYCoord(7, 4), cargo);
    testPassed &= validate(unloadLocs.contains(new XYCoord(apc.x, apc.y) ), "    APC doesn't know it can unload to its own position.");

    // Make sure we can unload a unit on the apc's current location.
    cargo.initTurn(testMap);
    apc.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 7, 3, testMap)), testGame);
    performGameAction(new UnloadLifecycle.UnloadAction(testMap, apc, Utils.findShortestPath(apc, 7, 4, testMap), cargo, 7, 3), testGame);
    testPassed &= validate(testMap.getLocation(7, 4).getResident() == apc, "    APC is not where it belongs.");
    testPassed &= validate(testMap.getLocation(7, 3).getResident() == cargo, "    Cargo is not at dropoff location");

    // Try to init a damaged unit inside the transport.
    cargo.alterHP(-5);
    testPassed &= validate( cargo.getHP() == 5, "    Cargo has the wrong amount of HP(" + cargo.getHP() + ")");
    cargo.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo, Utils.findShortestPath(cargo, 7, 4, testMap)), testGame);
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

    cleanupTest();

    return testPassed;
  }

  /** Ensure that we get sane results when attempting to join loaded transports. */
  private boolean testJoinLoadedTransports()
  {
    setupTest();

    boolean testPassed = true;

    // Add some units to drive this test.
    Unit lander1 = addUnit(testMap, testCo1, UnitModel.TRANSPORT | UnitModel.SEA, 1, 1);
    Unit lander2 = addUnit(testMap, testCo1, UnitModel.TRANSPORT | UnitModel.SEA, 2, 1);
    Unit cargo1 = addUnit(testMap, testCo1, UnitModel.TROOP, 1, 2);
    Unit cargo2 = addUnit(testMap, testCo1, UnitModel.MECH, 2, 2);

    // Make sure the transports are joinable.
    lander2.damageHP(5);

    // Load up the transports.
    cargo1.initTurn(testMap);
    cargo2.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo1, Utils.findShortestPath(cargo1, 1, 1, testMap)), testGame);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo2, Utils.findShortestPath(cargo2, 2, 1, testMap)), testGame);
    testPassed &= validate(testMap.getLocation(1, 2).getResident() != cargo1, "    Infantry is still on the map.");
    testPassed &= validate(testMap.getLocation(2, 2).getResident() != cargo2, "    Mech is still on the map.");
    testPassed &= validate(lander1.heldUnits.size() == 1, "    Lander1 is not holding a unit.");
    testPassed &= validate(lander2.heldUnits.size() == 1, "    Lander2 is not holding a unit.");
    lander1.initTurn(testMap);
    performGameAction(new JoinLifecycle.JoinAction(testMap, lander1, Utils.findShortestPath(lander1, 2, 1, testMap)), testGame);
    testPassed &= validate(testMap.getLocation(1, 1).getResident() == null, "    Lander1 is still on the map after joining.");
    lander2 = testMap.getLocation(2, 1).getResident(); // We don't care which lander survived the merge, or if a new one was created.
    testPassed &= validate(lander2.model.isAll(UnitModel.SEA | UnitModel.TRANSPORT), "    No lander at join location.");
    testPassed &= validate(lander2.heldUnits.size() == 2, "    Lander2 is holding " + lander2.heldUnits.size() + " units after join instead of 2.");

    // Let's do this again, but try to join a new loaded lander with the previous one. Saddle up.
    lander1 = addUnit(testMap, testCo1, UnitModel.TRANSPORT | UnitModel.SEA, 1, 1);
    cargo1 = addUnit(testMap, testCo1, UnitModel.TROOP, 1, 2);
    cargo1.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo1, Utils.findShortestPath(cargo1, 1, 1, testMap)), testGame);

    // Try to join again. This time it should fail.
    lander1.initTurn(testMap);
    lander2.damageHP(5);
    performGameAction(new JoinLifecycle.JoinAction(testMap, lander1, Utils.findShortestPath(lander1, 2, 1, testMap)), testGame);
    testPassed &= validate(testMap.getLocation(1, 1).getResident() == lander1, "    Lander1 is not on the map after failed join.");
    testPassed &= validate(lander1.heldUnits.size() == 1, "    Lander1 is not holding a unit after failed join.");
    testPassed &= validate(lander2.model.isAll(UnitModel.SEA | UnitModel.TRANSPORT), "    No lander at join location.");
    testPassed &= validate(lander2.heldUnits.size() == 2, "    Lander2 is not holding 2 units after failed join.");

    cleanupTest();

    return testPassed;
  }

  private class DeathCounter extends GameEventListener
  {
    private static final long serialVersionUID = 1L;
    public int count = 0;
    @Override
    public void receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
    {
      count++;
    };
  }

  /** Ensure that transport deaths are handled correctly. */
  private boolean testLoadedTransportDeath()
  {
    setupTest();

    boolean testPassed = true;

    // Add some units to drive this test.
    Unit lander1 = addUnit(testMap, testCo1, UnitModel.TRANSPORT | UnitModel.SEA, 1, 1);
    Unit cargo1 = addUnit(testMap, testCo1, UnitModel.TROOP, 1, 2);
    Unit arty = addUnit(testMap, testCo2, UnitModel.SIEGE | UnitModel.TANK, 1, 3);

    // Make sure the lander is killable.
    lander1.damageHP(8);

    // Load up the transport.
    cargo1.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo1, Utils.findShortestPath(cargo1, 1, 1, testMap)), testGame);
    testPassed &= validate(testMap.getLocation(1, 2).getResident() != cargo1, "    Infantry is still on the map.");
    testPassed &= validate(lander1.heldUnits.size() == 1, "    Lander1 is not holding a unit.");

    // Create a listener to detect if a unit dies.
    DeathCounter dc = new DeathCounter();
    dc.registerForEvents(testGame);

    // Shell the transport.
    arty.initTurn(testMap);
    performGameAction(new BattleLifecycle.BattleAction(testMap, arty, Utils.findShortestPath(arty, 1, 3, testMap), 1, 1), testGame);

    testPassed &= validate(testMap.getLocation(1, 1).getResident() == null, "    Lander1 is still on the map after destruction!");
    testPassed &= validate(dc.count == 2, "    Counted " + dc.count + " unit deaths instead of 2 after loaded transport died!");

    // Do it again, but instead of shooting the transport, just delete it.
    lander1 = addUnit(testMap, testCo1, UnitModel.TRANSPORT | UnitModel.SEA, 1, 1);
    cargo1 = addUnit(testMap, testCo1, UnitModel.TROOP, 1, 2);
    // Load up the transport.
    cargo1.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, cargo1, Utils.findShortestPath(cargo1, 1, 1, testMap)), testGame);
    dc.count = 0; // Reset the counter.
    performGameAction(new DeleteLifecycle.DeleteAction(lander1), testGame);
    testPassed &= validate(testMap.getLocation(1, 1).getResident() == null, "    Lander1 is still on the map after Deletion!");
    testPassed &= validate(dc.count == 2, "    Counted " + dc.count + " unit deaths instead of 2 after deleting loaded transport!");

    // NOTE/TODO: Is ability power awarded for the cargo unit?

    cleanupTest();

    return testPassed;
  }
}
