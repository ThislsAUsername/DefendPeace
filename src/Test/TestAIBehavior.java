package Test;

import AI.AICombatUtils;
import AI.AIController;
import AI.AIMaker;
import AI.Muriel;
import AI.WallyAI;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DefendPeace.CyanOcean.Patch;
import CommandingOfficers.DefendPeace.RoseThorn.Strong;
import Engine.Army;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Maps.MapReader;
import Units.Unit;
import lombok.var;

public class TestAIBehavior extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static AIMaker[] ais = { Muriel.info, WallyAI.info };
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest(AIMaker ai)
  {
    setupTest(Terrain.Maps.FiringRange.getMapInfo(), ai);
  }

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest(MapInfo mapInfo, AIMaker ai)
  {
    setupTest(mapInfo, ai, Strong.getInfo(), Patch.getInfo());
  }
  private void setupTest(MapInfo mapInfo, AIMaker ai, CommanderInfo co1, CommanderInfo co2)
  {
    GameScenario scn = new GameScenario();
    testCo1 = co1.create(scn.rules);
    testCo2 = co2.create(scn.rules);
    Army[] cos = { new Army(scn, testCo1), new Army(scn, testCo2) };

    AIController testAI = ai.create(cos[0]);
    testAI.setLogging(false);
    cos[0].setAIController(testAI);

    testMap = new MapMaster(cos, mapInfo);
    testGame = new GameInstance(cos, testMap);
  }

  private void cleanupTest()
  {
    testCo1 = null;
    testCo2 = null;
    testMap = null;
    testGame = null;
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = true;

    testPassed &= validate(testWalkInLine(Muriel.info), "  "+Muriel.info.getName()+" failed line walking test.");
    for( AIMaker ai : ais )
    {
      testPassed &= validate(testBuildMegatank(ai), "  "+ai.getName()+" failed build Megatank test.");
      testPassed &= validate(testHuntStall(ai), "  "+ai.getName()+" failed hunting stall-test.");
      testPassed &= validate(testClearAttackRoute(ai), "  "+ai.getName()+" failed route clearing test.");
      testPassed &= validate(testInfWadeThroughTanks(ai), "  "+ai.getName()+" failed Infantry move priority test.");
      testPassed &= validate(testTankWadeThroughInfs(ai), "  "+ai.getName()+" failed Tank move priority test.");
    }
    testPassed &= validate(testProductionClearing(WallyAI.info), "  Free up industry test failed.");
    testPassed &= validate(testUnCapture(WallyAI.info), "  Inf distraction test failed.");
    testPassed &= validate(testInfOptimization(WallyAI.info), "  Inf optimization test failed.");
    testPassed &= validate(testWalling(WallyAI.info), "  Walling test failed.");

    return testPassed;
  }

  /** Confirm that the AI will build the correct counter, even when there is only one possible counter. */
  private boolean testBuildMegatank(AIMaker ai)
  {
    setupTest(MapReader.readSingleMap("src/Test/TestProduceMega.map"), ai);

    // Provide resources.
    testCo1.army.money = 80000;

    // Run through the turn's actions.
    turn(testGame);
    boolean testPassed = true;
    GameAction act = testCo1.army.getNextAIAction(testMap);
    testPassed &= validate(performGameAction(act, testGame), "    Produced an invalid action!");

    // Should have built a Megatank as the best/only viable unit to counter an enemy Megatank.
    testPassed &= validate(testCo1.units.size() > 0, "    Failed to produce a unit!");
    if( testPassed )
      testPassed &= validate(testCo1.units.get(0).model.name.contentEquals("Megatank"), "    "+ai.getName()+" didn't build the right thing!");

    // Clean up
    cleanupTest();

    return testPassed;
  }

  /** Confirm that the AI will avoid stepping on its own factory. */
  private boolean testHuntStall(AIMaker ai)
  {
    setupTest(ai);

    // Where are things?
    XYCoord tankStart = new XYCoord(2, 1);
    XYCoord facPos = new XYCoord(7, 1);

    // Add some units, and grant us a factory so we can avoid blocking it.
    Unit myTank = addUnit(testMap, testCo1, "Md Tank", tankStart.x, tankStart.y);
    addUnit(testMap, testCo2, "Recon", 10, 1);
    testMap.setOwner(testCo1, facPos);

    // Verify that we own the factory in question.
    boolean testPassed = validate(testCo1.ownedProperties.contains(facPos), "    Failed to assign factory.");

    // Run through the turn's actions.
    turn(testGame);
    GameAction act = null;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    // Should have moved the Md Tank towards the enemy Recon, but not ended top of the factory.
    XYCoord tankEnd = new XYCoord(myTank.x, myTank.y);
    testPassed &= validate(!tankEnd.equals(tankStart), "    "+ai.getName()+" did not move the Md Tank!");
    testPassed &= validate(!tankEnd.equals(facPos), "    "+ai.getName()+" blocked a factory!");

    // Clean up
    cleanupTest();

    return testPassed;
  }

  /** Put some infantry in between an AA and its quarry. See if they will move out of the way. */
  @SuppressWarnings("unused")
  private boolean testClearAttackRoute(AIMaker ai)
  {
    setupTest(ai);

    // Pre-own some properties so they are out of the way.
    testMap.setOwner(testCo1, 2, 5 );
    testMap.setOwner(testCo1, 4, 4 );
    testMap.setOwner(testCo1, 4, 8 );
    testMap.setOwner(testCo1, 6, 1 );
    testMap.setOwner(testCo1, 7, 1 );
    testMap.setOwner(testCo1, 8, 8 );
    testMap.setOwner(testCo1, 12, 7);
    testMap.setOwner(testCo1, 10, 5);

    // Where are things?
    XYCoord facPos = new XYCoord(7, 8);

    // Add an enemy copter on a neutral fac, flanked by friendly infs, with a friendly AA nearby.
    Unit nmeCopter = addUnit(testMap, testCo2, "B-Copter", facPos);
    Unit myAA = addUnit(testMap, testCo1, "Anti-Air", facPos.up().left());
    Unit iLeft = addUnit(testMap, testCo1, "Infantry", facPos.left());
    Unit iUp = addUnit(testMap, testCo1, "Infantry", facPos.up());
    Unit iRight = addUnit(testMap, testCo1, "Infantry", facPos.right());

    // The infs all want to cap the fac, but can't because it is occupied. Gotta let the AA through.
    turn(testGame);
    testCo1.army.money = 0; // No production needed for this test.

    // Run through the turn's actions.
    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    testPassed &= validate(nmeCopter.getHealth() < 10, "    "+ai.getName()+" failed to attack enemy copter!");
    testPassed &= validate(!testMap.isLocationEmpty(facPos), "    "+ai.getName()+" failed to start capping the factory!");

    // Clean up
    cleanupTest();
    return testPassed;
  }

  /** Two units want to go the same way. One is where the other should end up. Make sure they coordinate. */
  /** START: [ ] [ ] INF [ ] [ ] INF [ ] [ ] [ ] HQ  */
  /** GOAL : [ ] [ ] [ ] [ ] [ ] INF [ ] [ ] INF HQ */
  private boolean testWalkInLine(AIMaker ai)
  {
    // Generate a custom tiny map to keep things simple.
    TerrainType[] HQCol = {TerrainType.HEADQUARTERS};
    TerrainType[] GRCol = {TerrainType.GRASS};
    TerrainType[][] terrainData =
      {HQCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, HQCol};
    XYCoord[] co1Props = { new XYCoord(0, 0) };
    XYCoord[] co2Props = { new XYCoord(9, 0) };
    XYCoord[][] properties = { co1Props, co2Props };
    MapInfo mapInfo = new MapInfo("LineTest", terrainData, properties);
    setupTest(mapInfo, ai);

    // Add two infantry.
    Unit lt = addUnit(testMap, testCo1, "Infantry", new XYCoord(2, 0));
    Unit rt = addUnit(testMap, testCo1, "Infantry", new XYCoord(5, 0));

    // The infs will want to cap the enemy HQ. Make sure the first one can
    // displace the second one so they both make best speed.
    turn(testGame);
    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    testPassed &= validate(lt.x == 5, "    "+ai.getName()+" failed to move left infantry as intended!");
    testPassed &= validate(rt.x == 8, "    "+ai.getName()+" failed to move right infantry as intended!");

    // Clean up
    cleanupTest();
    return testPassed;
  }

  /** An inf needs to wade through a bunch of Md Tanks to cap the HQ. */
  /** START: [ ] [ ] Inf MdT MdT MdT MdT MdT MdT MdT/HQ  */
  /** GOAL : [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] Inf/HQ */
  private boolean testInfWadeThroughTanks(AIMaker ai)
  {
    // Generate a custom tiny map to keep things simple.
    TerrainType[] HQCol = {TerrainType.HEADQUARTERS};
    TerrainType[] GRCol = {TerrainType.GRASS};
    TerrainType[][] terrainData =
      {HQCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, HQCol};
    XYCoord[] co1Props = { new XYCoord(0, 0) };
    XYCoord[] co2Props = { new XYCoord(9, 0) };
    XYCoord[][] properties = { co1Props, co2Props };
    MapInfo mapInfo = new MapInfo("LineTest", terrainData, properties);
    setupTest(mapInfo, ai);

    // Add one infantry and a bunch of tanks in the way
    Unit inf = addUnit(testMap, testCo1, "Infantry", new XYCoord(2, 0));
    for( int xx = 3; xx < 10; ++xx)
      addUnit(testMap, testCo1, "Md Tank", new XYCoord(xx, 0));
    turn(testGame);

    // The inf will want to cap the enemy HQ. Make sure the tanks get out of the way.
    boolean testPassed = true;
    int turnLimit = 4; // This should be enough time to cap the enemy HQ.
    for( int tt = 0; tt < turnLimit; ++tt )
    {
      day(testGame);
      GameAction act = null;
      do
      {
        act = testCo1.army.getNextAIAction(testMap);
        if( null != act )
          testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
      } while( null != act && testPassed );
    }

    testPassed &= validate(inf.x == 9, "    "+ai.getName()+" failed to move infantry onto HQ!");
    testPassed &= validate(testMap.getLocation(9, 0).getOwner() == inf.CO, "    "+ai.getName()+" failed to capture the HQ!");

    // Clean up
    cleanupTest();
    return testPassed;
  }

  /** Two units want to go the same way. One is where the other should end up. Make sure they coordinate. */
  /** START: [ ] MdT Inf Inf Inf Inf Inf Inf Inf Rty/HQ  */
  /** GOAL : [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] MdT Inf/HQ */
  private boolean testTankWadeThroughInfs(AIMaker ai)
  {
    // Generate a custom tiny map to keep things simple.
    TerrainType[] HQCol = {TerrainType.HEADQUARTERS};
    TerrainType[] GRCol = {TerrainType.GRASS};
    TerrainType[][] terrainData =
      {HQCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, GRCol, HQCol};
    XYCoord[] co1Props = { new XYCoord(0, 0) };
    XYCoord[] co2Props = { new XYCoord(9, 0) };
    XYCoord[][] properties = { co1Props, co2Props };
    MapInfo mapInfo = new MapInfo("LineTest", terrainData, properties);
    setupTest(mapInfo, ai);

    // Add one Md Tank, an enemy arty, and a bunch of infantry in the way
    for( int xx = 2; xx < 9; ++xx)
      addUnit(testMap, testCo1, "Infantry", new XYCoord(xx, 0));
    Unit nmeArty = addUnit(testMap, testCo2, "Artillery", new XYCoord(9, 0)); // Target artillery
    Unit inf = addUnit(testMap, testCo1, "Md Tank", new XYCoord(1, 0)); // Create tank last just to make the AI compensate.
    turn(testGame);

    // The inf will want to cap the enemy HQ. Make sure the tanks get out of the way.
    boolean testPassed = true;
    int turnLimit = 4; // This should be enough time to cap the enemy HQ.
    for( int tt = 0; tt < turnLimit; ++tt )
    {
      day(testGame);
      GameAction act = null;
      do
      {
        act = testCo1.army.getNextAIAction(testMap);
        if( null != act )
          testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
      } while( null != act && testPassed );
    }

    testPassed &= validate(nmeArty.getHealth() <= 0, "    "+ai.getName()+" failed to kill the artillery!");
    testPassed &= validate(testMap.getResident(9, 0).CO == inf.CO, "    "+ai.getName()+" failed to move infantry onto HQ!");
    testPassed &= validate(testMap.getLocation(9, 0).getOwner() == inf.CO, "    "+ai.getName()+" failed to capture the HQ!");

    // Clean up
    cleanupTest();
    return testPassed;
  }

  /** Confirm that the AI will clear its factory to build a counter, when there is only one possible counter. */
  private boolean testProductionClearing(AIMaker ai)
  {
    setupTest(MapReader.readSingleMap("src/Test/TestProductionClearing.map"), ai);

    // Give resources.
    testCo1.army.money = 80000;

    turn(testGame);
    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    // Should have built a Megatank as the best/only viable unit to counter an enemy Megatank.
    testPassed = validate(testCo1.units.size() > 0, "    Failed to produce a unit!");

    boolean foundMega = false;
    for( Unit u : testCo1.units )
    {
      foundMega |= u.model.name.contentEquals("Megatank");
      if( foundMega )
        break;
    }
    testPassed &= validate(foundMega, "    "+ai.getName()+" didn't build the right thing!");

    // Clean up
    cleanupTest();

    return testPassed;
  }

  /** Test that I will interrupt a city capture to interrupt a neutral factory capture */
  private boolean testUnCapture(AIMaker ai)
  {
    setupTest(MapReader.readSingleMap("src/Test/TestInfOptimization.map"), ai,
              CommandingOfficers.AW1.YC.Kanbei.getInfo(),
              CommandingOfficers.AW4.BrennerWolves.Will.getInfo());

    Unit interrupter = testMap.getResident(2, 1);
    Unit target      = testMap.getResident(5, 1);
    interrupter.capture(testMap);
    target     .capture(testMap);
    turn(testGame);

    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    testPassed &= validate(interrupter.getCaptureProgress() == 0, "    "+ai.getName()+" didn't stop capturing");
    testPassed &= validate(target     .getHP() < 10, "    "+ai.getName()+" didn't hit the target");

    // Clean up
    cleanupTest();

    return testPassed;
  }

  /**
   * We have 3 objectives:
   * Clear the neutral factory of a capping inf
   * Start capping that factory
   * Also start capping the tile the factory capper was previously capping
   */
  private boolean testInfOptimization(AIMaker ai)
  {
    setupTest(MapReader.readSingleMap("src/Test/TestInfOptimization.map"), ai,
              CommandingOfficers.AW1.YC.Kanbei.getInfo(),
              CommandingOfficers.AW4.BrennerWolves.Will.getInfo());

    // Starts capping the city after cappy abandons it
    Unit scab        = addUnit(testMap, testCo1, "Mech", new XYCoord(0, 1));
    // Stops capping to go to greener pastures
    Unit cappy       = testMap.getResident(2, 1);
    // Shoots from the north tile
    Unit shootyOne   = addUnit(testMap, testCo1, "Infantry", new XYCoord(2, 0));
    // Shoots from the west tile
    Unit shootyTwo   = addUnit(testMap, testCo1, "Infantry", new XYCoord(2, 2));
    // Did nothing wrong
    Unit target      = testMap.getResident(5, 1);
    cappy .capture(testMap);
    target.capture(testMap);
    turn(testGame);

    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    testPassed &= validate(target.getHP() == 0                 , "    "+ai.getName()+" didn't kill the target");
    testPassed &= validate(5 == shootyOne.x && 0 == shootyOne.y, "    "+ai.getName()+" didn't hit the factory with ShootyOne");
    testPassed &= validate(4 == shootyTwo.x && 1 == shootyTwo.y, "    "+ai.getName()+" didn't hit the factory with ShootyTwo");
    testPassed &= validate(cappy .getCaptureProgress() > 0     , "    "+ai.getName()+" didn't restart capturing with cappy");
    testPassed &= validate(5 == cappy.x && 1 == cappy.y        , "    "+ai.getName()+" didn't go to the factory with cappy");
    testPassed &= validate(scab  .getCaptureProgress() > 0     , "    "+ai.getName()+" didn't start capturing with the scab");

    // Clean up
    cleanupTest();

    return testPassed;
  }

  /**
   * The AI needs to block with the inf, and support the inf with the artillery
   */
  private boolean testWalling(AIMaker ai)
  {
    setupTest(MapReader.readSingleMap("src/Test/TestWalling.map"), ai,
              CommandingOfficers.AW1.YC.Kanbei.getInfo(),
              CommandingOfficers.AW4.BrennerWolves.Will.getInfo());

    Unit arty   = testMap.getResident(1, 1);
    Unit blocky = testMap.getResident(1, 2);
    Unit tanky  = testMap.getResident(5, 1);
    turn(testGame);

    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.army.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    var artyXyc   = new XYCoord(arty);
    var blockyXyc = new XYCoord(blocky);
    var tankyXyc  = new XYCoord(tanky);
    var tankZone  = AICombatUtils.findThreatPower(testMap, tanky, tankyXyc, arty.model).keySet();
    testPassed &= validate(2 >= artyXyc.getDistance(blockyXyc), "    "+ai.getName()+" didn't protect blocky with arty");
    testPassed &= validate(!tankZone.contains(artyXyc),         "    "+ai.getName()+" didn't protect arty with blocky");

    // Clean up
    cleanupTest();

    return testPassed;
  }
}
