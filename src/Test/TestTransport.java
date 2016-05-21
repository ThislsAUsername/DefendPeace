package Test;

import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Engine.GameAction;
import Terrain.GameMap;
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
	    testCo1 = new CmdrStrong();
	    testCo2 = new Commander();
	    Commander[] cos = {testCo1, testCo2};
	    
	    // TODO: This will have to change once GameMap doesn't build a default map.
	    testMap = new GameMap(cos);

	    // Remove the default units. TODO: Remove this once there isn't a default map.
	    testMap.getLocation(6, 5).setResident(null);
	    testMap.getLocation(8, 4).setResident(null);
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
		new GameAction(cargo, 4, 2, GameAction.ActionType.LOAD).execute(testMap);
		testPassed &= validate(testMap.getLocation(4, 2).getResident() != cargo, "    Loaded Unit is still on the map.");
		testPassed &= validate(apc.heldUnits.size() == 1, "    APC is not holding a unit.");
		new GameAction(apc, 7, 3, GameAction.ActionType.UNLOAD, 7, 4).execute(testMap);
		testPassed &= validate(testMap.getLocation(7,4).getResident() == cargo, "    Cargo was not dropped off correctly.");

		// Make sure we can unload a unit on the apc's current location.
		new GameAction(cargo, 7, 3, GameAction.ActionType.LOAD).execute(testMap);
		new GameAction(apc, 7, 4, GameAction.ActionType.UNLOAD, 7, 3).execute(testMap);
		testPassed = validate(testMap.getLocation(7, 4).getResident() == apc, "    APC is not where it belongs.");
		testPassed &= validate(testMap.getLocation(7, 3).getResident() == cargo, "    Cargo is not at dropoff location");

		// Clean up
		testMap.removeUnit(cargo);
		testMap.removeUnit(apc);

		return testPassed;
	}
}
