package Terrain.Maps;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.MapInfo;

public class Podracing extends IMapBuilder
{
  private final static String mapName = "Podracing";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static Environment.Terrains[][] terrainData =
   {{SE, SE, SE, SE, SE, SE, RF, SE, SE, SE}, // 0
    {RF, SE, SE, FR, FC, FC, FR, SE, SE, SE}, // 1
    {SE, SE, GR, FR, HQ, FC, FR, GR, SE, RF}, // 2
    {SE, SE, CT, GR, GR, GR, GR, CT, SE, SE}, // 3
    {SE, RF, GR, CT, GR, GR, CT, GR, RF, SE}, // 4
    {SE, SE, RD, FR, SE, RF, FR, RD, SE, SE}, // 5
    {RF, SE, RD, SE, SE, SE, SE, RD, SE, SE}, // 6
    {SE, SE, RD, SE, RF, SE, SE, RD, SE, RF}, // 7
    {SE, GR, RD, SE, SE, RF, SE, RD, GR, SE}, // 8
    {RF, FR, RD, SE, SE, SE, SE, RD, FR, SE}, // 9
    {SE, FR, CT, GR, SH, SH, GR, CT, FR, SE}, // 10
    {SE, GR, GR, CT, SE, SE, CT, GR, GR, SE}, // 11
    {SE, HQ, GR, GR, SH, SH, GR, GR, HQ, SE}, // 12
    {SE, FC, FC, GR, SE, SE, GR, FC, FC, SE}, // 13
    {SE, SE, SE, SE, SE, SE, SE, SE, SE, SE}};// 14
  private static XYCoord[] co1Props = { new XYCoord(1, 4), new XYCoord(1, 5), new XYCoord(2, 4), new XYCoord(2, 5) };
  private static XYCoord[] co2Props = { new XYCoord(13, 1), new XYCoord(13, 2), new XYCoord(12, 1) };
  private static XYCoord[] co3Props = { new XYCoord(13, 7), new XYCoord(13, 8), new XYCoord(12, 8) };
  private static XYCoord[][] properties = { co1Props, co2Props, co3Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
