package Terrain;

import java.util.ArrayList;

import Terrain.Maps.FiringRange;
import Terrain.Maps.Podracing;
import Terrain.Maps.SpannIsland;

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
    availableMaps.add(Podracing.getMapInfo());
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
