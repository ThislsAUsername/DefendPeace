package Terrain.Maps;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.Types.BaseTerrain;

public class FiringRange extends IMapBuilder
{
  private final static String mapName = "Firing Range";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static BaseTerrain[][] terrainData =
   {{SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}, // 0
    {SE, SH, GR, GR, CT, GR, GR, FC, HQ, SE}, // 1
    {SE, SH, CT, GR, GR, CT, GR, FC, GR, SE}, // 2
    {SE, GR, MT, GR, GR, FR, GR, GR, GR, SE}, // 3
    {SE, GR, GR, MT, CT, GR, RD, GR, CT, SE}, // 4
    {RF, GR, GR, GR, MT, GR, RD, GR, GR, SE}, // 5
    {SE, CT, GR, GR, GR, FR, RD, GR, GR, RF}, // 6
    {SE, FC, GR, RD, RD, RD, RD, GR, FC, SE}, // 7
    {SE, GR, GR, RD, FR, GR, GR, GR, CT, SE}, // 8
    {RF, GR, GR, RD, GR, MT, GR, GR, GR, SE}, // 9
    {SE, CT, GR, RD, GR, CT, MT, GR, GR, RF}, // 10
    {SE, GR, GR, GR, FR, GR, GR, MT, GR, SE}, // 11
    {SE, GR, FC, GR, CT, GR, GR, CT, SH, SE}, // 12
    {SE, HQ, FC, GR, GR, CT, GR, GR, SH, SE}, // 13
    {SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}};// 14
  private static XYCoord[] co1Props = { new XYCoord(1, 8), new XYCoord(1, 7), new XYCoord(2, 7) };
  private static XYCoord[] co2Props = { new XYCoord(13, 1), new XYCoord(13, 2), new XYCoord(12, 2) };
  private static XYCoord[][] properties = { co1Props, co2Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
