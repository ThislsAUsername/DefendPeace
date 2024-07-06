package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.Army;
import Engine.GameInstance;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.StateTrackers.DSSonjaDebuffTracker;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitMods.UnitModifier;
import Engine.Combat.CombatContext.CalcType;
import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.UnitContext;
import Units.UnitModel;
import lombok.var;

/**
 * Tests DS-ish Sonja and Lash interactions.
 */
public class TestTerrainanigans extends TestCase
{
  private Commander sonjaBW, testSubject, sonjaCart;
  private MapMaster map;
  private GameInstance game;

  private void setupTest(CommanderInfo testSubjectInfo)
  {
    MapInfo mapInfo = MapLibrary.getByName("Deep Forest");
    GameScenario scn = new GameScenario();
    Army[] armies = new Army[mapInfo.getNumCos()];
    armies[0] = new Army(scn, CommandingOfficers.AWBW.YC.Cyrus.getInfo().create(scn.rules));
    armies[1] = new Army(scn, testSubjectInfo.create(scn.rules));
    armies[2] = new Army(scn, testSubjectInfo.create(scn.rules));
    armies[3] = new Army(scn, CommandingOfficers.AW3.YC.Sonja.getInfo().create(scn.rules));

    sonjaBW     = armies[0].cos[0];
    testSubject = armies[1].cos[0];
    sonjaCart   = armies[3].cos[0];

    map  = new MapMaster(armies, mapInfo);
    game = new GameInstance(armies, map);

    turn(game);
  }

  @Override
  public boolean runTest()
  {

    boolean testPassed = true;

    testPassed &= validate(testSonjaDeath() , "  Sonja death test failed!");
//    testPassed &= validate(testSonjaChanges() , "  Lash SCOP test failed!");

    return testPassed;
  }

  private boolean testSonjaDeath()
  {
    setupTest(CommandingOfficers.AWBW.BH.Lash.getInfo());
    boolean testPassed = true;

    var debuffMod = StateTracker.instance(game, DSSonjaDebuffTracker.class);
    testPassed &= validate(debuffMod.debuffers.contains(sonjaCart),        "    Cart Sonja not a debuffer?");
    testPassed &= validate(1 == debuffMod.debuffers.size(),                "    Wrong debuffer count?");
    testPassed &= validate(0 == debuffMod.debuffMap.get(sonjaCart.army),   "    Sonja debuffs herself?");
    testPassed &= validate(1 == debuffMod.debuffMap.get(sonjaBW.army),     "    Sonja can't debuff Sonja?");
    testPassed &= validate(1 == debuffMod.debuffMap.get(testSubject.army), "    Sonja debuffs Lash wrong D2D?");

    UnitContext aa      = new UnitContext(addUnit(map, testSubject, UnitModel.SURFACE_TO_AIR, 2, 6)); // On a mountain
    UnitContext infCart = new UnitContext(addUnit(map, sonjaCart, UnitModel.TROOP, 1, 6)); // In forest next to mountain
    UnitContext infBW   = new UnitContext(addUnit(map, sonjaBW, UnitModel.TROOP, 2, 7)); // On adjacent mountain
    aa.unit.isTurnOver = false;

    BattleParams murderCalc    = new CombatContext(game, map, aa, infCart, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams preMurderCalc = new CombatContext(game, map, aa, infBW,   CalcType.NO_LUCK).applyModifiers().getAttack();
    // Murder; this should make Cart Sonja lose.
    performGameAction(new BattleLifecycle.BattleAction(map, aa.unit, GamePath.stayPut(aa.unit), 1, 6), game);
    testPassed &= validate(0 == infCart.unit.health,  "    Shooting the inf didn't make it dead?");
    testPassed &= validate(sonjaCart.army.isDefeated, "    Cart Sonja didn't lose?");

    BattleParams postMurderCalc = new CombatContext(game, map, aa, infBW, CalcType.NO_LUCK).applyModifiers().getAttack();
    turn(game);
    BattleParams postTurnCalc   = new CombatContext(game, map, aa, infBW, CalcType.NO_LUCK).applyModifiers().getAttack();

    testPassed &= validate(3 == murderCalc.attacker.terrainStars,     "    Shooting cart Sonja loses me terrain stars to Cyrus?");
    testPassed &= validate(2 == preMurderCalc.attacker.terrainStars,  "    Shooting Cyrus doesn't lose me an extra terrain star");
    testPassed &= validate(2 == postMurderCalc.attacker.terrainStars, "    Killing Sonja gets me back my star immedately");
    testPassed &= validate(3 == postTurnCalc.attacker.terrainStars,   "    Killing Sonja never gets me back my star");

    game.endGame();
    return testPassed;
  }
}
