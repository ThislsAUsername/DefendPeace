package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.TeleportEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.Utils;
import Engine.XYCoord;
import Engine.UnitActionLifecycles.JoinLifecycle;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Units.Unit;
import Units.UnitModel;

public class TestTeleport extends TestCase
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
    boolean testPassed = true;

    setupTest();

    // Useful coordinates.
    XYCoord start = new XYCoord(1, 4);
    XYCoord end = new XYCoord(1, 5);
    XYCoord water = new XYCoord(0, 0);

    // Event listener
    TestListener listener = new TestListener();

    // Add a unit.
    Unit friend = addUnit(testMap, testCo1, UnitModel.MECH, start.xCoord, start.yCoord);

    // Teleport him.
    GameEvent tp = new TeleportEvent(testMap, friend, end);
    tp.performEvent(testMap);
    testPassed &= validate((friend.x == end.xCoord && friend.y== end.yCoord), "    Friend doesn't think teleport moved him!");
    testPassed &= validate(friend == testMap.getLocation(end).getResident(), "    Friend didn't move on the map!");

    // Dunk him.
    tp = new TeleportEvent(testMap, friend, water);
    tp.performEvent(testMap);
    GameEventListener.publishEvent(tp); // Publish so we can check results.
    testPassed &= validate(listener.death, "    UnitDieEvent was not published for unit in water!");
    testPassed &= validate(listener.defeat, "    Listener received no defeat event for last unit drowning!");
    listener.reset();

    // Make a new friend, and an enemy.
    friend = addUnit(testMap, testCo1, UnitModel.MECH, start.xCoord, start.yCoord);
    Unit enemy = addUnit(testMap, testCo2, UnitModel.MECH, end.xCoord, end.yCoord);

    // Swap them
    tp = new TeleportEvent(testMap, friend, end, TeleportEvent.CollisionOutcome.SWAP);
    tp.performEvent(testMap);
    GameEventListener.publishEvent(tp);
    testPassed &= validate((friend.x == end.xCoord && friend.y== end.yCoord), "    Friend doesn't think he swapped!");
    testPassed &= validate(friend == testMap.getLocation(end).getResident(), "    Friend didn't move!");
    testPassed &= validate((enemy.x == start.xCoord && enemy.y== start.yCoord), "    Enemy doesn't think he swapped!");
    testPassed &= validate(enemy == testMap.getLocation(start).getResident(), "    Enemy didn't move!");
    testPassed &= validate(!listener.death, "    UnitDieEvent was published for unit swap!");
    testPassed &= validate(!listener.defeat, "    Listener received defeat event incorrectly!");
    listener.reset();

    // Stomp enemy with friend.
    tp = new TeleportEvent(testMap, friend, start, TeleportEvent.CollisionOutcome.KILL);
    tp.performEvent(testMap);
    GameEventListener.publishEvent(tp);
    testPassed &= validate((friend.x == start.xCoord && friend.y== start.yCoord), "    Friend doesn't think he swapped!");
    testPassed &= validate(friend == testMap.getLocation(start).getResident(), "    Friend didn't move!");
    testPassed &= validate(listener.death, "    UnitDieEvent was not published for stomped unit!");
    testPassed &= validate(listener.defeat, "    Listener received no defeat event!");
    listener.reset();

    // Clean up.
    testMap.removeUnit(friend);
    listener.unregister();

    return testPassed;
  }

  private static class TestListener extends GameEventListener
  {
    private static final long serialVersionUID = 1L;
    public boolean death;
    public boolean defeat;

    public TestListener()
    {
      GameEventListener.registerEventListener(this);
    }

    @Override
    public void receiveUnitDieEvent(UnitDieEvent event)
    {
      death = true;
    }
    
    @Override
    public void receiveCommanderDefeatEvent(CommanderDefeatEvent event)
    {
      defeat = true;
    }

    public void reset() {death=false; defeat=false;}
  }
}
