package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.Patch;
import CommandingOfficers.AW4.RuinedCommander;
import CommandingOfficers.AW4.RuinedCommander.UseAbilityFactory;
import CommandingOfficers.AW4.BrennerWolves.BrennerDoR;
import Engine.Army;
import Engine.GameActionSet;
import Engine.GameInstance;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitActionLifecycles.LoadLifecycle;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class TestCommanderBrenner extends TestCase
{
  private Commander Patch;
  private BrennerDoR Brenner;
  private MapMaster testMap;
  private GameInstance game;

  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    Brenner = new BrennerDoR(scn.rules);
    Patch = new Patch(scn.rules);
    Army[] cos = { new Army(scn, Brenner), new Army(scn, Patch) };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));

    game = new GameInstance(cos, testMap);
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = true;

    testPassed &= validate(testCOU(), "  COU test failed!");
    testPassed &= validate(testAttackCharge(), "  Attack charge test failed!");
    testPassed &= validate(testAbility(), "  Ability test failed!");

    game.endGame();
    return testPassed;
  }

  /** Utility method that checks whether there are any valid actions of this type for this unit in place */
  public boolean canAct(Unit unitToTry, UnitActionFactory actionType)
  {
    return null != actionType.getPossibleActions(testMap, GamePath.stayPut(unitToTry), unitToTry);
  }
  private boolean testCOU()
  {
    setupTest();

    Unit bopterOnFactory = addUnit(testMap, Brenner, UnitModel.AIR_TO_SURFACE, 1, 7);
    Unit bopterOnHQ      = addUnit(testMap, Brenner, UnitModel.AIR_TO_SURFACE, 1, 8);
    Unit infOnFactory    = addUnit(testMap, Brenner, UnitModel.LAND,           2, 7);
    Unit infOnGrass      = addUnit(testMap, Brenner, UnitModel.LAND,           2, 8);
    Unit infOnUnowned    = addUnit(testMap, Brenner, UnitModel.LAND,           7, 8);

    Brenner.initTurn(testMap);
    Brenner.army.money = 9001;
    boolean testPassed = true;
    DeployableCommander.DeployCOUFactory couer = Brenner.deployAction;

    testPassed &= validate(Brenner.COUs.size() == 0, "    Brenner has a COU before doing anything");
    testPassed &= validate(!canAct(bopterOnFactory, couer), "    Brenner can set his COU on an air unit on a factory.");
    testPassed &= validate( canAct(bopterOnHQ     , couer), "    Brenner can't set his COU on an HQ.");
    testPassed &= validate( canAct(infOnFactory   , couer), "    Brenner can't set his COU on a factory he owns.");
    testPassed &= validate(!canAct(infOnGrass     , couer), "    Brenner can set his COU on grass he doesn't own.");
    testPassed &= validate(!canAct(infOnUnowned   , couer), "    Brenner can set his COU on a factory he doesn't own.");

    performGameAction(new DeployableCommander.DeployCOUAction(Brenner.deployAction, infOnFactory), game);
    testPassed &= validate(!infOnFactory.isTurnOver, "    COUing cost the COU's turn.");
    testPassed &= validate(Brenner.COUs.contains(infOnFactory), "    COUing didn't COU the COU.");
    testPassed &= validate(9001 - Brenner.army.money == infOnFactory.getCost()/2, "    COUing didn't cost the expected amount.");
    testPassed &= validate(!canAct(bopterOnHQ     , couer), "    Brenner can double-COU.");
    testPassed &= validate(!canAct(infOnFactory   , couer), "    Brenner can COU his COU.");

    return testPassed;
  }

  /**
   * Verifies that the zone is the only place Brenner gets energy.<p>
   * The charge bug (combat initiator must be in your zone, not your unit) is not checked here.
   */
  private boolean testAttackCharge()
  {
    setupTest();

    // Add our combatants
    Unit mech = addUnit(testMap, Brenner, UnitModel.MECH, 1, 1); // 4 tiles from COU, so not in zone
    Unit COU  = addUnit(testMap, Brenner, UnitModel.TRANSPORT, 4, 2);
    Unit aa   = addUnit(testMap, Brenner, UnitModel.SURFACE_TO_AIR, 2, 2); // 2 tiles from COU, so in zone

    // And our victim
    Unit inf = addUnit(testMap, Patch, UnitModel.TROOP, 1, 2);

    Brenner.initTurn(testMap);
    Brenner.army.money = 9001;
    boolean testPassed = true;

    // Note: COUing on this tile is not normally valid, but we're skipping the logic that enforces that.
    performGameAction(new DeployableCommander.DeployCOUAction(Brenner.deployAction, COU), game);
    testPassed &= validate(Brenner.COUs.contains(COU), "    COUing didn't COU the COU?");
    testPassed &= validate(9001 - Brenner.army.money == COU.getCost()/2, "    COUing didn't cost the expected amount.");

    performGameAction(new BattleLifecycle.BattleAction(testMap, mech, GamePath.stayPut(mech), 1, 2), game);

    testPassed &= validate(mech.health < UnitModel.MAXIMUM_HEALTH, "    Mech took no counter.");
    testPassed &= validate(Brenner.getAbilityPower() == 0, "    Brenner got charge with no zone involvement.");

    int expectedAttackerCharge = inf.getHP();
    performGameAction(new BattleLifecycle.BattleAction(testMap, aa, GamePath.stayPut(aa), 1, 2), game);
    testPassed &= validate(Brenner.getAbilityPower() == expectedAttackerCharge, "    Brenner got the wrong amount of charge.");
    testPassed &= validate(testMap.getLocation(1, 2).getResident() == null, "    Defender is still on the map.");

    return testPassed;
  }

  private boolean testAbility()
  {
    setupTest();

    boolean testPassed = true;

    Unit mech = addUnit(testMap, Brenner, UnitModel.MECH, 3, 2);
    Unit COU  = addUnit(testMap, Brenner, UnitModel.TRANSPORT, 4, 2);
    mech.damageHealth(20);
    COU .damageHealth(20);
    UseAbilityFactory abilityer = new RuinedCommander.UseAbilityFactory(Brenner.myAbilities.get(0));

    Brenner.initTurn(testMap);
    performGameAction(new LoadLifecycle.LoadAction(testMap, mech, Utils.findShortestPath(mech, new XYCoord(COU), testMap)), game);

    ArrayList<GameActionSet> unchargedActions = COU.getPossibleActions(testMap, GamePath.stayPut(COU));
    Brenner.modifyAbilityStars(20);
    testPassed &= validate(unchargedActions.size() == COU.getPossibleActions(testMap, GamePath.stayPut(COU)).size(),
        "    Brenner can COP with a non-COU.");

    performGameAction(new DeployableCommander.DeployCOUAction(Brenner.deployAction, COU), game);
    testPassed &= validate( canAct(COU, abilityer), "    Brenner can't COP.");
    Brenner.modifyAbilityStars(-20);
    testPassed &= validate(unchargedActions.size() == COU.getPossibleActions(testMap, GamePath.stayPut(COU)).size(),
        "    Brenner can COP with no charge.");
    Brenner.modifyAbilityStars(20);

    // Activate his power
    performGameAction(abilityer.getPossibleActions(testMap, GamePath.stayPut(COU), COU).getSelected(), game);

    // Check that the ability did what it was supposed to.
    testPassed &= validate( COU .getHealth() == UnitModel.MAXIMUM_HEALTH, "    Brenner failed to heal himself.");
    testPassed &= validate( mech.getHealth() == UnitModel.MAXIMUM_HEALTH, "    Brenner failed to heal a loaded unit.");

    return testPassed;
  }
}
