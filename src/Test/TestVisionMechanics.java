package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.Path;
import Engine.Utils;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestVisionMechanics extends TestCase
{
  private static Commander strong = null;
  private static Commander patch = null;
  private static MapMaster testMap = null;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    strong = new Strong(scn.rules);
    patch = new Patch(scn.rules);
    Commander[] cos = { strong, patch };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    
    // Set up fog vision semantics
    for( Commander co : cos )
    {
      co.myView = new MapWindow(testMap, co, true);
    }
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
    Unit fool = addUnit(testMap, strong, UnitEnum.APC, 7, 2); fool.initTurn(testMap);
    Unit scout = addUnit(testMap, strong, UnitEnum.RECON, 7, 3); scout.initTurn(testMap);
    Unit punch = addUnit(testMap, strong, UnitEnum.ROCKETS, 8, 3); punch.initTurn(testMap);
    Unit resupplyable = addUnit(testMap, strong, UnitEnum.APC, 8, 8); resupplyable.initTurn(testMap);
    resupplyable.fuel = 0;
    
    // We need 2 units to observe, one of which should be hidden, the other in cover
    Unit bait = addUnit(testMap, patch, UnitEnum.APC, 6, 5);
    Unit meaty = addUnit(testMap, patch, UnitEnum.MD_TANK, 7, 5);
    patch.unitModels.get(UnitEnum.MD_TANK).hidden = true; // Does anyone else think this is a bad idea? No? Okay, must be fair and balanced.
    
    // It's Strong's turn. Set up his fog goggles.
    strong.initTurn(testMap);
    boolean testPassed = true;

    // Make sure we can't see what we're not supposed to.
    testPassed &= validate(strong.myView.isLocationFogged(6, 5),  "    We can magically see into forests");
    testPassed &= validate(!strong.myView.isLocationFogged(7, 5), "    We can't see roads");
    testPassed &= validate(strong.myView.isLocationEmpty(6, 5),   "    We can magically see units in forests");
    testPassed &= validate(strong.myView.isLocationEmpty(7, 5),   "    We can magically see invisible tanks");
    
    Path foolPath = Utils.findShortestPath(fool, 7, 8, strong.myView);
    GameAction resupplyBlind = new GameAction.ResupplyAction(fool, foolPath);
    testPassed &= validate(resupplyBlind.getEvents(testMap).size() == 1, "    Some fool was able to zoom straight through an invisible tank");

    Path punchSit = Utils.findShortestPath(punch, punch.x, punch.y, strong.myView);
    GameAction missBait = new GameAction.AttackAction(strong.myView, punch, punchSit, 6, 5);
    testPassed &= validate(missBait.getEvents(testMap).size() == 0, "    You can shoot things hidden in forests.");
    GameAction missMeat = new GameAction.AttackAction(strong.myView, punch, punchSit, 7, 5);
    testPassed &= validate(missMeat.getEvents(testMap).size() == 0, "    You can shoot invisible things.");

    // Drive by the two hidden units
    Path excursion = new Path(42);
    excursion.addWaypoint(7, 3);
    excursion.addWaypoint(7, 4);
    excursion.addWaypoint(6, 4);
    excursion.addWaypoint(6, 3);
    GameAction driveBy = new GameAction.WaitAction(scout, excursion);
    testPassed &= validate(driveBy.getEvents(testMap).size() == 1, "    Recons can't move, apparently.");
    performGameAction(driveBy, testMap);

    // Now that we can see everything, try stuff again and expect different results.
    testPassed &= validate(!strong.myView.isLocationFogged(6, 5), "    Doing a drive-by doesn't reveal forests");
    testPassed &= validate(!strong.myView.isLocationFogged(7, 5), "    Driving recons by roads makes them invisible");
    testPassed &= validate(!strong.myView.isLocationEmpty(6, 5),  "    Doing a drive-by doesn't reveal units in forests");
    testPassed &= validate(!strong.myView.isLocationEmpty(7, 5),  "    Doing a drive-by doesn't reveal invisible tanks");
    
    GameAction resupplySighted = new GameAction.ResupplyAction(fool, foolPath); // There's no validation for ResupplyAction's constructor, so we'll get pre-empted but generate properly
    testPassed &= validate(resupplySighted.getEvents(testMap).size() == 1, "    Some fool was able to zoom straight through a visible tank");
    
    GameAction shootBait = new GameAction.AttackAction(strong.myView, punch, punchSit, 6, 5);
    testPassed &= validate(shootBait.getEvents(testMap).size() == 2, "    You can't shoot things you can see.");
    GameAction shootMeat = new GameAction.AttackAction(strong.myView, punch, punchSit, 7, 5);
    testPassed &= validate(shootMeat.getEvents(testMap).size() == 2, "    You can't shoot things you can see.");
    
    // Clean up
    testMap.removeUnit(fool);
    testMap.removeUnit(scout);
    testMap.removeUnit(punch);
    
    testMap.removeUnit(bait);
    testMap.removeUnit(meaty);

    return testPassed;
  }
}
