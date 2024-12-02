package Test;

import java.util.ArrayList;

import AI.*;
import CommandingOfficers.*;
import Engine.*;
import Engine.GameScenario.FogMode;
import Engine.GameScenario.TagMode;
import Terrain.*;
import Terrain.Maps.MapReader;
import Terrain.Maps.TestRange;
import Units.AWBWUnits;
import Units.Unit;

public class TestAIConstraints extends TestCase
{
  private static MapMaster testMap;
  private static GameInstance testGame;

  private void setupTest(MapInfo mapInfo, AIMaker ai, GameScenario scn)
  {
    Army[] armies = new Army[mapInfo.getNumPlayers()];
    for( int i = 0; i < armies.length; ++i )
      armies[i] = new Army(scn, CommanderLibrary.NotACO.getInfo().create(scn.rules));

    AIController testAI = ai.create(armies[0]);
    testAI.setLogging(false);
    armies[0].setAIController(testAI);

    testMap = new MapMaster(armies, mapInfo);
    testGame = new GameInstance(armies, testMap);

    turn(testGame);
  }

  private void cleanupTest()
  {
    testMap = null;
    testGame = null;
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = true;

    final ArrayList<AIMaker> aiList = new ArrayList<>(AILibrary.getAIList());
    aiList.remove(0); // Humans are not AI
    for( AIMaker ai : aiList )
    {
      testPassed &= validate(testUnmovedFriend(ai), "  "+ai.getName()+" moves units it doesn't own.");
      testPassed &= validate(testBuildTooManyMans(ai), "  "+ai.getName()+" tries to build units while at the cap.");
    }

    return testPassed;
  }

  /** Confirm AIs don't move their friends' units. */
  private boolean testUnmovedFriend(AIMaker ai)
  {
    setupTest(MapReader.readSingleMap("src/Test/TestUnmovedFriend.map"), ai, new GameScenario());
    final Army armyOne = testGame.armies[0];

    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = armyOne.getNextAIAction(testMap);
      if( null != act )
      {
        final Unit actor = act.getActor();
        if ( actor != null )
          testPassed &= validate(actor.CO.army == armyOne, "    "+ai.getName()+" ordered non-owned unit "+actor.toStringWithLocation()+"!");

        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
      }
    } while( null != act && testPassed );

    // Clean up
    cleanupTest();

    return testPassed;
  }

  private boolean testBuildTooManyMans(AIMaker ai)
  {
    final int unitCap = 1;
    setupTest(TestRange.getMapInfo(), ai, new GameScenario(new AWBWUnits(), 1000, 1000, unitCap, FogMode.OFF_DOR, TagMode.OFF));
    final Army armyOne = testGame.armies[0];
    armyOne.team = 9;
    testGame.armies[1].team = armyOne.team; // In case we want to test caps > 1; you build up unit count faster when there's no fighting.
    // Run through a round of init-turns so that our allies are ready-to-act
    day(testGame);

    GameAction act = null;
    boolean testPassed = true;
    do
    {
      act = armyOne.getNextAIAction(testMap);
      if( null != act )
        testPassed &= validate(performGameAction(act, testGame), "    "+ai.getName()+" generated a bad action!");
    } while( null != act && testPassed );

    testPassed &= validate(armyOne.cos[0].units.size() == unitCap, "    "+ai.getName()+" built over the unit cap!");

    // Clean up
    cleanupTest();

    return testPassed;
  }
}
