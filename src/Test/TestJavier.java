package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;
import Engine.Combat.CombatContext.CalcType;
import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Maps.MapReader;
import Units.UnitContext;
import Units.UnitModel;

public class TestJavier extends TestCase
{
  private Commander javierAttack;
  private Commander javierDefend;
  private MapMaster map;
  private GameInstance game;

  private void setupTest(MapInfo mapInfo)
  {
    GameScenario scn = new GameScenario();
    Army[] armies = new Army[mapInfo.getNumCos()];
    for( int i = 0; i < armies.length; ++i )
      armies[i] = new Army(scn, CommandingOfficers.AW3.GE.Javier.getInfo().create(scn.rules));

    javierAttack = armies[0].cos[0];
    javierDefend = armies[1].cos[0];

    map = new MapMaster(armies, mapInfo);
    game = new GameInstance(armies, map);

    turn(game);
  }

  @Override
  public boolean runTest()
  {

    boolean testPassed = true;

    testPassed &= validate(testNoTowerStats() , "  Javier 0T test failed!");
    testPassed &= validate(testDoRTowerStats(), "  Javier DoR tower test failed!");
    testPassed &= validate(testDSTowerStats() , "  Javier DS tower test failed!");

    return testPassed;
  }

  private boolean testNoTowerStats()
  {
    MapInfo mapInfo = MapLibrary.getByName("Deep Forest");
    setupTest(mapInfo);
    boolean testPassed = true;

    UnitContext infA = new UnitContext(addUnit(map, javierAttack, UnitModel.TROOP, 7, 3));
    UnitContext arty = new UnitContext(addUnit(map, javierAttack, UnitModel.SIEGE, 7, 6));
    UnitContext infB = new UnitContext(addUnit(map, javierDefend, UnitModel.TROOP, 7, 5));
    ArrayList<UnitModifier> baseMods = new ArrayList<>(infB.mods);

    BattleParams shotNormal = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyNormal = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    infB.mods.clear();
    infB.mods.addAll(baseMods);
    javierDefend.myAbilities.get(0).enqueueUnitMods(map, infB.mods);
    BattleParams shotOnCOP  = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyOnCOP  = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    infB.mods.clear();
    infB.mods.addAll(baseMods);
    javierDefend.myAbilities.get(1).enqueueUnitMods(map, infB.mods);
    BattleParams shotOnSCOP = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyOnSCOP = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    testPassed &= validate(100 == shotNormal.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(100 == shotNormal.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(100 == artyNormal.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(120 == artyNormal.defenseSubtraction, "    Defense is wrong" );

    testPassed &= validate(100 == shotOnCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(110 == shotOnCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(100 == artyOnCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(150 == artyOnCOP.defenseSubtraction, "    Defense is wrong" );

    testPassed &= validate(100 == shotOnSCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(110 == shotOnSCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(100 == artyOnSCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(190 == artyOnSCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(0 == artyOnSCOP.calculateDamage(), "    Defense is wrong" );

    game.endGame();
    return testPassed;
  }

  private boolean testDoRTowerStats()
  {
    MapInfo mapInfo = MapLibrary.getByName("Deep Forest");
    setupTest(mapInfo);
    boolean testPassed = true;

    // Grab a tower
    map.getLocation(3, 4).setOwner(javierDefend);

    UnitContext infA = new UnitContext(addUnit(map, javierAttack, UnitModel.TROOP, 7, 3));
    UnitContext arty = new UnitContext(addUnit(map, javierAttack, UnitModel.SIEGE, 7, 6));
    UnitContext infB = new UnitContext(addUnit(map, javierDefend, UnitModel.TROOP, 7, 5));
    ArrayList<UnitModifier> baseMods = new ArrayList<>(infB.mods);

    BattleParams shotNormal = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyNormal = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    infB.mods.clear();
    infB.mods.addAll(baseMods);
    javierDefend.myAbilities.get(0).enqueueUnitMods(map, infB.mods);
    BattleParams shotOnCOP  = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyOnCOP  = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    infB.mods.clear();
    infB.mods.addAll(baseMods);
    javierDefend.myAbilities.get(1).enqueueUnitMods(map, infB.mods);
    BattleParams shotOnSCOP = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyOnSCOP = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    testPassed &= validate(110 == shotNormal.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(100 == shotNormal.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(110 == artyNormal.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(120 == artyNormal.defenseSubtraction, "    Defense is wrong" );

    testPassed &= validate(120 == shotOnCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(110 == shotOnCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(120 == artyOnCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(150 == artyOnCOP.defenseSubtraction, "    Defense is wrong" );

    testPassed &= validate(130 == shotOnSCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(110 == shotOnSCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(130 == artyOnSCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(190 == artyOnSCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(0 == artyOnSCOP.calculateDamage(), "    Defense is wrong" );

    game.endGame();
    return testPassed;
  }

  private boolean testDSTowerStats()
  {
    // Generate a non-cached version of the map info so we can mess with it
    MapInfo mapInfo = MapReader.readSingleMap(Engine.Driver.JAR_DIR + "res/map/Deep_Forest.map");
    mapInfo.terrain[3][4] = TerrainType.DS_TOWER;
    setupTest(mapInfo);
    boolean testPassed = true;

    // Grab a tower
    map.getLocation(3, 4).setOwner(javierDefend);

    UnitContext infA = new UnitContext(addUnit(map, javierAttack, UnitModel.TROOP, 7, 3));
    UnitContext arty = new UnitContext(addUnit(map, javierAttack, UnitModel.SIEGE, 7, 6));
    UnitContext infB = new UnitContext(addUnit(map, javierDefend, UnitModel.TROOP, 7, 5));
    ArrayList<UnitModifier> baseMods = new ArrayList<>(infB.mods);

    BattleParams shotNormal = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyNormal = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    infB.mods.clear();
    infB.mods.addAll(baseMods);
    javierDefend.myAbilities.get(0).enqueueUnitMods(map, infB.mods);
    BattleParams shotOnCOP  = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyOnCOP  = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    infB.mods.clear();
    infB.mods.addAll(baseMods);
    javierDefend.myAbilities.get(1).enqueueUnitMods(map, infB.mods);
    BattleParams shotOnSCOP = new CombatContext(game, map, infA, infB, CalcType.NO_LUCK).applyModifiers().getAttack();
    BattleParams artyOnSCOP = new CombatContext(game, map, arty, infB, CalcType.NO_LUCK).applyModifiers().getAttack();

    testPassed &= validate(100 == shotNormal.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(110 == shotNormal.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(100 == artyNormal.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(130 == artyNormal.defenseSubtraction, "    Defense is wrong" );

    testPassed &= validate(100 == shotOnCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(130 == shotOnCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(100 == artyOnCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(170 == artyOnCOP.defenseSubtraction, "    Defense is wrong" );

    testPassed &= validate(100 == shotOnSCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(140 == shotOnSCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(100 == artyOnSCOP.defenseDivision,    "    Defense is wrong" );
    testPassed &= validate(220 == artyOnSCOP.defenseSubtraction, "    Defense is wrong" );
    testPassed &= validate(0 == artyOnSCOP.calculateDamage(), "    Defense is wrong" );

    game.endGame();
    return testPassed;
  }
}
