package Test;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.Combat.CombatContext.CalcType;
import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.UnitContext;
import Units.UnitModel;

/**
 * Tests Power of Money variants because I make poor life choices
 */
public class TestColinMath extends TestCase
{
  private Commander colinBW, colin2, colin3, olaf;
  private MapMaster map;
  private GameInstance game;

  @Override
  public boolean runTest()
  {

    boolean testPassed = true;

    testPassed &= validate(testPowerOfMoney() , "  Power of Money test failed!");

    return testPassed;
  }

  private boolean testPowerOfMoney()
  {
    MapInfo mapInfo = MapLibrary.getByName("Deep Forest");
    GameScenario scn = new GameScenario();
    Army[] armies = new Army[mapInfo.getNumPlayers()];
    armies[0] = new Army(scn, CommandingOfficers.AWBW.BM.Olaf.getInfo().create(scn.rules));
    armies[1] = new Army(scn, CommandingOfficers.AWBW.BM.Colin.getInfo().create(scn.rules));
    armies[2] = new Army(scn, CommandingOfficers.AW2.BM.Colin.getInfo().create(scn.rules));
    armies[3] = new Army(scn, CommandingOfficers.AW3.BM.Colin.getInfo().create(scn.rules));

    olaf    = armies[0].cos[0];
    colinBW = armies[1].cos[0];
    colin2  = armies[2].cos[0];
    colin3  = armies[3].cos[0];

    map  = new MapMaster(armies, mapInfo);
    game = new GameInstance(armies, map);

    turn(game);

    boolean testPassed = true;

    UnitContext tankOlaf = new UnitContext(addUnit(map, olaf, UnitModel.ASSAULT, 4, 7)); // On road

    UnitContext tankBW   = new UnitContext(colinBW, colinBW.getUnitModel(UnitModel.ASSAULT, false));
    UnitContext tank2    = new UnitContext(colin2, colin2.getUnitModel(UnitModel.ASSAULT, false));
    UnitContext tank3    = new UnitContext(colin3, colin3.getUnitModel(UnitModel.ASSAULT, false));

    BattleParams shotBW = new CombatContext(game, map, tankBW, tankOlaf, 1, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams shot2  = new CombatContext(game, map, tank2 , tankOlaf, 1, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams shot3  = new CombatContext(game, map, tank3 , tankOlaf, 1, CalcType.NO_LUCK).applyModifiers().getAttack();

    shotBW.attackPower += 10;
    shotBW.attackPower += CommandingOfficers.AWBW.BM.Colin.calcSuperBoost(241700);
    int hitBW = shotBW.calculateDamage();
    testPassed &= validate(hitBW == 453,     "    ColinBW PoM is wrong");

    shot3.attackPower += 10;
    shot3.attackPower += CommandingOfficers.AW3.BM.Colin.calcSuperBoost(241700);
    int hit3 = shot3.calculateDamage();
    testPassed &= validate(hit3 == 497,     "    Colin3 PoM is wrong");

    shot2.attackerDamageMultiplier = CommandingOfficers.AW2.BM.Colin.calcSuperMult(240900);
    int hit2 = shot2.calculateDamage();
    testPassed &= validate(hit2 == 442,     "    Colin2 PoM is wrong");
    shot2.attackerDamageMultiplier = CommandingOfficers.AW2.BM.Colin.calcSuperMult(431660);
    hit2 = shot2.calculateDamage();
    testPassed &= validate(hit2 == 753,     "    Colin2 PoM is wrong");
    shot2.attackerDamageMultiplier = CommandingOfficers.AW2.BM.Colin.calcSuperMult(110000);
    hit2 = shot2.calculateDamage();
    testPassed &= validate(hit2 == 228,     "    Colin2 PoM is wrong");
    shot2.attackerHealth = 40;
    hit2 = shot2.calculateDamage();
    testPassed &= validate(hit2 ==  91,     "    Colin2 PoM is wrong");
    shot2.attackerDamageMultiplier = CommandingOfficers.AW2.BM.Colin.calcSuperMult(640660);
    shot2.attackerHealth = 60;
    hit2 = shot2.calculateDamage();
    testPassed &= validate(hit2 == 657,     "    Colin2 PoM is wrong");

    game.endGame();
    return testPassed;
  }
}
