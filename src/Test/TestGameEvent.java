package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.LoadEvent;
import Engine.GameEvents.MoveEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.GameEvents.UnloadEvent;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

public class TestGameEvent extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static MapMaster testMap;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Patch(scn.rules);
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
    boolean testPassed = true;
    testPassed &= validate(testBattleEvent(), "  BattleEvent test failed.");
    testPassed &= validate(testCaptureEvent(), "  CaptureEvent test failed.");
    testPassed &= validate(testCreateUnitEvent(), "  CreateUnitEvent test failed.");
    testPassed &= validate(testLoadUnloadEvent(), "  LoadUnloadEvent test failed.");
    testPassed &= validate(testMoveEvent(), "  MoveEvent test failed.");
    testPassed &= validate(testUnitDieEvent(), "  UnitDieEvent test failed.");
    testPassed &= validate(testResupplyEvent(), "  Resupply test failed.");
    testPassed &= validate(testCommanderDefeatEvent(), "  CommanderDefeatEvent test failed."); // Put this one last because it alters the map.

    return testPassed;
  }

  private boolean testBattleEvent()
  {
    boolean testPassed = true;

    // Add our combatants
    Unit infA = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 1, 1);
    Unit infB = addUnit(testMap, testCo2, UnitEnum.INFANTRY, 1, 2);

    BattleEvent event = new BattleEvent(infA, infB, 2, 2, testMap);
    event.performEvent(testMap);
    testPassed &= validate(infB.getHP() < 10, "    Defender Was not damaged");
    testPassed &= validate(infA.getHP() < 10, "    Defender did not counter-attack");

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);

    return testPassed;
  }

  private boolean testCaptureEvent()
  {
    boolean testPassed = true;

    // We loaded Firing Range, so we expect a city at location (2, 2)
    Terrain.Location city = testMap.getLocation(2, 2);
    testPassed &= validate(city.getEnvironment().terrainType == TerrainType.CITY, "    No city at (2, 2).");
    testPassed &= validate(city.getOwner() == null, "    City should not be owned by any CO yet.");

    // Add a unit
    Unit infA = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 2, 2);
    testPassed &= validate(infA.getCaptureProgress() == 0, "    Infantry capture progress is not 0.");

    // Create a new event, and ensure it does not predict full capture in one turn.
    CaptureEvent captureEvent = new CaptureEvent(infA, city);
    testPassed &= validate(captureEvent.willCapture() == false, "    Event incorrectly predicts capture will succeed.");
    // NOTE: The prediction will be unreliable after performing the event. I'm re-using it here for convenience, but
    //       GameEvents are really designed to be single-use.

    captureEvent.performEvent(testMap);
    testPassed &= validate(infA.getCaptureProgress() == 10, "    Infantry capture progress is not 10.");

    // Hurt the unit so he won't captre as fast.
    infA.damageHP(5.0);
    captureEvent.performEvent(testMap);
    testPassed &= validate(infA.getCaptureProgress() == 15, "    Infantry capture progress is not 15.");

    // Move the unit; he should lose his capture progress.
    infA.initTurn(testMap);
    GameAction moveAction = new GameAction.WaitAction(infA, Utils.findShortestPath(infA, 1, 2, testMap));
    performGameAction(moveAction, testMap);
    infA.initTurn(testMap);
    GameAction moveAction2 = new GameAction.WaitAction(infA, Utils.findShortestPath(infA, 2, 2, testMap));
    performGameAction(moveAction2, testMap);

    // 5, 10, 15
    captureEvent.performEvent(testMap);
    testPassed &= validate(infA.getCaptureProgress() == 5,
        "    Infantry capture progress should be 5, not " + infA.getCaptureProgress() + ".");
    captureEvent.performEvent(testMap);
    testPassed &= validate(infA.getCaptureProgress() == 10,
        "    Infantry capture progress should be 10, not " + infA.getCaptureProgress() + ".");
    captureEvent.performEvent(testMap);
    testPassed &= validate(infA.getCaptureProgress() == 15,
        "    Infantry capture progress should be 15, not " + infA.getCaptureProgress() + ".");

    // Recreate the captureEvent so we can check the prediction again.
    captureEvent = new CaptureEvent(infA, city);
    testPassed &= validate(captureEvent.willCapture() == true, "    Event incorrectly predicts failure to capture.");
    captureEvent.performEvent(testMap);
    testPassed &= validate(infA.getCaptureProgress() == 0, "    Infantry capture progress should be 0 again.");
    testPassed &= validate(city.getOwner() == infA.CO, "    City is not owned by the infantry's CO, but should be.");

    // Clean up
    testMap.removeUnit(infA);

    return testPassed;
  }

  private boolean testCreateUnitEvent()
  {
    boolean testPassed = true;

    XYCoord coords = new XYCoord(13, 8);
    int startFunds = testCo1.money = 9001;
    CreateUnitEvent event = new CreateUnitEvent(testCo1, testCo1.getUnitModel(UnitEnum.INFANTRY), coords);

    testPassed &= validate(testMap.getLocation(coords).getResident() == null, "    Location is already occupied.");

    event.performEvent(testMap);

    Unit resident = testMap.getLocation(coords).getResident();
    testPassed &= validate(resident != null, "    Failed to create a unit.");
    testPassed &= validate(resident.model.type == UnitEnum.INFANTRY, "    Unit created with wrong type.");
    testPassed &= validate(resident.CO == testCo1, "    Unit created with wrong type.");
    // TODO: Consider moving cost into a new TransferFundsEvent.
    testPassed &= validate(testCo1.money == (startFunds - resident.model.getCost()), "    Unit cost not accounted correctly.");

    // Clean up.
    testMap.removeUnit(resident);

    return testPassed;
  }

  private boolean testLoadUnloadEvent()
  {
    boolean testPassed = true;

    // Add some units.
    Unit inf = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 2, 2);
    Unit mech = addUnit(testMap, testCo1, UnitEnum.MECH, 2, 3);
    Unit apc = addUnit(testMap, testCo1, UnitEnum.APC, 3, 2);

    // Try to load the infantry onto the mech unit, and ensure it fails.
    new LoadEvent(inf, mech).performEvent(testMap);
    testPassed &= validate(testMap.getLocation(2, 2).getResident() == inf, "    Infantry should still be at (2, 2).");
    testPassed &= validate(2 == inf.x && 2 == inf.y, "    Infantry should still think he is at (2, 2).");
    testPassed &= validate(mech.heldUnits == null, "    Mech should not have holding capacity.");

    // Try to load the infantry into the APC, and make sure it works.
    new LoadEvent(inf, apc).performEvent(testMap);
    testPassed &= validate(testMap.getLocation(2, 2).getResident() == null, "   Infantry is still at his old map location.");
    testPassed &= validate(-1 == inf.x && -1 == inf.y, "    Infantry does not think he is in the transport.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC is not holding 1 unit, but should be holding Infantry.");
    testPassed &= validate(apc.heldUnits.get(0).model.type == UnitModel.UnitEnum.INFANTRY,
        "    Held unit is not type INFANTRY, but should be.");

    // Now see if we can also load the mech into the APC; verify this fails.
    new LoadEvent(mech, apc).performEvent(testMap);
    testPassed &= validate(testMap.getLocation(2, 3).getResident() == mech, "    Mech should still be at (2, 3).");
    testPassed &= validate(2 == mech.x && 3 == mech.y, "    Mech does not think he is at (2, 3), but he should.");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC should still only be holding 1 unit.");

    // Unload the mech; this should fail, since he is not on the transport.
    new UnloadEvent(apc, mech, 3, 3).performEvent(testMap);
    testPassed &= validate(testMap.getLocation(3, 3).getResident() == null, "    Location (3, 3) should have no residents.");
    testPassed &= validate(2 == mech.x && 3 == mech.y, "    Mech thinks he has moved, but should still be at (2, 3).");
    testPassed &= validate(apc.heldUnits.size() == 1, "    APC should still have one passenger.");

    // Unload the infantry; this should succeed.
    new UnloadEvent(apc, inf, 3, 3).performEvent(testMap);
    testPassed &= validate(testMap.getLocation(3, 3).getResident() == inf, "    Infantry is not at the dropoff point.");
    testPassed &= validate(apc.heldUnits.size() == 0,
        "    APC should have zero cargo, but heldUnits size is " + apc.heldUnits.size());
    testPassed &= validate(3 == inf.x && 3 == inf.y, "    Infantry does not think he is at dropoff point.");

    // Clean up
    testMap.removeUnit(inf);
    testMap.removeUnit(mech);
    testMap.removeUnit(apc);

    return testPassed;
  }

  boolean testMoveEvent()
  {
    boolean testPassed = true;

    // Add some units.
    Unit inf = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 2, 2);
    Unit mech = addUnit(testMap, testCo1, UnitEnum.MECH, 2, 3);
    Unit apc = addUnit(testMap, testCo1, UnitEnum.APC, 3, 2);

    Path path = new Path(1.0); // TODO: Why do we have to provide a speed here?
    path.addWaypoint(3, 3); // we need two waypoints to not break compatibility with MoveEvent, since it assumes the first waypoint isn't used.
    path.addWaypoint(7, 5); // A suitable place to move (should be the middle of the road in Firing Range).

    // Move the infantry - Note that MoveEvent does not verify that this is a valid move for the unit. This is
    // expected to happen in GameAction, which is typically responsible for creating MoveEvents.
    new MoveEvent(inf, path).performEvent(testMap); // Move the infantry 8 spaces, woo!
    testPassed &= validate(7 == inf.x && 5 == inf.y, "    Infantry should think he is at (7, 5) after moving.");
    testPassed &= validate(testMap.getLocation(7, 5).getResident() == inf, "    Infantry is not at (7, 5) after moving.");

    path.addWaypoint(7, 6); // New endpoint.
    new MoveEvent(mech, path).performEvent(testMap);
    testPassed &= validate(7 == mech.x && 6 == mech.y, "    Mech should think he is at (7, 6) after moving.");
    testPassed &= validate(testMap.getLocation(7, 6).getResident() == mech, "    Mech is not at (7, 6) after moving.");

    path.addWaypoint(7, 0); // New endpoint over water.
    new MoveEvent(mech, path).performEvent(testMap); // This should not execute. Water is bad for grunts.
    testPassed &= validate(7 == mech.x && 6 == mech.y, "    Mech does not think he is at (7, 6), but should.");
    testPassed &= validate(testMap.getLocation(7, 6).getResident() == mech, "    Mech is not still at (7, 6), but should be.");
    testPassed &= validate(testMap.getLocation(7, 0).getResident() == null, "    Location (7, 0) should still be empty.");

    path.addWaypoint(7, 5); // New endpoint to move apc over infantry.
    new MoveEvent(apc, path).performEvent(testMap); // This should not execute. Treads are bad for grunts.
    testPassed &= validate(7 == inf.x && 5 == inf.y, "    Infantry should still think he is at (7, 5).");
    testPassed &= validate(testMap.getLocation(7, 5).getResident() == inf, "    Infantry should still be at (7, 5).");
    testPassed &= validate(3 == apc.x && 2 == apc.y, "    APC should still think it is at (3, 2).");
    testPassed &= validate(testMap.getLocation(3, 2).getResident() == apc, "    APC should still be at (3, 2)");

    // Clean up.
    testMap.removeUnit(inf);
    testMap.removeUnit(mech);
    testMap.removeUnit(apc);

    return testPassed;
  }

  private boolean testUnitDieEvent()
  {
    boolean testPassed = true;

    // Add some units.
    Unit inf = addUnit(testMap, testCo1, UnitEnum.INFANTRY, 2, 2);
    Unit mech = addUnit(testMap, testCo1, UnitEnum.MECH, 2, 3);
    mech.damageHP(5); // Just for some variation.

    // Knock 'em dead.
    new UnitDieEvent(inf).performEvent(testMap);
    new UnitDieEvent(mech).performEvent(testMap);

    // Make sure the pins are down.
    testPassed &= validate(inf.getPreciseHP() == 0, "    Infantry still has health after dying.");
    testPassed &= validate(inf.x == -1 && inf.y == -1, "    Infantry still thinks he is on the map after death.");
    testPassed &= validate(testMap.getLocation(2, 2).getResident() == null, "    Infantry did not vacate his space after death.");
    testPassed &= validate(mech.getPreciseHP() == 0, "    Mech still has health after dying.");
    testPassed &= validate(mech.x == -1 && mech.y == -1, "    Mech still thinks he is on the map after death.");
    testPassed &= validate(testMap.getLocation(2, 3).getResident() == null, "    Mech did not vacate his space after death.");

    // No cleanup required.

    return testPassed;
  }

  boolean testResupplyEvent()
  {
    boolean testPassed = true;

    // Add some units.
    Unit apc = addUnit(testMap, testCo1, UnitEnum.APC, 1, 3);
    Unit mech = addUnit(testMap, testCo1, UnitEnum.MECH, 1, 4);
    Unit mech2 = addUnit(testMap, testCo1, UnitEnum.MECH, 3, 3);
    Unit recon = addUnit(testMap, testCo1, UnitEnum.RECON, 1, 8); // On the HQ

    // Take away ammo/fuel.
    int numWeapons = mech.weapons.size();
    for( int i = 0; i < numWeapons; ++i )
    {
      mech.weapons.get(i).ammo = 0;
      mech2.weapons.get(i).ammo = 0;
    }
    apc.fuel = apc.model.maxFuel / 2;
    mech.fuel = 0;
    mech2.fuel = 0;
    recon.fuel = 0;

    // Double-check the units are out of bullets/gas.
    testPassed &= validate(mech.fuel == 0, "    Mech still has fuel, but shouldn't.");
    testPassed &= validate(mech2.fuel == 0, "    Mech2 still has fuel, but shouldn't.");
    testPassed &= validate(recon.fuel == 0, "    Recon still has fuel, but shouldn't.");
    for( int i = 0; i < numWeapons; ++i )
    {
      Weapon wpn = mech.weapons.get(i);
      testPassed &= validate((wpn.ammo == 0),
          "    Mech weapon " + wpn.model.toString() + "  still has " + wpn.ammo + " ammo, but should be empty.");
      Weapon wpn2 = mech2.weapons.get(i);
      testPassed &= validate((wpn2.ammo == 0),
          "    Mech2 weapon " + wpn2.model.toString() + "  still has " + wpn2.ammo + " ammo, but should be empty.");
    }

    // Simulate a new turn for the APC/Recon; the apc should re-supply the mech, and the recon should re-supply from the  HQ.
    GameEventQueue events = new GameEventQueue();
    events.addAll(apc.initTurn(testMap));
    events.addAll(recon.initTurn(testMap));
    for( GameEvent event : events )
    {
      event.performEvent(testMap);
    }

    // Give the APC a new GameAction to go resupply mech2.
    GameAction resupplyAction = new GameAction.ResupplyAction(apc, Utils.findShortestPath(apc, 2, 3, testMap));
    performGameAction(resupplyAction, testMap);

    // Make sure the mechs got their mojo back.
    testPassed &= validate(apc.fuel != apc.model.maxFuel, "    APC resupplied itself. Life doesn't work that way.");
    testPassed &= validate(mech.fuel == mech.model.maxFuel, "    Mech should have max fuel after turn init, but doesn't.");
    testPassed &= validate(mech2.fuel == mech2.model.maxFuel, "    Mech2 should have max fuel after resupply, but doesn't.");
    testPassed &= validate(recon.fuel == recon.model.maxFuel, "    Recon should have max fuel after new turn, but doesn't.");
    for( int i = 0; i < numWeapons; ++i )
    {
      Weapon wpn = mech.weapons.get(i);
      testPassed &= validate((wpn.ammo == wpn.model.maxAmmo), "    Mech weapon should have max ammo after resupply.");
      Weapon wpn2 = mech2.weapons.get(i);
      testPassed &= validate((wpn2.ammo == wpn2.model.maxAmmo), "    Mech2 weapon should have max ammo after resupply.");
    }

    // Clean up.
    testMap.removeUnit(apc);
    testMap.removeUnit(mech);

    return testPassed;
  }

  private boolean testCommanderDefeatEvent()
  {
    boolean testPassed = true;

    // Make sure our target CO is not defeated already.
    testPassed &= validate(testCo2.isDefeated == false, "    testCo2 started out in defeat, but he should not be.");

    // We loaded Firing Range, so we expect a city at location (2, 2)
    Terrain.Location city = testMap.getLocation(10, 1);
    // ... an HQ at location (13, 1)
    Terrain.Location hq = testMap.getLocation(13, 1);
    // ... and two factories at (13, 2) and (12, 2).
    Terrain.Location fac1 = testMap.getLocation(13, 2);
    Terrain.Location fac2 = testMap.getLocation(12, 2);

    // Verify the map looks as we expect.
    testPassed &= validate(city.getEnvironment().terrainType == TerrainType.CITY, "    No city at (10, 1).");
    city.setOwner(testCo2);
    testPassed &= validate(city.getOwner() == testCo2, "    City should belong to CO 2.");
    testPassed &= validate(hq.getEnvironment().terrainType == TerrainType.HEADQUARTERS, "    No HQ where expected.");
    testPassed &= validate(hq.getOwner() == testCo2, "    HQ should belong to CO 2.");
    testPassed &= validate(fac1.getEnvironment().terrainType == TerrainType.FACTORY, "    Fac 1 is not where expected.");
    testPassed &= validate(fac1.getOwner() == testCo2, "    Fac 1 should belong to CO 2.");
    testPassed &= validate(fac2.getEnvironment().terrainType == TerrainType.FACTORY, "    Fac 2 is not where expected.");
    testPassed &= validate(fac2.getOwner() == testCo2, "    Fac 2 should belong to CO 2.");

    // Grant some units to testCo2
    Unit baddie1 = addUnit(testMap, testCo2, UnitEnum.INFANTRY, 13, 1);
    Unit baddie2 = addUnit(testMap, testCo2, UnitEnum.MECH, 12, 1);
    Unit baddie3 = addUnit(testMap, testCo2, UnitEnum.APC, 13, 2);

    // Verify the units were added correctly.
    testPassed &= validate(testMap.getLocation(13, 1).getResident() == baddie1, "    Unit baddie1 is not where he belongs.");
    testPassed &= validate(baddie1.x == 13 && baddie1.y == 1, "    Unit baddie1 doesn't know where he is.");
    testPassed &= validate(testMap.getLocation(12, 1).getResident() == baddie2, "    Unit baddie2 is not where he belongs.");
    testPassed &= validate(baddie2.x == 12 && baddie2.y == 1, "    Unit baddie2 doesn't know where he is.");
    testPassed &= validate(testMap.getLocation(13, 2).getResident() == baddie3, "    Unit baddie3 is not where he belongs.");
    testPassed &= validate(baddie3.x == 13 && baddie3.y == 2, "    Unit baddie3 doesn't know where he is.");

    // Bring to pass this poor commander's defeat.
    CommanderDefeatEvent event = new CommanderDefeatEvent(testCo2);
    event.performEvent(testMap);

    //================================ Validate post-conditions.

    // All of testCo2's Units should be removed from the map.
    testPassed &= validate(testMap.getLocation(13, 1).getResident() == null,
        "    Unit baddie1 was not removed from the map after defeat.");
    testPassed &= validate(baddie1.x == -1 && baddie1.y == -1, "    Unit baddie1 still thinks he's on the map after defeat.");
    testPassed &= validate(testMap.getLocation(12, 1).getResident() == null,
        "    Unit baddie2 was not removed from the map after defeat.");
    testPassed &= validate(baddie2.x == -1 && baddie2.y == -1, "    Unit baddie2 still thinks he's on the map after defeat.");
    testPassed &= validate(testMap.getLocation(13, 2).getResident() == null,
        "    Unit baddie3 was not removed from the map after defeat.");
    testPassed &= validate(baddie3.x == -1 && baddie3.y == -1, "    Unit baddie3 still thinks he's on the map after defeat.");

    // Ensure testCo2 no longer owns properties.
    testPassed &= validate(city.getOwner() == null, "    City should no longer have an owner.");
    testPassed &= validate(hq.getOwner() == null, "    HQ should no longer have an owner.");
    testPassed &= validate(fac1.getOwner() == null, "    Fac 1 should no longer have an owner.");
    testPassed &= validate(fac2.getOwner() == null, "    Fac 2 should no longer have an owner.");

    // His former HQ should now be a mere city.
    testPassed &= validate(hq.getEnvironment().terrainType == TerrainType.CITY,
        "    HQ was not downgraded to city after defeat.");

    // Confirm that yea, verily, testCo2 is truly defeated.
    testPassed &= validate(true == testCo2.isDefeated, "    testCo2 does not think he is defeated (but he so is).");

    // No cleanup should be required.
    return testPassed;
  }
}
