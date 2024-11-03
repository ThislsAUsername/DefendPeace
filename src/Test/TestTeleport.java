package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.DefendPeace.CyanOcean.Patch;
import Engine.Army;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.TeleportEvent;
import Engine.XYCoord;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class TestTeleport extends TestCase
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
    Army[] cos = { new Army(scn, testCo1), new Army(scn, testCo2) };

    testMap = new MapMaster(cos, Terrain.Maps.FiringRange.getMapInfo());
    testGame = new GameInstance(cos, testMap);
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
    TestListener listener = new TestListener(testGame);

    // Add a unit.
    Unit friend = addUnit(testMap, testCo1, UnitModel.MECH, start.x, start.y);
    friend.isTurnOver = false;

    // Teleport him.
    GameAction tp = new GameAction.TeleportAction(friend, end);
    for( GameEvent event : tp.getEvents(testMap) )
      event.performEvent(testMap);
    testPassed &= validate((friend.x == end.x && friend.y== end.y), "    Friend doesn't think teleport moved him!");
    testPassed &= validate(friend == testMap.getLocation(end).getResident(), "    Friend didn't move on the map!");

    // Dunk him.
    friend.isTurnOver = false;
    tp = new GameAction.TeleportAction(friend, water);
    for( GameEvent event : tp.getEvents(testMap) )
    {
      event.performEvent(testMap);
      GameEventListener.publishEvent(event, testGame); // Publish so we can check results.
    }
    testPassed &= validate(listener.death, "    UnitDieEvent was not published for unit in water!");
    testPassed &= validate(listener.defeat, "    Listener received no defeat event for last unit drowning!");
    listener.reset();

    // Make a new friend, and an enemy.
    friend = addUnit(testMap, testCo1, UnitModel.MECH, start.x, start.y);
    Unit enemy = addUnit(testMap, testCo2, UnitModel.MECH, end.x, end.y);

    // Swap them
    friend.isTurnOver = false;
    tp = new GameAction.TeleportAction(friend, end, TeleportEvent.CollisionOutcome.SWAP);
    for( GameEvent event : tp.getEvents(testMap) )
    {
      event.performEvent(testMap);
      GameEventListener.publishEvent(event, testGame); // Publish so we can check results.
    }
    testPassed &= validate((friend.x == end.x && friend.y== end.y), "    Friend doesn't think he swapped!");
    testPassed &= validate(friend == testMap.getLocation(end).getResident(), "    Friend didn't move!");
    testPassed &= validate((enemy.x == start.x && enemy.y== start.y), "    Enemy doesn't think he swapped!");
    testPassed &= validate(enemy == testMap.getLocation(start).getResident(), "    Enemy didn't move!");
    testPassed &= validate(!listener.death, "    UnitDieEvent was published for unit swap!");
    testPassed &= validate(!listener.defeat, "    Listener received defeat event incorrectly!");
    listener.reset();

    // Stomp enemy with friend.
    friend.isTurnOver = false;
    tp = new GameAction.TeleportAction(friend, start, TeleportEvent.CollisionOutcome.KILL);
    for( GameEvent event : tp.getEvents(testMap) )
    {
      event.performEvent(testMap);
      GameEventListener.publishEvent(event, testGame); // Publish so we can check results.
    }
    testPassed &= validate((friend.x == start.x && friend.y== start.y), "    Friend doesn't think he swapped!");
    testPassed &= validate(friend == testMap.getLocation(start).getResident(), "    Friend didn't move!");
    testPassed &= validate(listener.death, "    UnitDieEvent was not published for stomped unit!");
    testPassed &= validate(listener.defeat, "    Listener received no defeat event!");
    listener.reset();

    // Clean up.
    testMap.removeUnit(friend);
    listener.unregister(testGame);

    return testPassed;
  }

  private static class TestListener implements GameEventListener
  {
    private static final long serialVersionUID = 1L;
    public boolean death;
    public boolean defeat;

    public TestListener(GameInstance gi)
    {
      GameEventListener.registerEventListener(this, gi);
    }

    @Override
    public GameEventQueue receiveUnitDieEvent(Unit unit, XYCoord grave, Integer healthBeforeDeath)
    {
      death = true;
      return null;
    }
    
    @Override
    public GameEventQueue receiveCommanderDefeatEvent(ArmyDefeatEvent event)
    {
      defeat = true;
      return null;
    }

    public void reset() {death=false; defeat=false;}
  }
}
