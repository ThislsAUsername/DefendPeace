package Test;

import CommandingOfficers.Cinder;
import CommandingOfficers.Commander;
import CommandingOfficers.Venge;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
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
    GameScenario scn = new GameScenario();
    cinder = new Cinder(scn.rules);
    venge = new Venge(scn.rules);
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
    testPassed &= validate(testIronWill(), "  Venge's Iron Will combat mod test failed.");
    testPassed &= validate(testRetribution(), "  Venge's Retribution combat mod test failed.");
    
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

  /** Test that combat works as a black box, but that Venge gets his way all the same. */
  private boolean testIronWill()
  {
    // Add our test subjects
    Unit infA = addUnit(testMap, cinder, UnitEnum.INFANTRY, 7, 3);
    Unit infB = addUnit(testMap, venge, UnitEnum.INFANTRY, 7, 5);
    
    // Check our damage for each first strike pre-power...
    BattleSummary normalAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4);

    venge.modifyAbilityPower(42); // juice up
    venge.getReadyAbilities().get(0).activate(testMap); // activate Iron WIll
    
    // ...and after power
    BattleSummary ironAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4);
    
    // Check that Venge's Iron Will works properly without breaking things (other than balance)
    boolean testPassed = true;
    testPassed &= validate(infB.model.getDefenseRatio() > 100, "    Iron Will didn't buff defense.");
    
    // First, check the logic of A->B
    testPassed &= validate(normalAB.defenderHPLoss > normalAB.attackerHPLoss, "    First strike didn't work properly for Cinder.");

    testPassed &= validate(ironAB.attacker == infA, "    infA attacked, but isn't the attacker.");
    testPassed &= validate(ironAB.defender == infB, "    infB was attacked, but isn't the defender.");
    testPassed &= validate(ironAB.defenderHPLoss < ironAB.attackerHPLoss, "    Venge didn't defend better, or didn't get Iron Will's buff.");

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
    Unit infA = addUnit(testMap, cinder, UnitEnum.INFANTRY, 7, 3);
    Unit infB = addUnit(testMap, venge, UnitEnum.INFANTRY, 7, 5);
    
    // Check our damage for each first strike pre-power...
    BattleSummary normalAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4);
    BattleSummary normalBA = CombatEngine.simulateBattleResults(infB, infA, testMap, 7, 4);

    venge.modifyAbilityPower(42); // juice up
    venge.getReadyAbilities().get(1).activate(testMap); // activate Retribution
    
    // ...and after power
    BattleSummary retribAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4);
    BattleSummary retribBA = CombatEngine.simulateBattleResults(infB, infA, testMap, 7, 4);
    
    // Check that Venge's Retribution works properly without breaking things (other than balance)
    boolean testPassed = true;
    testPassed &= validate(infB.model.getDamageRatio() > 110, "    Retribution didn't buff offense.");
    testPassed &= validate(infB.model.getDefenseRatio() < 100, "    Retribution didn't reduce defense.");
    
    // First, check the logic of A->B
    testPassed &= validate(normalAB.defenderHPLoss > normalAB.attackerHPLoss, "    First strike didn't work properly for Cinder.");

    testPassed &= validate(retribAB.attacker == infA, "    infA attacked, but isn't the attacker.");
    testPassed &= validate(retribAB.defender == infB, "    infB was attacked, but isn't the defender.");
    testPassed &= validate(retribAB.defenderHPLoss < retribAB.attackerHPLoss, "    Cinder got first strike when Retribution should have stolen it.");

    // Now do B->A
    testPassed &= validate(normalBA.defenderHPLoss > normalBA.attackerHPLoss, "    First strike didn't work properly for Venge.");
    
    testPassed &= validate(normalBA.defenderHPLoss < retribBA.defenderHPLoss, "    Venge didn't deal more damage with buffed offense.");

    testPassed &= validate(retribBA.attacker == infB, "    infB attacked, but isn't the attacker.");
    testPassed &= validate(retribBA.defender == infA, "    infA was attacked, but isn't the defender.");
    testPassed &= validate(retribBA.defenderHPLoss > retribBA.attackerHPLoss, "    Retribution somehow deprived Venge of first strike?");

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);
    venge.initTurn(testMap);

    return testPassed;
  }
}
