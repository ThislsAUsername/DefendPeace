package Test;

import java.io.File;

import CommandingOfficers.Commander;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameInstance;
import Engine.GameScenario;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class TestSaveLoad extends TestCase
{
  private static Commander strong = null;
  private static Commander patch = null;
  private static MapMaster testMap = null;
  private static GameInstance game = null;

  /** Make two COs and a MapMaster to use with this test case. */
  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    strong = new Strong(scn.rules);
    patch = new Patch(scn.rules);
    Commander[] cos = { strong, patch };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));

    game = new GameInstance(testMap);
    game.saveFile = "fancyTestSave.svp";
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(testSaveLoad(), "  Save/load test failed!");
    
    return testPassed;
  }

  private boolean testSaveLoad()
  {
    Unit fool = addUnit(testMap, strong, UnitModel.TRANSPORT, 7, 2); fool.initTurn(testMap);
    Unit scout = addUnit(testMap, strong, UnitModel.RECON, 7, 3); scout.initTurn(testMap);
    Unit punch = addUnit(testMap, strong, UnitModel.SIEGE, 4, 5); punch.initTurn(testMap);
    Unit resupplyable = addUnit(testMap, strong, UnitModel.TRANSPORT, 8, 8); resupplyable.initTurn(testMap);
    resupplyable.fuel = 0;
    
    Unit bait = addUnit(testMap, patch, UnitModel.TRANSPORT, 6, 5);
    Unit meaty = addUnit(testMap, patch, UnitModel.ASSAULT, 7, 5);
    
    // It's Strong's turn. Set up his fog goggles.
    strong.initTurn(testMap);
    boolean testPassed = true;

    String path = game.writeSave();
    File file = new File(path);
    testPassed &= validate(file.exists(),  "    The file we just saved to doesn't exist");
    testPassed &= validate(GameInstance.isSaveCompatible(path),  "    We are incompatible with the save we just made");
    GameInstance loaded = GameInstance.loadSave(path);
    testPassed &= validate(null != loaded,  "    The save didn't actually load");
    file.delete();
    
    // Clean up
    testMap.removeUnit(fool);
    testMap.removeUnit(scout);
    testMap.removeUnit(punch);
    testMap.removeUnit(resupplyable);
    
    testMap.removeUnit(bait);
    testMap.removeUnit(meaty);

    return testPassed;
  }
}
