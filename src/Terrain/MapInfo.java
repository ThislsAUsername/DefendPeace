package Terrain;

import Engine.XYCoord;

public class MapInfo
{
  public final String mapName;
  public final Environment.Terrains[][] terrain;
  public final XYCoord[][] COProperties;

  public MapInfo(String name, Environment.Terrains[][] tiles, XYCoord[][] props)
  {
    mapName = name;
    terrain = tiles;
    COProperties = props;
  }

  public int getWidth()
  {
    return terrain.length;
  }

  public int getHeight()
  {
    return terrain[0].length;
  }
}
