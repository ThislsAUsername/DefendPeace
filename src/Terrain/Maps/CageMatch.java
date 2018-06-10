package Terrain.Maps;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.Types.BaseTerrain;

public class CageMatch extends IMapBuilder
{
  private final static String mapName = "Cage Match";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static BaseTerrain[][] terrainData =
   {{SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}, // 0
    {SE, SE, FC, FC, FR, GR, GR, MT, GR, GR, FR, FC, FC, SE, SE}, // 1
    {SE, FC, HQ, FR, CT, GR, GR, FC, GR, GR, CT, FR, HQ, FC, SE}, // 2
    {SE, FC, FR, GR, GR, GR, GR, GR, GR, GR, GR, GR, FR, FC, SE}, // 3
    {SE, FR, CT, GR, MT, CT, GR, CT, GR, CT, MT, GR, CT, FR, SE}, // 4
    {SE, GR, GR, GR, CT, MT, GR, RD, GR, MT, CT, GR, GR, GR, SE}, // 5
    {SE, GR, GR, GR, GR, GR, GR, RD, GR, GR, GR, GR, GR, GR, SE}, // 6
    {SE, MT, FC, GR, CT, RD, RD, FC, RD, RD, CT, GR, FC, MT, SE}, // 7
    {SE, GR, GR, GR, GR, GR, GR, RD, GR, GR, GR, GR, GR, GR, SE}, // 8
    {SE, GR, GR, GR, CT, MT, GR, RD, GR, MT, CT, GR, GR, GR, SE}, // 9
    {SE, FR, CT, GR, MT, CT, GR, CT, GR, CT, MT, GR, CT, FR, SE}, // 10
    {SE, FC, FR, GR, GR, GR, GR, GR, GR, GR, GR, GR, FR, FC, SE}, // 11
    {SE, FC, HQ, FR, CT, GR, GR, FC, GR, GR, CT, FR, HQ, FC, SE}, // 12
    {SE, SE, FC, FC, FR, GR, GR, MT, GR, GR, FR, FC, FC, SE, SE}, // 13
    {SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}};// 14
  // 0   1   2   3   4   5   6   7   8   9   10  11  12  13  14
  private static XYCoord[] co1Props = { new XYCoord(2, 2), new XYCoord(1, 2), new XYCoord(1, 3), new XYCoord(2, 1), new XYCoord(3, 1) };
  private static XYCoord[] co2Props = { new XYCoord(12, 12), new XYCoord(11, 13), new XYCoord(12, 13), new XYCoord(13, 11), new XYCoord(13, 12) };
  private static XYCoord[] co3Props = { new XYCoord(12, 2), new XYCoord(11, 1), new XYCoord(12, 1), new XYCoord(13, 2), new XYCoord(13, 3) };
  private static XYCoord[] co4Props = { new XYCoord(2, 12), new XYCoord(1, 11), new XYCoord(1, 12), new XYCoord(2, 13), new XYCoord(3, 13) };
  private static XYCoord[][] properties = { co1Props, co2Props, co3Props, co4Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
