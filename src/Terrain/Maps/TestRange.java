package Terrain.Maps;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.Types.TerrainType;

public class TestRange extends IMapBuilder
{
  private final static String mapName = "Test Range";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static TerrainType[][] terrainData = {
      { SE, SE, SE, SE, SE, SE, SE, SE, SE, SE }, // 0
      { SE, SH, GR, GR, CT, SP, GR, FC, CT, SE }, // 1
      { SE, SH, CT, GR, GR, CT, GR, FC, GR, SE }, // 2
      { SE, GR, MT, GR, GR, FR, GR, GR, GR, SE }, // 3
      { SE, GR, GR, MT, CT, FC, RD, GR, CT, SE }, // 4
      { RF, GR, GR, GR, MT, HQ, AP, GR, SP, SE }, // 5
      { SE, SP, GR, GR, FC, FR, FC, GR, GR, RF }, // 6
      { SE, FC, GR, RD, RD, RD, RD, GR, FC, SE }, // 7
      { SE, GR, GR, FC, FR, FC, GR, GR, SP, SE }, // 8
      { RF, SP, GR, AP, HQ, MT, GR, GR, GR, SE }, // 9
      { SE, CT, GR, RD, FC, CT, MT, GR, GR, RF }, // 10
      { SE, GR, GR, GR, FR, GR, GR, MT, GR, SE }, // 11
      { SE, GR, FC, GR, CT, GR, GR, CT, SH, SE }, // 12
      { SE, CT, FC, GR, SP, CT, GR, GR, SH, SE }, // 13
      { SE, SE, SE, SE, SE, SE, SE, SE, SE, SE } };// 14
  private static XYCoord[] co1Props = { new XYCoord(1, 8), new XYCoord(1, 7), new XYCoord(2, 7), new XYCoord(5, 5),
      new XYCoord(5, 6), new XYCoord(4, 5), new XYCoord(5, 8), new XYCoord(1, 5), new XYCoord(6, 1), new XYCoord(6, 4),
      new XYCoord(6, 6) };
  private static XYCoord[] co2Props = { new XYCoord(13, 1), new XYCoord(13, 2), new XYCoord(12, 2), new XYCoord(9, 4),
      new XYCoord(9, 3), new XYCoord(10, 4), new XYCoord(13, 4), new XYCoord(9, 1), new XYCoord(8, 3), new XYCoord(8, 5),
      new XYCoord(8, 8) };
  private static XYCoord[][] properties = { co1Props, co2Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
