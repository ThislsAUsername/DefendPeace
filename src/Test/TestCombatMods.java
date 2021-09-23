package Test;

import CommandingOfficers.Cinder;
import CommandingOfficers.Commander;
import CommandingOfficers.Venge;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class TestCombatMods extends TestCase
{
  private static Commander cinder;
  private static Commander venge;
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    cinder = new Cinder(scn.rules);
    venge = new Venge(scn.rules);
    Commander[] cos = { cinder, venge };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    testGame = new GameInstance(testMap);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testBasicMod(), "  Basic combat mod test failed.");
    testPassed &= validate(testIronWill(), "  Venge's Iron Will combat mod test failed.");
    testPassed &= validate(testRetribution(), "  Venge's Retribution combat mod test failed.");

    return testPassed;
  }

  /** Test that combat modifiers are applied both in defense and offense. */
  private boolean testBasicMod()
  {
    // Add our test subjects
    Unit infActive = addUnit(testMap, cinder, UnitModel.TROOP, 7, 3);
    infActive.initTurn(testMap); // Make sure he is ready to move.
    Unit infPassive = addUnit(testMap, cinder, UnitModel.TROOP, 7, 5);
    
    // We need a victim and an angry man to avenge him
    Unit bait = addUnit(testMap, venge, UnitModel.TRANSPORT, 7, 4);
    Unit meaty = addUnit(testMap, venge, UnitModel.ASSAULT, 8, 4);
    
    // Poke the bear...
    performGameAction(new BattleLifecycle.BattleAction(testMap, infActive, Utils.findShortestPath(infActive, 7, 3, testMap), 7, 4), testGame);

    // See how much damage meaty can do to our two contestants on offense...
    BattleSummary vengeful = CombatEngine.simulateBattleResults(meaty, infActive, testMap, 8, 3);
    BattleSummary normal = CombatEngine.simulateBattleResults(meaty, infPassive, testMap, 8, 5);
    // ...and defense
    BattleSummary vengefulCounter = CombatEngine.simulateBattleResults(infActive, meaty, testMap, 8, 3);
    BattleSummary normalCounter = CombatEngine.simulateBattleResults(infPassive, meaty, testMap, 8, 5);
    
    // Check that Venge's passive ability works on both attack and defense
    boolean testPassed = validate(vengeful.defender.deltaHP < normal.defender.deltaHP, "    Being angry didn't help Venge attack extra hard.");
    testPassed &= validate(vengefulCounter.attacker.deltaHP < normalCounter.attacker.deltaHP, "    Being angry didn't help Venge defend extra hard.");

    // Clean up
    testMap.removeUnit(infActive);
    testMap.removeUnit(infPassive);
    testMap.removeUnit(bait);
    testMap.removeUnit(meaty);

    return testPassed;
  }

  /** Test that combat works as a black box, but that Venge gets his way all the same. */
  private boolean testIronWill()
  {
    // Add our test subjects; road inf attacking into plains inf
    Unit infA = addUnit(testMap, cinder, UnitModel.TROOP, 7, 4);
    Unit infB = addUnit(testMap, venge, UnitModel.TROOP, 8, 5);
    infB.isTurnOver = false;
    
    // Check our damage for each first strike pre-power...
    BattleSummary normalAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 5);

    venge.modifyAbilityPower(42); // juice up
    performGameAction(new GameAction.AbilityAction(venge, venge.getReadyAbilities().get(0)), testGame); // activate Iron Will
    
    // ...and after power
    BattleSummary ironAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 5);
    
    // Check that Venge's Iron Will works properly without breaking things (other than balance)
    boolean testPassed = true;
    // First, check the logic of A->B
    testPassed &= validate(normalAB.defender.deltaPreciseHP < normalAB.attacker.deltaPreciseHP, "    First strike didn't work properly for Cinder.");

    testPassed &= validate(ironAB.attacker.unit == infA, "    infA attacked, but isn't the attacker.");
    testPassed &= validate(ironAB.defender.unit == infB, "    infB was attacked, but isn't the defender.");
    testPassed &= validate(ironAB.defender.deltaPreciseHP > ironAB.attacker.deltaPreciseHP, "    Venge didn't defend better, or didn't get Iron Will's buff.");

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);
    venge.initTurn(testMap);

    return testPassed;
  }

  /** Test that combat works as a black box, but that Venge gets his way all the same. */
  private boolean testRetribution()
  {
    // Add our test subjects
    Unit infA = addUnit(testMap, cinder, UnitModel.TROOP, 7, 3);
    Unit infB = addUnit(testMap, venge, UnitModel.TROOP, 7, 5);

    // Check our damage for each first strike pre-power...
    BattleSummary normalAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4);
    BattleSummary normalBA = CombatEngine.simulateBattleResults(infB, infA, testMap, 7, 4);

    venge.modifyAbilityPower(42); // juice up
    performGameAction(new GameAction.AbilityAction(venge, venge.getReadyAbilities().get(1)), testGame); // activate Retribution

    // ...and after power
    BattleSummary retribAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4);
    BattleSummary retribBA = CombatEngine.simulateBattleResults(infB, infA, testMap, 7, 4);

    // Check that Venge's Retribution works properly without breaking things (other than balance)
    boolean testPassed = true;
    testPassed &= validate(infB.model.getDamageRatio() > 110, "    Retribution didn't buff offense.");
    testPassed &= validate(infB.model.getDefenseRatio() < 100, "    Retribution didn't reduce defense.");

    // First, check the logic of A->B
    testPassed &= validate(normalAB.defender.deltaHP < normalAB.attacker.deltaHP, "    First strike didn't work properly for Cinder.");

    testPassed &= validate(retribAB.attacker.unit == infA, "    infA attacked, but isn't the attacker.");
    testPassed &= validate(retribAB.defender.unit == infB, "    infB was attacked, but isn't the defender.");
    testPassed &= validate(retribAB.defender.deltaHP > retribAB.attacker.deltaHP, "    Cinder got first strike when Retribution should have stolen it.");

    // Now do B->A
    testPassed &= validate(normalBA.defender.deltaHP < normalBA.attacker.deltaHP, "    First strike didn't work properly for Venge.");

    testPassed &= validate(normalBA.defender.deltaHP > retribBA.defender.deltaHP, "    Venge didn't deal more damage with buffed offense.");

    testPassed &= validate(retribBA.attacker.unit == infB, "    infB attacked, but isn't the attacker.");
    testPassed &= validate(retribBA.defender.unit == infA, "    infA was attacked, but isn't the defender.");
    testPassed &= validate(retribBA.defender.deltaHP < retribBA.attacker.deltaHP, "    Retribution somehow deprived Venge of first strike?");

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);
    venge.initTurn(testMap);

    return testPassed;
  }
}
