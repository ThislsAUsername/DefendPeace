package Test;

import AI.Muriel;
import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModelScheme;

public class TestAIMuriel extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static Muriel testAI;
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Strong(scn.rules);
    testCo2 = new Patch(scn.rules);
    testAI = new Muriel(testCo1);
    testCo1.setAIController(testAI);
    Commander[] cos = { testCo1, testCo2 };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    testGame = new GameInstance(testMap);
  }

  private void cleanupTest()
  {
    testCo1 = null;
    testCo2 = null;
    testAI = null;
    testMap = null;
    testGame = null;
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = true;
    testPassed &= validate(testBuildMegatank(), "  Build Megatank test failed.");
    testPassed &= validate(testHuntStall(), "  Hunting stall-test failed.");
    return testPassed;
  }

  /** Confirm that Muriel will build the correct counter, even when there is only one possible counter. */
  private boolean testBuildMegatank()
  {
    setupTest();

    // Add an enemy to counter.
    addUnit(testMap, testCo2, "Megatank", 1, 1);

    // Put Muriel in control
    testCo1.setAIController(testAI);

    // Give Muriel resources.
    testCo1.money = 80000;

    // Ask Muriel what to do.
    testCo1.initTurn(testMap);
    GameAction act = testCo1.getNextAIAction(testMap);
    performGameAction(act, testGame);

    // Muriel should have built a Megatank as the best/only viable unit to counter an enemy Megatank.
    boolean testPassed = validate(testCo1.units.size() == 1, "    Failed to produce a unit!");
    testPassed &= validate(testCo1.units.get(0).model.name.contentEquals("Megatank"), "    Muriel built the wrong thing!");

    // Clean up
    cleanupTest();

    return testPassed;
  }

  /** Confirm that Muriel will build the correct counter, even when there is only one possible counter. */
  private boolean testHuntStall()
  {
    setupTest();

    // Where are things?
    XYCoord tankStart = new XYCoord(2, 1);
    XYCoord facPos = new XYCoord(7, 1);

    // Add some units, and grant us a factory so we can avoid blocking it.
    Unit myTank = addUnit(testMap, testCo1, "Md Tank", tankStart.xCoord, tankStart.yCoord);
    addUnit(testMap, testCo2, "Recon", 10, 1);
    testMap.getLocation(facPos).setOwner(testCo1);

    // Verify that we own the factory in question.
    boolean testPassed = validate(testCo1.ownedProperties.contains(facPos), "    Failed to assign factory.");

    // Ask Muriel what to do.
    testCo1.initTurn(testMap);
    GameAction act = testCo1.getNextAIAction(testMap);
    performGameAction(act, testGame);

    // Muriel should have moved the Md Tank towards the enemy Recon, but not ended top of the factory.
    XYCoord tankEnd = new XYCoord(myTank.x, myTank.y);
    testPassed &= validate(!tankEnd.equals(tankStart), "    Muriel did not move the Md Tank!");
    testPassed &= validate(!tankEnd.equals(facPos), "    Muriel blocked a factory!");

    // Clean up
    cleanupTest();

    return testPassed;
  }
}
