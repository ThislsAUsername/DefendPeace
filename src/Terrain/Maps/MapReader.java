package Terrain.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.MapInfo;

public class MapReader extends IMapBuilder
{
  private final static String mapName = "Spann Island";
  // Defines the terrain for this map. Each row is a vertical column of the map.
  private final static Environment.Terrains[][] terrainData = { { SE, SE, SE, SE, SE, SE, SE, SE, SE, SE }, // 0
      { SE, SE, GR, CT, GR, FC, FC, SE, RF, SE }, // 1
      { SE, GR, GR, CT, GR, RD, HQ, FC, SE, SE }, // 2
      { SE, FR, FR, RD, RD, RD, GR, FC, SE, SE }, // 3
      { SE, RD, RD, RD, GR, CT, GR, GR, MT, SE }, // 4
      { SE, RD, FR, SE, SE, SE, GR, GR, CT, SE }, // 5
      { SE, RD, SE, SE, SE, SE, GR, GR, MT, SE }, // 6
      { SE, RD, GR, SE, SE, FR, FR, GR, FR, SE }, // 7
      { SE, RD, RD, RD, RD, RD, FR, GR, CT, SE }, // 8
      { SE, GR, CT, GR, CT, RD, GR, GR, GR, SE }, // 9
      { SE, FR, GR, MT, MT, RD, SE, SE, RD, SE }, // 10
      { SE, FC, GR, GR, CT, RD, SE, SE, GR, SE }, // 11
      { SE, GR, HQ, RD, RD, RD, RD, GR, CT, SE }, // 12
      { SE, FC, FC, FC, GR, CT, SE, GR, CT, SE }, // 13
      { SE, SE, SE, SE, SE, SE, SE, SE, SE, SE } };// 14
  private static XYCoord[] co1Props = { new XYCoord(1, 5), new XYCoord(1, 6), new XYCoord(2, 6), new XYCoord(2, 7),
      new XYCoord(3, 7) };
  private static XYCoord[] co2Props = { new XYCoord(11, 1), new XYCoord(12, 2), new XYCoord(13, 1), new XYCoord(13, 2),
      new XYCoord(13, 3) };
  private static XYCoord[][] properties = { co1Props, co2Props };
  private static MapInfo info = new MapInfo(mapName, terrainData, properties);

  /**
   * Tells the MapReader to read in the maps.
   */
  public static ArrayList<MapInfo> readMapData()
  {
    ArrayList<MapInfo> importMaps = new ArrayList<MapInfo>();

    try
    {
      final File folder = new File("res/map");

      for( final File fileEntry : folder.listFiles() )
      {
        // we aren't checking subdirectories, yet
        if( !fileEntry.isDirectory() )
        {
          try
          {
            // System.out.println();
            String mapName = fileEntry.getName();
            if( !mapName.contains(".map") )
              continue;
            mapName = mapName.replaceAll("_", " ");
            mapName = mapName.replaceAll(".map", "");
            System.out.println("Parsing map: " + mapName);
            Scanner scanner = new Scanner(fileEntry);
            String line = scanner.nextLine();
            ArrayList<ArrayList<Environment.Terrains>> terrainData = new ArrayList<ArrayList<Environment.Terrains>>();

            ArrayList<ArrayList<XYCoord>> properties = new ArrayList<ArrayList<XYCoord>>(4);
            properties.add(new ArrayList<XYCoord>());
            properties.add(new ArrayList<XYCoord>());
            properties.add(new ArrayList<XYCoord>());
            properties.add(new ArrayList<XYCoord>());

            for( int i = 0; i < line.length() / 4; i++ )
            {
              terrainData.add(new ArrayList<Environment.Terrains>());
            }
            boolean moreLines = true;
            for( int yCoord = 0; moreLines; yCoord++ )
            {
              for( int i = 0; i < line.length(); i += 4 )
              {
                int sideNumber = 99;
                try
                {
                  sideNumber = Integer.parseInt(("" + line.charAt(i) + line.charAt(i + 1)).trim());
                }
                catch (NumberFormatException e)
                {}
                if( sideNumber < 4 ) // TODO: add more sides
                {
                  properties.get(sideNumber).add(new XYCoord(i / 4, yCoord));
                }
                String terrainCode = "" + line.charAt(i + 2) + line.charAt(i + 3);
                terrainData.get(i / 4).add(stringToCode(terrainCode));
              }
              moreLines = scanner.hasNextLine();
              if (moreLines)
                line = scanner.nextLine();
            }
            ArrayList<Environment.Terrains[]> terrainArrayArray = new ArrayList<Environment.Terrains[]>();
            for( int i = 0; i < terrainData.size(); i++ )
            {
              terrainArrayArray.add(terrainData.get(i).toArray(new Environment.Terrains[0]));
            }
            ArrayList<XYCoord[]> propertyArrayArray = new ArrayList<XYCoord[]>();
            for( int i = 0; i < properties.size(); i++ )
            {
              if( properties.get(i).size() > 0 )
                propertyArrayArray.add(properties.get(i).toArray(new XYCoord[0]));
            }
            MapInfo info = new MapInfo(mapName, terrainArrayArray.toArray(new Environment.Terrains[0][0]),
                propertyArrayArray.toArray(new XYCoord[0][0]));
            importMaps.add(info);
            scanner.close();
          }
          // skip the file if you can't find it
          catch (FileNotFoundException e)
          {
            System.out.println("WARNING: Could not find map file " + fileEntry.getName());
          }
        }
      }
    }
    catch (NullPointerException e)
    {}
    return importMaps;
  }

  private static Environment.Terrains stringToCode(String input)
  {
    // helpful regex:
    // ^(..)$
    // case "\1":\nreturn \1;
    switch (input)
    {
      case "CT":
        return CT;
      case "DN":
        return DN;
      case "FC":
        return FC;
      //      case "AP":
      //        return AP;
      //      case "SP":
      //        return SP;
      case "FR":
        return FR;
      case "GR":
        return GR;
      case "HQ":
        return HQ;
      //      case "LB":
      //        return LB;
      case "MT":
        return MT;
      case "RF":
        return RF;
      case "RD":
        return RD;
      case "SE":
        return SE;
      case "SH":
        return SH;
      default:
        return GR;
    }
  }

  public static MapInfo getMapInfo()
  {
    return info;
  }
}
