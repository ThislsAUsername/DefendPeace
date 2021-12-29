package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GamePath;
import Engine.Utils;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitActionLifecycles.ResupplyLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.Environment.Weathers;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class TestVisionMechanics extends TestCase
{
  private static Commander strong = null;
  private static Commander patch = null;
  private static MapMaster testMap = null;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    scn.rules.isFogEnabled = true;
    strong = new Strong(scn.rules);
    patch = new Patch(scn.rules);
    Commander[] cos = { strong, patch };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    testGame = new GameInstance(new GameScenario(), cos, testMap, Weathers.CLEAR, false);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testFogDetection(), "  Fog detection test failed!");
    
    return testPassed;
  }

  private boolean testFogDetection()
  {
    Unit fool = addUnit(testMap, strong, UnitModel.TRANSPORT, 7, 2); fool.initTurn(testMap);
    Unit scout = addUnit(testMap, strong, UnitModel.RECON, 7, 3); scout.initTurn(testMap);
    Unit punch = addUnit(testMap, strong, UnitModel.SIEGE, 4, 5); punch.initTurn(testMap);
    Unit resupplyable = addUnit(testMap, strong, UnitModel.TRANSPORT, 8, 8); resupplyable.initTurn(testMap);
    resupplyable.fuel = 0;
    
    // We need 2 units to observe, one of which should be hidden, the other in cover
    Unit bait = addUnit(testMap, patch, UnitModel.TRANSPORT, 6, 5);
    Unit meaty = addUnit(testMap, patch, UnitModel.ASSAULT, 7, 5);
    meaty.model.hidden = true; // Does anyone else think this is a bad idea? No? Okay, must be fair and balanced.
    
    // It's Strong's turn. Set up his fog goggles.
    strong.initTurn(testMap);
    boolean testPassed = true;

    // Make sure we can't see what we're not supposed to.
    testPassed &= validate(strong.myView.isLocationFogged(6, 5),  "    We can magically see into forests");
    testPassed &= validate(!strong.myView.isLocationFogged(7, 5), "    We can't see roads");
    testPassed &= validate(strong.myView.isLocationEmpty(6, 5),   "    We can magically see units in forests");
    testPassed &= validate(strong.myView.isLocationEmpty(7, 5),   "    We can magically see invisible tanks");
    
    GamePath foolPath = Utils.findShortestPath(fool, 7, 8, strong.myView);
    GameAction resupplyBlind = new ResupplyLifecycle.ResupplyAction(fool, foolPath);
    testPassed &= validate(resupplyBlind.getEvents(testMap).size() == 1, "    Some fool was able to zoom straight through an invisible tank");

    GamePath punchSit = Utils.findShortestPath(punch, punch.x, punch.y, strong.myView);
    GameAction missBait = new BattleLifecycle.BattleAction(strong.myView, punch, punchSit, 6, 5);
    testPassed &= validate(missBait.getEvents(testMap).size() == 0, "    You can shoot things hidden in forests.");
    GameAction missMeat = new BattleLifecycle.BattleAction(strong.myView, punch, punchSit, 7, 5);
    testPassed &= validate(missMeat.getEvents(testMap).size() == 0, "    You can shoot invisible things.");

    // Drive by the two hidden units
    GamePath excursion = new GamePath();
    excursion.addWaypoint(7, 3);
    excursion.addWaypoint(7, 4);
    excursion.addWaypoint(6, 4);
    excursion.addWaypoint(6, 3);
    GameAction driveBy = new WaitLifecycle.WaitAction(scout, excursion);
    testPassed &= validate(driveBy.getEvents(testMap).size() == 1, "    Recons can't move, apparently.");
    performGameAction(driveBy, testGame);

    // Now that we can see everything, try stuff again and expect different results.
    testPassed &= validate(!strong.myView.isLocationFogged(6, 5), "    Doing a drive-by doesn't reveal forests");
    testPassed &= validate(!strong.myView.isLocationFogged(7, 5), "    Driving recons by roads makes them invisible");
    testPassed &= validate(!strong.myView.isLocationEmpty(6, 5),  "    Doing a drive-by doesn't reveal units in forests");
    testPassed &= validate(!strong.myView.isLocationEmpty(7, 5),  "    Doing a drive-by doesn't reveal invisible tanks");
    
    GameAction resupplySighted = new ResupplyLifecycle.ResupplyAction(fool, foolPath); // There's no validation for ResupplyAction's constructor, so we'll get pre-empted but generate properly
    testPassed &= validate(resupplySighted.getEvents(testMap).size() == 1, "    Some fool was able to zoom straight through a visible tank");
    
    GameAction shootBait = new BattleLifecycle.BattleAction(strong.myView, punch, punchSit, 6, 5);
    testPassed &= validate(shootBait.getEvents(testMap).size() == 2, "    You can't shoot things you can see.");
    GameAction shootMeat = new BattleLifecycle.BattleAction(strong.myView, punch, punchSit, 7, 5);
    testPassed &= validate(shootMeat.getEvents(testMap).size() == 2, "    You can't shoot things you can see.");
    
    // Clean up
    testMap.removeUnit(fool);
    testMap.removeUnit(scout);
    testMap.removeUnit(punch);
    testMap.removeUnit(resupplyable);
    
    testMap.removeUnit(bait);
    testMap.removeUnit(meaty);

    return testPassed;
  }
}
