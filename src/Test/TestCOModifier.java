package Test;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderPatch;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import Terrain.TerrainType;
import Units.UnitModel;

public class TestCOModifier extends TestCase
{
  private static Commander patch;

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testDamageModifier(), "  Damage Modifier test failed!");
    testPassed &= validate(testMovementModifier(), "  Movement modifier test failed!");
    testPassed &= validate(testProductionModifier(), "  Production modifier test failed!");
    
    return testPassed;
  }

  /** Make two COs and a GameMap to use with this test case. */
  private void setupTest()
  {
    patch = new CommanderPatch();
  }

  private boolean testDamageModifier()
  {
    boolean testPassed = true;
    UnitModel inf = patch.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    
    // Get base damage ratio.
    int startDmg = inf.getDamageRatio();

    // Apply a damage modifier and make sure we saw an increase.
    CODamageModifier dmgMod = new CODamageModifier(50);
    dmgMod.apply(patch);
    int newDmg = inf.getDamageRatio();
    testPassed &= validate(startDmg < newDmg, "    Damage modifier did not increase damage ratio!");
    
    // Make sure reverting takes it back to normal.
    dmgMod.revert(patch);
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
    moveMod.addApplicableUnitType(UnitModel.UnitEnum.INFANTRY);
    moveMod.apply(patch);
    int newMove = inf.movePower;
    testPassed &= validate( (newMove - startMove) == MOVEMOD, "    Movement modifier did not apply as expected!");

    // Make sure reverting takes it back to normal.
    moveMod.revert(patch);
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
    upMod.apply(patch);
    
    // Verify that Patch can now build Battleships from a factory.
    testPassed &= validate( factoryModels.contains(bship), "    Factory cannot build Battleships, though it should be able to now.");
    
    // Revert the mod and ensure we can no longer build battleships.
    upMod.revert(patch);
    testPassed &= validate( !factoryModels.contains(bship), "    Factory can still build Battleships, though it should not.");
    
    // Make sure it didn't also remove the ability to construct Infantry.
    testPassed &= validate( factoryModels.contains(inf), "    Factory can no longer build Infantry, though it should.");
    
    return testPassed;
  }
}
