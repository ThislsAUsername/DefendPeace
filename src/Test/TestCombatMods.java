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
    testPassed &= validate(testTeamAttack(), "  Team attack test failed.");
    testPassed &= validate(testIndirectAttacks(), "  Indirect combat test failed.");
    testPassed &= validate(testMoveAttack(), "  Move-Attack test failed.");
    testPassed &= validate(testKillLastUnit(), "  Last-unit death test failed.");
    
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
  
  /** Test that attacking your friends doesn't work. */
  private boolean testTeamAttack()
  {
    // Add our combatants
    Unit mechA = addUnit(testMap, cinder, UnitEnum.MECH, 1, 1);
    Unit infB = addUnit(testMap, venge, UnitEnum.INFANTRY, 1, 2);
    
    // Make them friends
    cinder.team = 0;
    venge.team = 0;

    // Make sure the infantry will die with one attack
    infB.damageHP(7);

    // Hug the infantry in a friendly manner.
    performGameAction(new GameAction.AttackAction(testMap, mechA, Utils.findShortestPath(mechA, 1, 1, testMap), 1, 2),
        testMap);

    // Check that the mech is undamaged, and that the infantry is still with us.
    boolean testPassed = validate(mechA.getPreciseHP() == 10, "    Attacker lost or gained health.");
    testPassed &= validate(testMap.getLocation(1, 2).getResident() != null, "    Defender died.");

    // Clean up
    testMap.removeUnit(mechA);
    testMap.removeUnit(infB);
    cinder.team = -1;
    venge.team = -1;

    return testPassed;
  }

  /** Test that weapon range works properly, and that immobile weapons cannot move and fire. */
  private boolean testIndirectAttacks()
  {
    // Add our combatants
    Unit offender = addUnit(testMap, cinder, UnitEnum.ARTILLERY, 6, 5);
    Unit defender = addUnit(testMap, venge, UnitEnum.MECH, 6, 6);
    Unit victim = addUnit(testMap, venge, UnitEnum.ARTILLERY, 6, 7);

    // offender will attempt to shoot point blank. This should fail, since artillery cannot direct fire.
    offender.initTurn(testMap); // Make sure he is ready to move.
    performGameAction(new GameAction.AttackAction(testMap, offender, Utils.findShortestPath(offender, 6, 5, testMap), 6, 6),
        testMap);
    boolean testPassed = validate(defender.getPreciseHP() == 10, "    Artillery dealt damage at range 1. Artillery range should be 2-3.");
    
    // offender will attempt to move and fire. This should fail, since artillery cannot fire after moving.
    offender.initTurn(testMap);
    performGameAction(new GameAction.AttackAction(testMap, offender, Utils.findShortestPath(offender, 6, 4, testMap), 6, 6),
        testMap);
    testPassed &= validate(defender.getPreciseHP() == 10, "    Artillery dealt damage despite moving before firing.");

    // offender will shoot victim.
    offender.initTurn(testMap); // Make sure he is ready to move.
    performGameAction(new GameAction.AttackAction(testMap, offender, Utils.findShortestPath(offender, 6, 5, testMap), 6, 7),
        testMap);
    testPassed &= validate(victim.getPreciseHP() != 10, "    Artillery failed to do damage at a range of 2, without moving.");
    testPassed &= validate(offender.getPreciseHP() == 10, "    Artillery received a counterattack from a range of 2. Counterattacks should only be possible at range 1.");

    // defender will attack offender.
    defender.initTurn(testMap); // Make sure he is ready to move.
    performGameAction(new GameAction.AttackAction(testMap, defender, Utils.findShortestPath(defender, 6, 6, testMap), 6, 5),
        testMap);
    
    // check that offender is damaged and defender is not.
    testPassed &= validate(offender.getPreciseHP() != 10, "    Mech failed to deal damage to adjacent artillery.");
    testPassed &= validate(defender.getPreciseHP() == 10, "    Mech receives a counterattack from artillery at range 1. Artillery range should be 2-3.");

    // Clean up
    testMap.removeUnit(offender);
    testMap.removeUnit(defender);
    testMap.removeUnit(victim);

    return testPassed;
  }
  
  /** Test that units can move and attack in one turn. */
  private boolean testMoveAttack()
  {
    // Add our combatants
    Unit mechA = addUnit(testMap, cinder, UnitEnum.MECH, 1, 1);
    Unit infB = addUnit(testMap, venge, UnitEnum.INFANTRY, 1, 3);

    // Execute inf- I mean, the action.
    mechA.initTurn(testMap); // Make sure he is ready to move.
    performGameAction(new GameAction.AttackAction(testMap, mechA, Utils.findShortestPath(mechA, 1, 2, testMap), 1, 3), testMap);

    // Check that the mech is undamaged, and that the infantry is no longer with us.
    boolean testPassed = validate(infB.getHP() < 10, "    Defender took no damage.");
    testPassed &= validate(mechA.getHP() < 10, "    Attacker took no damage.");

    // Clean up
    testMap.removeUnit(mechA);
    testMap.removeUnit(infB);
    cinder.units.clear();
    venge.units.clear();

    return testPassed;
  }

  /** Make sure we generate a CommanderDefeatEvent when killing the final unit for a CO. */
  private boolean testKillLastUnit()
  {
    boolean testPassed = true;

    // Add our combatants
    Unit mechA = addUnit(testMap, cinder, UnitEnum.MECH, 1, 1);
    Unit infB = addUnit(testMap, venge, UnitEnum.INFANTRY, 1, 2);

    // Make sure the infantry will die with one attack
    infB.damageHP(7);

    // Create the attack action so we can predict the unit will die, and his CO will therefore be defeated.
    mechA.initTurn(testMap); // Make sure he is ready to act.
    GameAction battleAction = new GameAction.AttackAction(testMap, mechA, Utils.findShortestPath(mechA, 1, 1, testMap), 1, 2);

    // Extract the resulting GameEventQueue.
    GameEventQueue events = battleAction.getEvents(testMap);

    // Make sure a CommanderDefeatEvent was generated as a result (the actual event test is in TestGameEvent.java).
    boolean hasDefeatEvent = false;
    for( GameEvent event : events )
    {
      if( event instanceof CommanderDefeatEvent )
      {
        hasDefeatEvent = true;
        break;
      }
    }
    testPassed &= validate( hasDefeatEvent, "    No CommanderDefeatEvent generated when losing final unit!");

    return testPassed;
  }
}
