package Terrain;

import Engine.XYCoord;
import Terrain.Types.TerrainType;

public class MapInfo
{
  public final String mapName;
  public final TerrainType[][] terrain;
  public final XYCoord[][] COProperties;

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props)
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

  public int getNumCos()
  {
    return COProperties.length;
  }
}
