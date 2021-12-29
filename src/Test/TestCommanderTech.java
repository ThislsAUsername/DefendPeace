package Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import CommandingOfficers.Tech;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.Patch;
import Engine.Army;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapLibrary;
import Terrain.MapMaster;

public class TestCommanderTech extends TestCase
{
  private Commander mPatch;
  private Tech mTech;
  private MapMaster mTestMap;

  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    Tech.getInfo().injectUnits(scn.rules.unitModelScheme.getGameReadyModels());
    mTech = new Tech(scn.rules);
    mTech.modifyAbilityPower(20);
    mPatch = new Patch(scn.rules);
    Army[] cos = { new Army(mTech), new Army(mPatch) };

    mTestMap = new MapMaster(cos, MapLibrary.getByName("Test Range"));
  }

  private void cleanupTest()
  {
    mTech = null;
    mPatch = null;
    mTestMap = null;
  }

  private CommanderAbility findAbilityByName(Commander co, String name)
  {
    CommanderAbility pow = null;

    ArrayList<CommanderAbility> abilities = mTech.getReadyAbilities();
    for( CommanderAbility ca : abilities )
    {
      if( ca.toString().equalsIgnoreCase(name) )
      {
        pow = ca;
        break;
      }
    }

    return pow;
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = true;

    testPassed &= validate(testTechDropRandomization(), "  Tech Drops are random!");

    return testPassed;
  }

  private boolean testTechDropRandomization()
  {
    boolean testPassed = true;

    setupTest();

    // Steel Hail does three TechDrops, so we'll use it to drive our test.
    CommanderAbility tech_drop = findAbilityByName(mTech, "Steel Hail");

    testPassed &= validate(null != tech_drop, "Failed to find Steel Hail ability!");

    // Record the predicted and actual destinations.
    Collection<DamagePopup> p_locs = tech_drop.getDamagePopups(mTestMap);
    GameEventQueue r_locs = tech_drop.getEvents(mTestMap);

    testPassed &= validate(p_locs.size() == 3, "Tech Drop predicted " + r_locs.size() + " events instead of 3!");
    testPassed &= validate(r_locs.size() == 3, "Tech Drop generated " + r_locs.size() + " events instead of 3!");

    // Pull out the predicted/actual coordinates and sort them.
    List<XYCoord> predLocs = p_locs.stream().map((dp) -> dp.coords).collect(Collectors.toList());
    List<XYCoord> realLocs = r_locs.stream().map((ge) -> ge.getEndPoint()).collect(Collectors.toList());
    predLocs.sort((xy1, xy2) -> xy1.xCoord - xy2.xCoord);
    realLocs.sort((xy1, xy2) -> xy1.xCoord - xy2.xCoord);

    // Make some convenient shorthand names
    XYCoord p_xy0 = predLocs.get(0);
    XYCoord p_xy1 = predLocs.get(1);
    XYCoord p_xy2 = predLocs.get(2);

    XYCoord r_xy0 = realLocs.get(0);
    XYCoord r_xy1 = realLocs.get(1);
    XYCoord r_xy2 = realLocs.get(2);

    // Make sure we don't drop to the same place twice.
    testPassed &= validate(!r_xy0.equals(r_xy1), "Drop locations 0 and 1 match!");
    testPassed &= validate(!r_xy0.equals(r_xy2), "Drop locations 0 and 2 match!");
    testPassed &= validate(!r_xy1.equals(r_xy2), "Drop locations 1 and 2 match!");

    // Make sure the predictions match reality.
    testPassed &= validate(r_xy0.equals(p_xy0), "Drop point 0 changed from " + p_xy0 + " to " + r_xy0 + "!");
    testPassed &= validate(r_xy1.equals(p_xy1), "Drop point 1 changed from " + p_xy1 + " to " + r_xy1 + "!");
    testPassed &= validate(r_xy2.equals(p_xy2), "Drop point 2 changed from " + p_xy2 + " to " + r_xy2 + "!");

    cleanupTest();
    return testPassed;
  }
}
