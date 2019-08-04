package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import CommandingOfficers.Modifiers.UnitRemodelModifier;
import Engine.GameScenario;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

public class TestCOModifier extends TestCase
{
  private static Commander strong = null;
  private static Commander patch = null;
  private static MapMaster testMap = null;

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testDamageModifier(), "  Damage Modifier test failed!");
    testPassed &= validate(testMovementModifier(), "  Movement modifier test failed!");
    testPassed &= validate(testProductionModifier(), "  Production modifier test failed!");
    testPassed &= validate(testUnitRemodelModifier(), "  Unit Remodel modifier test failed!");
    
    return testPassed;
  }

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    strong = new Strong(scn.rules);
    patch = new Patch(scn.rules);
    Commander[] cos = { strong, patch };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));
  }

  private boolean testDamageModifier()
  {
    boolean testPassed = true;
    UnitModel inf = patch.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    
    // Get base damage ratio.
    int startDmg = inf.getDamageRatio();

    // Apply a damage modifier and make sure we saw an increase.
    CODamageModifier dmgMod = new CODamageModifier(50);
    dmgMod.applyChanges(patch);
    int newDmg = inf.getDamageRatio();
    testPassed &= validate(startDmg < newDmg, "    Damage modifier did not increase damage ratio!");
    
    // Make sure reverting takes it back to normal.
    dmgMod.revertChanges(patch);
    int lastDmg = inf.getDamageRatio();
    testPassed &= validate(startDmg == lastDmg, "    Damage modifier did not return to normal");
    
    return testPassed;
  }

  private boolean testMovementModifier()
  {
    boolean testPassed = true;
    UnitModel inf = patch.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    
    // Get base movement speed
    int startMove = inf.movePower;
    
    // Apply a movement modifier and re-check.
    int MOVEMOD = 3;
    COMovementModifier moveMod = new COMovementModifier(MOVEMOD);
    moveMod.addApplicableUnitModel(patch.getUnitModel(UnitModel.UnitEnum.INFANTRY));
    moveMod.applyChanges(patch);
    int newMove = inf.movePower;
    testPassed &= validate( (newMove - startMove) == MOVEMOD, "    Movement modifier did not apply as expected!");

    // Make sure reverting takes it back to normal.
    moveMod.revertChanges(patch);
    int lastMove = inf.movePower;
    testPassed &= validate( lastMove == startMove, "    Movement modifier did not return the move power to normal!");

    return testPassed;
  }

  private boolean testProductionModifier()
  {
    boolean testPassed = true;
    // Make a shallow copy and a deep copy of what Patch can build from a Factory.
    TerrainType FC = TerrainType.FACTORY;
    ArrayList<UnitModel> factoryModels = patch.unitProductionByTerrain.get(FC);
    
    // Define a type we want to add, and verify it isn't already buildable.
    UnitModel bship = patch.getUnitModel(UnitModel.UnitEnum.BATTLESHIP);
    testPassed &= validate( !factoryModels.contains(bship), "    Factory can build Battleship by default. Malformed test!" );
    
    // Define a type we will try to add, that is buildable already.
    UnitModel inf = patch.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    testPassed &= validate( factoryModels.contains(inf), "    Factory cannot build infantry by default. Malformed test!" );
    
    // Try to add both types.
    UnitProductionModifier upMod = new UnitProductionModifier(FC, bship);
    upMod.addProductionPair(FC, inf);
    upMod.applyChanges(patch);
    
    // Verify that Patch can now build Battleships from a factory.
    testPassed &= validate( factoryModels.contains(bship), "    Factory cannot build Battleships, though it should be able to now.");
    
    // Revert the mod and ensure we can no longer build battleships.
    upMod.revertChanges(patch);
    testPassed &= validate( !factoryModels.contains(bship), "    Factory can still build Battleships, though it should not.");
    
    // Make sure it didn't also remove the ability to construct Infantry.
    testPassed &= validate( factoryModels.contains(inf), "    Factory can no longer build Infantry, though it should.");
    
    return testPassed;
  }

  private boolean testUnitRemodelModifier()
  {
    boolean testPassed = true;

    addUnit(testMap, patch, UnitModel.UnitEnum.INFANTRY, 2, 2);
    addUnit(testMap, patch, UnitModel.UnitEnum.RECON, 2, 3);
    Unit infantry = testMap.getLocation(2, 2).getResident();
    Unit recon = testMap.getLocation(2, 3).getResident();

    testPassed &= validate( infantry != null, "    Infantry is missing. Malformed test!");
    testPassed &= validate( recon != null, "    Recon is missing. Malformed test!");

    UnitModel infModel = patch.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    UnitModel reconModel = patch.getUnitModel(UnitModel.UnitEnum.RECON);

    testPassed &= validate( infantry.model == infModel, "    Infantry is not Infantry. Malformed test!");
    testPassed &= validate( recon.model == reconModel, "    Recon is not Recon. Malformed test!");

    // Turn our hapless infantryman into a lean, mean, driving machine. Make sure the Recon is still a Recon also.
    UnitRemodelModifier remod = new UnitRemodelModifier(infModel.type, reconModel.type);
    remod.applyChanges(patch);
    testPassed &= validate( infantry.model == reconModel, "    Infantry is not Recon after being turned into one.");
    testPassed &= validate( recon.model == reconModel, "    Recon is not Recon, but it still should be.");
    for( int i = 0; i < infantry.weapons.size(); ++i )
    {
      testPassed &= validate( infantry.weapons.get(i).model == recon.weapons.get(i).model, "    Infantry weapons are not Recon weapons, though he is recon." );
    }

    // OK, that was weird. Change him back. Please. Make sure nothing weird happened to the Recon in the process.
    remod.revertChanges(patch);
    testPassed &= validate( infantry.model == infModel, "    Infantry is not Infantry after being changed back.");
    testPassed &= validate( recon.model == reconModel, "    Recon is not Recon, though it should not have changed.");

    // Another test! We must make sure that two units, after being transmogrified into the same thing, will return to their correct forms.
    UnitModel mechModel = patch.getUnitModel(UnitModel.UnitEnum.MECH);
    remod = new UnitRemodelModifier(infModel.type, mechModel.type);
    remod.addUnitRemodel(reconModel.type, mechModel.type);
    remod.applyChanges(patch);
    testPassed &= validate( infantry.model == mechModel, "    Infantry is not Mech after being changed.");
    testPassed &= validate( recon.model == mechModel, "    Recon is not Mech after being changed.");

    remod.revertChanges(patch);
    testPassed &= validate( infantry.model == infModel, "    Infantry is not Infantry after being unMeched.");
    testPassed &= validate( recon.model == reconModel, "    Recon is not Recon after being unMeched.");


    return testPassed;
  }
}
