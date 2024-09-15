package Terrain;

import java.util.ArrayDeque;
import java.util.ArrayList;

import Terrain.MapInfo.MapNode;
import Terrain.Maps.CageMatch;
import Terrain.Maps.FiringRange;
import Terrain.Maps.MapReader;
import Terrain.Maps.SpannIsland;
import Terrain.Maps.TestRange;
import lombok.var;

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
    var nodes = new ArrayDeque<MapNode>();
    nodes.add(MapReader.readMapData());
    while (!nodes.isEmpty())
    {
      MapNode n = nodes.poll();
      if( null == n.result )
        nodes.addAll(n.children);
      else
        availableMaps.add(n.result);
    }
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
