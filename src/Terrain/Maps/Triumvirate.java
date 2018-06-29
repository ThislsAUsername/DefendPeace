package Terrain.Maps;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.Types.TerrainType;

public class Triumvirate extends IMapBuilder
{
  private final static String mapName = "Triumvirate";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static TerrainType[][] terrainData =
   {{SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}, // 0
    {SE, FR, GR, GR, SE, SE, FR, FR, FR, SE}, // 1
    {SE, SE, FR, SE, SE, FR, GR, HQ, GR, SE}, // 2
    {SE, SE, SE, FR, GR, GR, FC, FC, FC, SE}, // 3
    {SE, SE, FR, GR, GR, CT, GR, GR, GR, SE}, // 4
    {SE, SE, GR, GR, CT, FC, GR, GR, FR, SE}, // 5
    {SE, FR, GR, FC, GR, GR, GR, CT, GR, SE}, // 6
    {SE, FR, HQ, FC, GR, GR, GR, FC, FR, SE}, // 7
    {SE, FR, GR, FC, GR, GR, GR, CT, GR, SE}, // 8
    {SE, SE, GR, GR, CT, FC, GR, GR, FR, SE}, // 9
    {SE, SE, FR, GR, GR, CT, GR, GR, GR, SE}, // 10
    {SE, SE, SE, FR, GR, GR, FC, FC, FC, SE}, // 11
    {SE, SE, FR, SE, SE, FR, GR, HQ, GR, SE}, // 12
    {SE, FR, GR, GR, SE, SE, FR, FR, FR, SE}, // 13
    {SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}};// 14
  private static XYCoord[] co1Props = { new XYCoord(2, 7), new XYCoord(3, 6), new XYCoord(3, 7), new XYCoord(3, 8) };
  private static XYCoord[] co2Props = { new XYCoord(7, 2), new XYCoord(6, 3), new XYCoord(7, 3), new XYCoord(8, 3) };
  private static XYCoord[] co3Props = { new XYCoord(12, 7), new XYCoord(11, 6), new XYCoord(11, 7), new XYCoord(11, 8) };
  private static XYCoord[][] properties = { co1Props, co2Props, co3Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
