package Test;

import CommandingOfficers.Commander;
import CommandingOfficers.DefendPeace.GreySky.Cinder;
import CommandingOfficers.DefendPeace.misc.Venge;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.StateTrackers.DamageDealtToIncomeConverter;
import Terrain.MapLibrary;
import Terrain.MapMaster;

public class TestStateTracker extends TestCase
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
    testPassed &= validate(testInitialization(), "  Initialization test failed.");

    return testPassed;
  }

  private boolean testInitialization()
  {
    boolean testPassed = true; // Assume nothin's busted
    int currentTrackers = testGame.stateTrackers.keySet().size();
    // Venge has no trackers, and Cinder has two: WitchFireTracker and BuildCountsTracker
    testPassed &= validate(currentTrackers == 2, "    Expected 2 trackers, but the game has "+currentTrackers+" instead.");

    Object og = DamageDealtToIncomeConverter.instance(testGame, DamageDealtToIncomeConverter.class);
    for( int i = 0; i < 10; ++i )
    {
      Object thing2 = DamageDealtToIncomeConverter.instance(testGame, DamageDealtToIncomeConverter.class);
      currentTrackers = testGame.stateTrackers.keySet().size();
      testPassed &= validate(currentTrackers == 3, "    Expected 3 trackers, but the game has "+currentTrackers+" instead.");
      testPassed &= validate(thing2 == og, "    New tracker != original tracker.");
    }

    return testPassed;
  }

}
