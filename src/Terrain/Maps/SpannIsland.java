package Terrain.Maps;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.Types.BaseTerrain;

public class SpannIsland extends IMapBuilder
{
  private final static String mapName = "Spann Island";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static BaseTerrain[][] terrainData =
    { { SE, SE, SE, SE, SE, SE, SE, SE, SE, SE }, // 0
      { SE, SE, GR, CT, GR, FC, FC, SE, RF, SE }, // 1
      { SE, GR, GR, CT, GR, RD, HQ, FC, SE, SE }, // 2
      { SE, FR, FR, RD, RD, RD, GR, FC, SE, SE }, // 3
      { SE, RD, RD, RD, GR, CT, GR, GR, MT, SE }, // 4
      { SE, RD, FR, SE, SE, SE, GR, GR, CT, SE }, // 5
      { SE, BR, SE, SE, SE, SE, GR, GR, MT, SE }, // 6
      { SE, RD, GR, SE, SE, FR, FR, GR, FR, SE }, // 7
      { SE, RD, RD, RD, RD, RD, FR, GR, CT, SE }, // 8
      { SE, GR, CT, GR, CT, RD, GR, GR, GR, SE }, // 9
      { SE, FR, GR, MT, MT, RD, SE, SE, BR, SE }, // 10
      { SE, FC, GR, GR, CT, RD, SE, SE, GR, SE }, // 11
      { SE, GR, HQ, RD, RD, RD, BR, GR, CT, SE }, // 12
      { SE, FC, FC, FC, GR, CT, SE, GR, CT, SE }, // 13
      { SE, SE, SE, SE, SE, SE, SE, SE, SE, SE } };// 14
  private static XYCoord[] co1Props = { new XYCoord(1, 5), new XYCoord(1, 6), new XYCoord(2, 6), new XYCoord(2, 7), new XYCoord(3, 7) };
  private static XYCoord[] co2Props = { new XYCoord(11, 1), new XYCoord(12, 2), new XYCoord(13, 1), new XYCoord(13, 2), new XYCoord(13, 3) };
  private static XYCoord[][] properties = { co1Props, co2Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
