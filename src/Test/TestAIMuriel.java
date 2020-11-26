package Test;

import AI.Muriel;
import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
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
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Strong(scn.rules);
    testCo2 = new Patch(scn.rules);
    Commander[] cos = { testCo1, testCo2 };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    testGame = new GameInstance(testMap);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testBuildMegatank(), "  Build Megatank test failed.");
    return testPassed;
  }

  /** Confirm that Muriel will build the correct counter, even when there is only one possible counter. */
  private boolean testBuildMegatank()
  {
    // Add an enemy to counter.
    Unit nme = addUnit(testMap, testCo1, "Megatank", 1, 1);

    // Put Muriel in control
    Muriel mrl = new Muriel(testCo2);
    testCo2.setAIController(mrl);

    // Give Muriel resources.
    testCo2.money = 80000;

    // Ask Muriel what to do.
    testCo2.initTurn(testMap);
    GameAction act = testCo2.getNextAIAction(testMap);
    performGameAction(act, testGame);

    // Muriel should have built a Megatank as the best/only viable unit to counter an enemy Megatank.
    boolean testPassed = validate(testCo2.units.size() == 1, "    Failed to produce a unit!");
    testPassed = validate(testCo2.units.get(0).model.name.contentEquals("Megatank"), "    Muriel built the wrong thing!");

    // Clean up
    testMap.removeUnit(nme);

    return testPassed;
  }
}
