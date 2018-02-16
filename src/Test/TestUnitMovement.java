package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderPatch;
import CommandingOfficers.CommanderStrong;
import Engine.GameAction;
import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.MapLibrary;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestUnitMovement extends TestCase
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
    // Create a CO and a GameMap.
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testSimpleMovement(), "  Simple movement test failed.");
    testPassed &= validate(testOutOfRangeMovement(), "  Move out of range test failed.");
    testPassed &= validate(testFuelCosts(), "  Fuel cost test failed.");
    return testPassed;
  }

  /** Make an infantry unit, and order him to move. Easy peasy. */
  private boolean testSimpleMovement()
  {
    // Add a Unit and try to move it.
    Unit mover = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 4, 4);
    XYCoord destination = new XYCoord(6, 5);
    GameAction ga = new GameAction.WaitAction(mover, Utils.findShortestPath(mover, destination, testMap));

    performGameAction( ga, testMap );

    // Evaluate the test.    
    boolean testPassed = validate(testMap.getLocation(4, 4).getResident() == null, "    Infantry is still at the start point.");
    testPassed &= validate(testMap.getLocation(destination).getResident() == mover, "    Infantry is not at the destination.");
    testPassed &= validate((destination.xCoord == mover.x) && (destination.yCoord == mover.y),
        "    Infantry doesn't think he's at the destination.");
    testPassed &= validate(96 == mover.fuel, "    Infantry did not lose the proper amount of fuel.");

    // Clean up for the next test.
    testMap.removeUnit(mover);

    return testPassed;
  }

  /** Make an Infantry unit, and tell him to move farther than he can. */
  private boolean testOutOfRangeMovement()
  {
    // Make a unit and add it to the map.
    Unit mover = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 4, 4);

    // Make an action to move the unit 5 spaces away, and execute it.
    GameAction ga = new GameAction.WaitAction(mover, Utils.findShortestPath(mover, 7, 6, testMap));
    performGameAction( ga, testMap );

    // Make sure the action didn't actually execute.
    boolean testPassed = validate(testMap.getLocation(4, 4).getResident() == mover, "    Infantry moved when he shouldn't have.");
    testPassed &= validate(4 == mover.x && 4 == mover.y, "    Infantry thinks he moved when he should not have.");
    testPassed &= validate(testMap.getLocation(7, 6).getResident() == null, "    Target location has a resident when it should not.");
    testPassed &= validate(99 == mover.fuel, "    Infantry lost fuel when attempting an invalid movement that should fail.");

    // Clean up for the next test.
    testMap.removeUnit(mover);

    return testPassed;
  }

  /** Check fuel costs for various paths with various terrain. */
  private boolean testFuelCosts()
  {
    // We don't need any units, since whether fuel drain properly applies to units is handled by the other two tests.
    
    // A 7-space movement across nothing but grass.
    Path grassPath = new Path(1.0);
    grassPath.addWaypoint(3, 7);
    grassPath.addWaypoint(4, 7);
    grassPath.addWaypoint(5, 7);
    grassPath.addWaypoint(6, 7);
    grassPath.addWaypoint(7, 7);
    grassPath.addWaypoint(8, 7);
    grassPath.addWaypoint(9, 7);
    grassPath.addWaypoint(10, 7);

    // A 4-space movement across 1 road, 1 plain, 1 forest, and 1 city
    Path multiPath = new Path(1.0);
    multiPath.addWaypoint(5, 6);
    multiPath.addWaypoint(4, 6);
    multiPath.addWaypoint(3, 6);
    multiPath.addWaypoint(3, 5);
    multiPath.addWaypoint(2, 5);
    
    // Make sure the action didn't actually execute.
    boolean testPassed = validate(grassPath.getFuelCost(testCo1.getUnitModel(UnitEnum.INFANTRY), testMap) == 7, "    Infantry do not charge 1 fuel per space of grass.");
    testPassed &= validate(multiPath.getFuelCost(testCo1.getUnitModel(UnitEnum.INFANTRY), testMap) == 4, "    Infantry movecost is not 1 for road, grass, forest, or city.");
    testPassed &= validate(grassPath.getFuelCost(testCo1.getUnitModel(UnitEnum.B_COPTER), testMap) == 7, "    B Copter does not charge 1 fuel per space of grass.");
    testPassed &= validate(multiPath.getFuelCost(testCo1.getUnitModel(UnitEnum.B_COPTER), testMap) == 4, "    B Copter movecost is not 1 for road, grass, forest, or city.");
    testPassed &= validate(grassPath.getFuelCost(testCo1.getUnitModel(UnitEnum.RECON), testMap) == 14, "    Recon does not charge 2 fuel per space of grass.");
    testPassed &= validate(multiPath.getFuelCost(testCo1.getUnitModel(UnitEnum.RECON), testMap) == 7, "    Recon movecost is wrong for road, grass, forest, or city.");
    testPassed &= validate(grassPath.getFuelCost(testCo1.getUnitModel(UnitEnum.TANK), testMap) == 7, "    Tank does not charge 1 fuel per space of grass.");
    testPassed &= validate(multiPath.getFuelCost(testCo1.getUnitModel(UnitEnum.TANK), testMap) == 5, "    Tank movecost is wrong for road, grass, forest, or city.");
    testPassed &= validate(multiPath.getFuelCost(testCo1.getUnitModel(UnitEnum.CRUISER), testMap) == 396, "    Cruiser movecost is wrong for road, grass, forest, or city.");

    return testPassed;
  }
}
