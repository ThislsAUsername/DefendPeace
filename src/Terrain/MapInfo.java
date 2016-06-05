package Terrain;

public class MapInfo
{
  public final String mapName;
  public final Location[][] terrain;
  
  public MapInfo(String name, Location[][] tiles)
  {
    mapName = name;
    terrain = tiles;
  }
}
