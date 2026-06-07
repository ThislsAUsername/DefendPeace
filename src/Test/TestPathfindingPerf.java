package Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

import CommandingOfficers.*;
import Engine.*;
import Terrain.*;
import Units.UnitModelScheme.GameReadyModels;
import Units.MoveTypes.MoveType;
import lombok.var;

public class TestPathfindingPerf extends TestCase
{
  private MapMaster testMap;
  private GameInstance testGame;

  private void setupTest(MapInfo mapInfo)
  {
    var scn = new GameScenario();
    Army[] armies = new Army[mapInfo.getNumPlayers()];
    for( int i = 0; i < armies.length; ++i )
      armies[i] = new Army(scn, CommanderLibrary.NotACO.getInfo().create(scn.rules));

    testMap  = new MapMaster(armies, mapInfo);
    testGame = new GameInstance(armies, testMap);

    turn(testGame);
  }

  @Override
  public boolean runTest()
  {
    boolean testPassed = true;
    if( testPassed )
      return testPassed;

    final ArrayList<MapInfo> mapList = MapLibrary.getMapList();
    // Warm up the codepath.
    setupTest(mapList.get(0));

    DecimalFormat df = new DecimalFormat("#.##");
    double ns2s = 1./1000000000;
    var calcTimes = new ArrayList<Double>();
    double calcTotal = 0.0;
    for( var mi : mapList )
    {
      if( mi.terrain.length > 100 )
        continue; // Europe Map is a tad unreasonable for this, taking in excess of 100 seconds.
      setupTest(mi);
      long calcStartNanos = System.nanoTime();

      var ums = testGame.rules.unitModelScheme;
      GameReadyModels grms = ums.getGameReadyModels();
      var uniqueMoveTypes = new HashSet<MoveType>();
      for( var um : grms.unitModels )
        uniqueMoveTypes.add(um.baseMoveType);
      for( var mt : uniqueMoveTypes )
      {
        for( int x = 0; x < testMap.mapWidth; x++ )
        {
          for( int y = 0; y < testMap.mapHeight; y++ )
          {
            XYCoord xyc        = new XYCoord(x, y);
            PathCalcParams pcp = new PathCalcParams(mt, 6, xyc, testMap);
            pcp.setTheoretical();
            pcp.findAllPaths();
          } // ~y
        } // ~x
      }

      long calcEndNanos = System.nanoTime();
      double calcDeltaSec = (calcEndNanos - calcStartNanos) * ns2s;
      calcTotal += calcDeltaSec;
      calcTimes.add(calcDeltaSec);
      System.out.println(mi.mapName + ": " + df.format(calcDeltaSec) + "s, " + df.format(calcTotal) + "s so far");
    }
    System.out.println("Took " + df.format(calcTotal) + "s total, " + df.format(calcTotal / mapList.size()) + "s average");

    return testPassed;
  }
}
