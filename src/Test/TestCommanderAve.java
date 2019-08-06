package Test;

import java.util.ArrayList;

import CommandingOfficers.Ave;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.Patch;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel.UnitEnum;

public class TestCommanderAve extends TestCase
{
  private Commander Patch;
  private Ave Ave;
  private MapMaster testMap;
  private GameInstance game;

  private void setupTest()
  {
    GameScenario scn = new GameScenario();
    Ave = new Ave(scn.rules);
    Patch = new Patch(scn.rules);
    Commander[] cos = { Ave, Patch };

    testMap = new MapMaster(cos, MapLibrary.getByName("Firing Range"));

    for( Commander co : cos )
    {
      co.myView = new MapWindow(testMap, co);
    }
    game = new GameInstance(testMap);
  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;

    testPassed &= validate(testSnowSpread(), "  Snow spread test failed!");
    testPassed &= validate(testCapture(), "  Capture test failed!");
    testPassed &= validate(testGlacio(), "  Glacio test failed!");

    game.endGame();
    return testPassed;
  }

  boolean testCapture()
  {
    boolean testPassed = true;
    XYCoord city = new XYCoord(10, 1);

    testPassed &= validate(testMap.getEnvironment(city).terrainType == TerrainType.CITY, "    City location is not a city!");
    testPassed &= validate(testMap.getEnvironment(city).weatherType == Weathers.CLEAR, "    Weather is not clear at start!");

    Unit infantry = addUnit(testMap, Ave, Units.UnitModel.UnitEnum.INFANTRY, city.xCoord, city.yCoord);
    GameAction capture = new GameAction.CaptureAction(testMap, infantry, Utils.findShortestPath(infantry, city, testMap));

    // Give it the ol' one-two.
    infantry.initTurn(testMap);
    performGameAction(capture, testMap);
    infantry.initTurn(testMap);
    performGameAction(capture, testMap);

    testPassed &= validate(Ave.getSnowMapClone()[city.xCoord][city.yCoord] == CommandingOfficers.Ave.SNOW_THRESHOLD, "    Ave doesn't have 1 snow in city after capture");

    return testPassed;
  }

  boolean testSnowSpread()
  {
    boolean testPassed = true;
    GameEventQueue events = Ave.initTurn(testMap);
    for(GameEvent event: events) event.performEvent(testMap);
    
    // Verify that after initializing the first time, Ave's properties are all snow-bound.
    for( XYCoord prop : Ave.ownedProperties )
    {
      testPassed &= validate(testMap.isLocationValid(prop), "    Location is invalid!");
      testPassed &= validate(testMap.getLocation(prop).getEnvironment().weatherType == Weathers.SNOW, "    Weather at " + prop + " is not snow!");
    }

    // Add a new property.
    XYCoord snowCity = new XYCoord(12,5);
    XYCoord inRange = new XYCoord(7,5);
    XYCoord outOfRange = new XYCoord(6,5);

    // Grant a new city and make sure the snow spreads from there, but not too far.
    testMap.getLocation(snowCity).setOwner(Ave);
    int maxIters = 15;
    for( int i = 1; i < maxIters; ++i )
    {
      // Add more snow.
      GameEventQueue turnEvents = Ave.initTurn(testMap);
      for(GameEvent event: turnEvents) event.performEvent(testMap);
      if( testMap.getEnvironment(inRange).weatherType == Weathers.SNOW )
      {
        System.out.println("Snow after " + i + " turns");
      }
      if( testMap.getEnvironment(snowCity).weatherType != Weathers.SNOW )
      {
        testPassed &= validate(false, "    Snow city is not snowy!");
        break;
      }
      if( testMap.getEnvironment(outOfRange).weatherType == Weathers.SNOW )
      {
        testPassed &= validate(false, "    Snow expanded beyond max range!");
        break;
      }
    }
    //System.out.println("Snow map:\n" + Ave.getSnowMapAsString());

    // Take the city away, and make sure the snow recedes.
    testMap.getLocation(snowCity).setOwner(null);
    for( int i = 1; i < maxIters; ++i )
    {
      GameEventQueue turnEvents = game.turn();
      for(GameEvent event: turnEvents) event.performEvent(testMap);
      Environment cityEnv = testMap.getEnvironment(snowCity); 
      if( cityEnv.weatherType != Weathers.SNOW )
      {
        System.out.println("Snow melted after " + i + " turns");
        break;
      }
    }
    //System.out.println("Snow map:\n" + Ave.getSnowMapAsString());
    testPassed &= validate( testMap.getEnvironment(snowCity).weatherType != Weathers.SNOW, "    SnowCity never melted!" );

    return testPassed;
  }

  private boolean testGlacio()
  {
    boolean testPassed = true;

    // Activate Ave's abilities, and make sure we see the intended effects.
    // We'll also add some units to see Glacio's damage and tree-clearing effects.
    XYCoord forestTile = new XYCoord(8, 4);
    addUnit(testMap, Ave, UnitEnum.INFANTRY, 8, 5);
    addUnit(testMap, Ave, UnitEnum.INFANTRY, 6, 5);
    Unit patchInf = addUnit(testMap, Patch, UnitEnum.INFANTRY, 7, 5);
    testPassed &= validate( testMap.getEnvironment(forestTile).terrainType == TerrainType.FOREST, "    " + forestTile + " is not a Forest, but should be!");

    //Ave.log(true);
    //System.out.println("Snow map:\n" + Ave.getSnowMapAsString());
    Ave.modifyAbilityPower(20); // Charge up and use all of Ave's abilities
    ArrayList<CommanderAbility> abilities = Ave.getReadyAbilities();
    for( CommanderAbility ca : abilities )
    {
      ca.activate(testMap);
    }

    // Check that the ability did what it was supposed to.
    XYCoord inRange = new XYCoord(7,5);
    testPassed &= validate( testMap.getEnvironment(inRange).weatherType == Weathers.SNOW, "    inRange tile didn't refreeze!" );
    testPassed &= validate( testMap.getEnvironment(forestTile).terrainType == TerrainType.GRASS, "    " + forestTile + " was not cleared to GRASS!");
    testPassed &= validate( patchInf.getHP() == 8, String.format("    Infantry has %s HP, but should have 8!", patchInf.getHP()));
    //System.out.println("Snow map after Glacio:\n" + Ave.getSnowMapAsString());
    //Ave.log(false);

    return testPassed;
  }
}
