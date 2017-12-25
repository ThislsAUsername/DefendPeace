package Terrain;

import java.util.ArrayList;

import Terrain.Maps.CageMatch;
import Terrain.Maps.FiringRange;
import Terrain.Maps.Triumvirate;
import Terrain.Maps.SpannIsland;
import Terrain.Maps.TestingRange;

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
    availableMaps.add(FiringRange.getMapInfo());
    availableMaps.add(SpannIsland.getMapInfo());
    availableMaps.add(Triumvirate.getMapInfo());
    availableMaps.add(CageMatch.getMapInfo());
    availableMaps.add(TestingRange.getMapInfo());
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
