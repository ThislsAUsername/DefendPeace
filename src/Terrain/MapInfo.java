package Terrain;

import java.util.ArrayList;
import java.util.Map;

import Engine.XYCoord;
import Units.UnitModel;

public class MapInfo
{
  public final String mapName;
  public final TerrainType[][] terrain;
  public final XYCoord[][] COProperties;
  public final ArrayList<Map<XYCoord,UnitModel.UnitEnum>> mapUnits;

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props)
  {
    this(name, tiles, props, new ArrayList<Map<XYCoord,UnitModel.UnitEnum>>());
  }

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props, ArrayList<Map<XYCoord,UnitModel.UnitEnum>> units)
  {
    mapName = name;
    terrain = tiles;
    COProperties = props;
    mapUnits = units;
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
