package Test;

import CommandingOfficers.Cinder;
import CommandingOfficers.Commander;
import CommandingOfficers.DefendPeace.misc.Venge;
import Engine.Army;
import Engine.GameInstance;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.Combat.CombatContext.CalcType;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.ResupplyEvent;
import Engine.StateTrackers.DamageDealtToIncomeConverter;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitMods.UnitDamageModifier;
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
    Army[] cos = { new Army(scn, cinder), new Army(scn, venge) };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
    testGame = new GameInstance(cos, testMap);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testBasicMod(), "  Basic combat mod test failed.");
    testPassed &= validate(testIronWill(), "  Venge's Iron Will combat mod test failed.");
    testPassed &= validate(testRetribution(), "  Venge's Retribution combat mod test failed.");
    testPassed &= validate(testDamageDealtToIncomeConverter(), "  DamageDealtToIncomeConverter test failed.");

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
    BattleSummary vengeful = CombatEngine.simulateBattleResults(meaty, infActive, testMap, 8, 3, CalcType.NO_LUCK);
    BattleSummary normal = CombatEngine.simulateBattleResults(meaty, infPassive, testMap, 8, 5, CalcType.NO_LUCK);
    // ...and defense
    BattleSummary vengefulCounter = CombatEngine.simulateBattleResults(infActive, meaty, testMap, 8, 3, CalcType.NO_LUCK);
    BattleSummary normalCounter = CombatEngine.simulateBattleResults(infPassive, meaty, testMap, 8, 5, CalcType.NO_LUCK);

    // Check that Venge's passive ability works on both attack and defense
    boolean testPassed = validate(vengeful.defender.deltaHealth < normal.defender.deltaHealth, "    Being angry didn't help Venge attack extra hard.");
    testPassed &= validate(vengefulCounter.attacker.deltaHealth < normalCounter.attacker.deltaHealth, "    Being angry didn't help Venge defend extra hard.");

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
    BattleSummary normalAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 5, CalcType.NO_LUCK);

    venge.modifyAbilityStars(42); // juice up
    venge.getReadyAbilities().get(0).activate(testMap); // activate Iron Will

    // ...and after power
    BattleSummary ironAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 5, CalcType.NO_LUCK);

    // Check that Venge's Iron Will works properly without breaking things (other than balance)
    boolean testPassed = true;
    // First, check the logic of A->B
    testPassed &= validate(normalAB.defender.deltaPreciseHealth < normalAB.attacker.deltaPreciseHealth, "    First strike didn't work properly for Cinder.");

    testPassed &= validate(ironAB.attacker.unit == infA, "    infA attacked, but isn't the attacker.");
    testPassed &= validate(ironAB.defender.unit == infB, "    infB was attacked, but isn't the defender.");
    testPassed &= validate(ironAB.defender.deltaPreciseHealth > ironAB.attacker.deltaPreciseHealth, "    Venge didn't defend better, or didn't get Iron Will's buff.");

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);
    performEvents(testGame, venge.army.initTurn(testMap));

    return testPassed;
  }

  /** Test that combat works as a black box, but that Venge gets his way all the same. */
  private boolean testRetribution()
  {
    // Add our test subjects
    Unit infA = addUnit(testMap, cinder, UnitModel.TROOP, 7, 3);
    Unit infB = addUnit(testMap, venge, UnitModel.TROOP, 7, 5);

    // Check our damage for each first strike pre-power...
    BattleSummary normalAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4, CalcType.NO_LUCK);
    BattleSummary normalBA = CombatEngine.simulateBattleResults(infB, infA, testMap, 7, 4, CalcType.NO_LUCK);

    venge.modifyAbilityStars(42); // juice up
    venge.getReadyAbilities().get(1).activate(testMap); // activate Retribution

    // ...and after power
    BattleSummary retribAB = CombatEngine.simulateBattleResults(infA, infB, testMap, 7, 4, CalcType.NO_LUCK);
    BattleSummary retribBA = CombatEngine.simulateBattleResults(infB, infA, testMap, 7, 4, CalcType.NO_LUCK);

    // Check that Venge's Retribution works properly without breaking things (other than balance)
    boolean testPassed = true;

    // First, check the logic of A->B
    testPassed &= validate(normalAB.defender.deltaHealth < normalAB.attacker.deltaHealth, "    First strike didn't work properly for Cinder.");

    testPassed &= validate(retribAB.attacker.unit == infA, "    infA attacked, but isn't the attacker.");
    testPassed &= validate(retribAB.defender.unit == infB, "    infB was attacked, but isn't the defender.");
    testPassed &= validate(retribAB.defender.deltaHealth > retribAB.attacker.deltaHealth, "    Cinder got first strike when Retribution should have stolen it.");

    // Now do B->A
    testPassed &= validate(normalBA.defender.deltaHealth < normalBA.attacker.deltaHealth, "    First strike didn't work properly for Venge.");
    testPassed &= validate(normalBA.defender.deltaHealth > retribBA.defender.deltaHealth, "    Venge didn't deal more damage with buffed offense.");

    testPassed &= validate(retribBA.attacker.unit == infB, "    infB attacked, but isn't the attacker.");
    testPassed &= validate(retribBA.defender.unit == infA, "    infA was attacked, but isn't the defender.");
    testPassed &= validate(retribBA.defender.deltaHealth < retribBA.attacker.deltaHealth, "    Retribution somehow deprived Venge of first strike?");

    // Clean up
    testMap.removeUnit(infA);
    testMap.removeUnit(infB);
    performEvents(testGame, venge.army.initTurn(testMap));

    return testPassed;
  }

  private boolean testDamageDealtToIncomeConverter()
  {
    boolean testPassed = true; // Assume nothin's busted
    DamageDealtToIncomeConverter ddtic = DamageDealtToIncomeConverter.instance(testGame, DamageDealtToIncomeConverter.class);

    Unit aa = addUnit(testMap, cinder, UnitModel.SURFACE_TO_AIR, 7, 3);
    // juice aa to be extra angry and get overkills
    aa.alterHealth(10, true);
    aa.addUnitModifier(new UnitDamageModifier(42));
    final GamePath aaPath = Utils.findShortestPath(aa, 7, 3, testMap);

    int currentFundsReturn = 0;
    // Get lots of free money, and make sure we get the right amount
    for( int i = 0; i < 100; ++i )
    {
      currentFundsReturn += i;
      ddtic.startTracking(cinder.army, i);
      cinder.army.money = 0;

      // give aa ammo
      aa.isTurnOver = false;
      ResupplyEvent event = new ResupplyEvent(null, aa);
      event.performEvent( testGame.gameMap );
      GameEventListener.publishEvent(event, testGame);

      // Add the victim, and yeet him
      addUnit(testMap, venge, UnitModel.TROOP, 7, 4);
      performGameAction(new BattleLifecycle.BattleAction(testMap, aa, aaPath, 7, 4), testGame);

      testPassed &= validate(cinder.army.money == currentFundsReturn * 1000, "    Expected to make "+currentFundsReturn*1000+", but got "+cinder.army.money+" instead.");
    }
    // Remove those amounts, and make sure that works
    for( int i = 0; i < 55; ++i )
    {
      currentFundsReturn -= i;
      ddtic.stopTracking(cinder.army, i);
      ddtic.stopTracking(cinder.army, i); // Remove twice, so we can be sure that extra removals don't break things
      cinder.army.money = 0;

      // give aa ammo
      aa.isTurnOver = false;
      ResupplyEvent event = new ResupplyEvent(null, aa);
      event.performEvent( testGame.gameMap );
      GameEventListener.publishEvent(event, testGame);

      // Add the victim, and yeet him
      addUnit(testMap, venge, UnitModel.TROOP, 7, 4);
      performGameAction(new BattleLifecycle.BattleAction(testMap, aa, aaPath, 7, 4), testGame);

      testPassed &= validate(cinder.army.money == currentFundsReturn * 1000, "    Expected to make "+currentFundsReturn*1000+", but got "+cinder.army.money+" instead.");
    }
    // Do the second half backwards, for giggles
    for( int i = 99; i > 42; --i )
    {
      currentFundsReturn -= i;
      if( currentFundsReturn < 0 )
        currentFundsReturn = 0;
      ddtic.stopTracking(cinder.army, i);
      cinder.army.money = 0;

      // give aa ammo
      aa.isTurnOver = false;
      ResupplyEvent event = new ResupplyEvent(null, aa);
      event.performEvent( testGame.gameMap );
      GameEventListener.publishEvent(event, testGame);

      // Add the victim, and yeet him
      addUnit(testMap, venge, UnitModel.TROOP, 7, 4);
      performGameAction(new BattleLifecycle.BattleAction(testMap, aa, aaPath, 7, 4), testGame);

      testPassed &= validate(cinder.army.money == currentFundsReturn * 1000, "    Expected to make "+currentFundsReturn*1000+", but got "+cinder.army.money+" instead.");
    }

    // Clean up
    testMap.removeUnit(aa);

    return testPassed;
  }

}
