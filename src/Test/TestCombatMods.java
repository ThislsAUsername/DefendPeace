package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderCinder;
import CommandingOfficers.CommanderVenge;
import Engine.GameAction;
import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestCombatMods extends TestCase
{
  private static Commander cinder;
  private static Commander venge;
  private static MapMaster testMap;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    cinder = new CommanderCinder();
    venge = new CommanderVenge();
    Commander[] cos = { cinder, venge };

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
    cinder.registerForEvents();
    venge.registerForEvents();

    boolean testPassed = true;
    testPassed &= validate(testBasicMod(), "  Basic combat mod test failed.");
    
    cinder.unregister();
    venge.unregister();
    return testPassed;
  }

  /** Test that combat modifiers are applied both in defense and offense. */
  private boolean testBasicMod()
  {
    // Add our test subjects
    Unit infActive = addUnit(testMap, cinder, UnitEnum.INFANTRY, 7, 3);
    infActive.initTurn(testMap); // Make sure he is ready to move.
    Unit infPassive = addUnit(testMap, cinder, UnitEnum.INFANTRY, 7, 5);
    
    // We need a victim and an angry man to avenge him
    Unit bait = addUnit(testMap, venge, UnitEnum.APC, 7, 4);
    Unit meaty = addUnit(testMap, venge, UnitEnum.MD_TANK, 8, 4);
    
    // Poke the bear...
    performGameAction(new GameAction.AttackAction(testMap, infActive, Utils.findShortestPath(infActive, 7, 3, testMap), 7, 4), testMap);

    // See how much damage meaty can do to our two contestants on offense...
    BattleSummary vengeful = CombatEngine.simulateBattleResults(meaty, infActive, testMap, 8, 3);
    BattleSummary normal = CombatEngine.simulateBattleResults(meaty, infPassive, testMap, 8, 5);
    // ...and defense
    BattleSummary vengefulCounter = CombatEngine.simulateBattleResults(infActive, meaty, testMap, 8, 3);
    BattleSummary normalCounter = CombatEngine.simulateBattleResults(infPassive, meaty, testMap, 8, 5);
    
    // Check that Venge's passive ability works on both attack and defense
    boolean testPassed = validate(vengeful.defenderHPLoss > normal.defenderHPLoss, "    Being angry didn't help Venge attack extra hard.");
    testPassed &= validate(vengefulCounter.attackerHPLoss > normalCounter.attackerHPLoss, "    Being angry didn't help Venge defend extra hard.");

    // Clean up
    testMap.removeUnit(infActive);
    testMap.removeUnit(infPassive);
    testMap.removeUnit(bait);
    testMap.removeUnit(meaty);

    return testPassed;
  }
}
