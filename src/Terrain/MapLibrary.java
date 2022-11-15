package Terrain;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderLibrary;
import Engine.Army;
import Engine.FloodFillFunctor;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.Maps.CageMatch;
import Terrain.Maps.FiringRange;
import Terrain.Maps.MapReader;
import Terrain.Maps.SpannIsland;
import Terrain.Maps.TestRange;
import Units.UnitContext;
import Units.UnitModel;

public class MapLibrary
{
  private static ArrayList<MapInfo> availableMaps;
  
  public static ArrayList<MapInfo> getMapList()
  {
    if(null == availableMaps)
    {
      loadMapInfos();
    }
    return availableMaps;
  }
  
  private static void loadMapInfos()
  {
    availableMaps = new ArrayList<MapInfo>();
    availableMaps.add(TestRange.getMapInfo());
    availableMaps.add(FiringRange.getMapInfo());
    availableMaps.add(SpannIsland.getMapInfo());
    availableMaps.add(CageMatch.getMapInfo());
    availableMaps.addAll(MapReader.readMapData());
    long startTotal = System.currentTimeMillis();
    for(MapInfo mi : availableMaps)
    {
      System.out.print(mi.mapName + ": ");
      long startMap = System.currentTimeMillis();
      GameInstance gi = setupTest(mi);
      MapMaster gameMap = gi.gameMap;
      final Commander co = gi.activeArmy.cos[0];
      final ArrayList<UnitModel> allModels = gi.activeArmy.gameRules.unitModelScheme.getGameReadyModels().unitModels;
      boolean includeOccupiedSpaces = true;
      for(UnitModel um : allModels)
      {
        UnitContext uc = new UnitContext(co, um);
        FloodFillFunctor mover = uc.calculateMoveType().getUnitMoveFunctor(null, includeOccupiedSpaces, includeOccupiedSpaces);
        int movePower = uc.calculateMovePower();
        for( int x = 0; x < gameMap.mapWidth; ++x )
        {
          for( int y = 0; y < gameMap.mapHeight; ++y )
          {
            Utils.findFloodFillArea(new XYCoord(x, y), mover, movePower, gameMap);
          }
        }
      }
      System.out.println((System.currentTimeMillis() - startMap) + " ms");
    }
    System.out.println("Total time: " + (System.currentTimeMillis() - startTotal)/1000 + " s");
  }

  public static void performEvents(GameInstance game, GameEventQueue sequence)
  {
    for( GameEvent event : sequence )
    {
      event.performEvent( game.gameMap );
      GameEventListener.publishEvent(event, game);
    }
  }
  private static GameInstance setupTest(MapInfo mapInfo)
  {
    GameScenario scn = new GameScenario();
    Army[] armies = new Army[mapInfo.getNumCos()];
    for( int i = 0; i < armies.length; ++i )
      armies[i] = new Army(scn, CommanderLibrary.NotACO.getInfo().create(scn.rules));

    MapMaster testMap = new MapMaster(armies, mapInfo);
    GameInstance game = new GameInstance(armies, testMap);

    GameEventQueue sequence = new GameEventQueue();
    game.turn(sequence);
    performEvents(game, sequence);
    return game;
  }

  public static MapInfo getByName(String mapName)
  {
    ArrayList<MapInfo> maps = getMapList();
    MapInfo requested = null;
    for(MapInfo mi : maps)
    {
      if( mi.mapName.equalsIgnoreCase(mapName) )
      {
        requested = mi;
        break;
      }
    }
    return requested;
  }
}
