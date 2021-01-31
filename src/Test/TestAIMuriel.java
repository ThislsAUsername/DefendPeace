package Test;

import java.util.function.Function;

import AI.AIController;
import AI.AIMaker;
import AI.Muriel;
import AI.WallyAI;
import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Maps.MapReader;
import Units.Unit;

public class TestAIMuriel extends TestCase
{
  private static Commander testCo1;
  private static Commander testCo2;
  private static AIMaker[] ais = { Muriel.info, WallyAI.info };
  private static MapMaster testMap;
  private static GameInstance testGame;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest(AIMaker ai)
  {
    setupTest(MapLibrary.getByName("Firing Range"), ai);
  }

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest(MapInfo mapInfo, AIMaker ai)
  {
    GameScenario scn = new GameScenario();
    testCo1 = new Strong(scn.rules);
    testCo2 = new Patch(scn.rules);
    AIController testAI = ai.create(testCo1);
    testAI.setLogging(false);
    testCo1.setAIController(testAI);
    Commander[] cos = { testCo1, testCo2 };

    testMap = new MapMaster(cos, mapInfo);
    testGame = new GameInstance(testMap);
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

    for( AIMaker ai : ais )
    {
      testPassed &= validate(testBuildMegatank(ai), "  "+ai.getName()+" failed build Megatank test.");
      testPassed &= validate(testHuntStall(ai), "  "+ai.getName()+" failed hunting stall-test.");
      testPassed &= validate(testClearAttackRoute(ai), "  "+ai.getName()+" failed route clearing test.");
      testPassed &= validate(testWalkInLine(ai), "  "+ai.getName()+" failed line walking test.");
      testPassed &= validate(testInfWadeThroughTanks(ai), "  "+ai.getName()+" failed Infantry move priority test.");
    }
    testPassed &= validate(testTankWadeThroughInfs(Muriel.info), "  Tank move priority test failed.");
    // TODO: Consider making Wally pass the above
    testPassed &= validate(testProductionClearing(WallyAI.info), "  Free up industry test failed.");

    return testPassed;
  }

  /** Confirm that the AI will build the correct counter, even when there is only one possible counter. */
  private boolean testBuildMegatank(AIMaker ai)
  {
    setupTest(ai);

    // Add an enemy to counter.
    addUnit(testMap, testCo2, "Megatank", 1, 1);

    // Give Muriel resources.
    testCo1.money = 80000;

    // Ask Muriel what to do.
    testCo1.initTurn(testMap);
    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    // Muriel should have built a Megatank as the best/only viable unit to counter an enemy Megatank.
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

  /** Confirm that Muriel will build the correct counter, even when there is only one possible counter. */
  private boolean testHuntStall(AIMaker ai)
  {
    setupTest(ai);

    // Where are things?
    XYCoord tankStart = new XYCoord(2, 1);
    XYCoord facPos = new XYCoord(7, 1);

    // Add some units, and grant us a factory so we can avoid blocking it.
    Unit myTank = addUnit(testMap, testCo1, "Md Tank", tankStart.xCoord, tankStart.yCoord);
    addUnit(testMap, testCo2, "Recon", 10, 1);
    testMap.getLocation(facPos).setOwner(testCo1);

    // Verify that we own the factory in question.
    boolean testPassed = validate(testCo1.ownedProperties.contains(facPos), "    Failed to assign factory.");

    // Ask Muriel what to do.
    testCo1.initTurn(testMap);
    GameAction act = null;
    do
    {
      act = testCo1.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    // Muriel should have moved the Md Tank towards the enemy Recon, but not ended top of the factory.
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

    // Give Muriel some properties so they are out of the way.
    testMap.getLocation(2, 5).setOwner(testCo1);
    testMap.getLocation(4, 4).setOwner(testCo1);
    testMap.getLocation(4, 8).setOwner(testCo1);
    testMap.getLocation(6, 1).setOwner(testCo1);
    testMap.getLocation(7, 1).setOwner(testCo1);
    testMap.getLocation(8, 8).setOwner(testCo1);
    testMap.getLocation(12, 7).setOwner(testCo1);
    testMap.getLocation(10, 5).setOwner(testCo1);

    // Where are things?
    XYCoord facPos = new XYCoord(7, 8);

    // Add an enemy copter on a neutral fac, flanked by friendly infs, with a friendly AA nearby.
    Unit nmeCopter = addUnit(testMap, testCo2, "B-Copter", facPos);
    Unit myAA = addUnit(testMap, testCo1, "Anti-Air", facPos.up().left());
    Unit iLeft = addUnit(testMap, testCo1, "Infantry", facPos.left());
    Unit iUp = addUnit(testMap, testCo1, "Infantry", facPos.up());
    Unit iRight = addUnit(testMap, testCo1, "Infantry", facPos.right());

    // The infs all want to cap the fac, but can't because it is occupied. Gotta let the AA through.
    // Fetch and execute Muriel actions until she stops creating them.
    testCo1.initTurn(testMap);
    testCo1.money = 0; // No production needed for this test.

    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    testPassed &= validate(nmeCopter.getHP() < 10, "    "+ai.getName()+" failed to attack enemy copter!");
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
    testCo1.initTurn(testMap);
    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.getNextAIAction(testMap);
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

    // The inf will want to cap the enemy HQ. Make sure the tanks get out of the way.
    boolean testPassed = true;
    int turnLimit = 4; // This should be enough time to cap the enemy HQ.
    for( int tt = 0; tt < turnLimit; ++tt )
    {
      testCo1.initTurn(testMap);
      GameAction act = null;
      do
      {
        act = testCo1.getNextAIAction(testMap);
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

    // The inf will want to cap the enemy HQ. Make sure the tanks get out of the way.
    boolean testPassed = true;
    int turnLimit = 4; // This should be enough time to cap the enemy HQ.
    for( int tt = 0; tt < turnLimit; ++tt )
    {
      testCo1.initTurn(testMap);
      GameAction act = null;
      do
      {
        act = testCo1.getNextAIAction(testMap);
        if( null != act )
          testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
      } while( null != act && testPassed );
    }

    testPassed &= validate(nmeArty.getHP() <= 0, "    "+ai.getName()+" failed to kill the artillery!");
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
    testCo1.money = 80000;

    testCo1.initTurn(testMap);
    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = testCo1.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    // Muriel should have built a Megatank as the best/only viable unit to counter an enemy Megatank.
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
}
