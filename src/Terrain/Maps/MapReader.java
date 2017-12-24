package Terrain.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.MapInfo;

public class MapReader extends IMapBuilder
{
  /**
   * Tells the MapReader to read in the maps.
   */
  public static ArrayList<MapInfo> readMapData()
  {
    ArrayList<MapInfo> importMaps = new ArrayList<MapInfo>();

    // This try{} is to safeguard us from exceptions if the res/map folder doesn't exist.
    // If it fails, we don't need to do anything in the catch{} since we just won't have anything in our list.
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
            // We get the filename, and make it look nice for our map list.
            String mapName = fileEntry.getName();
            // Mostly, we just don't want to try to interpret the python script as a map. That'd be bad.
            if( !mapName.contains(".map") )
              continue;
            // underscores->spaces makes it pretty
            mapName = mapName.replaceAll("_", " ");
            mapName = mapName.replaceAll(".map", "");
            System.out.println("INFO: Parsing map: " + mapName);

            // We need a list of who starts owning what properties. This is that list.
            // Each arraylist contains coordinates, and which list it is denotes who owns that property.
            ArrayList<ArrayList<XYCoord>> properties = new ArrayList<ArrayList<XYCoord>>();
            // Will support 14 sides in the future, possibly more.
            for( int i = 0; i < 4; i++ ) // TODO: add more sides
            {
              properties.add(new ArrayList<XYCoord>());
            }

            Scanner scanner = new Scanner(fileEntry);
            // We need the first line so we can pre-fill our terrain data array with the proper number of sub-arrays.
            String line = scanner.nextLine();

            // The game's internal representation of the map is transposed from the representation you'd see if you wrote out the array.
            // This is so because that allows you to access mapArray[x][y], which is convenient for handling the map.
            // Thus, each subarray contains a column.
            // Each row on the map consists of one value with the same index from each subarray.
            ArrayList<ArrayList<Environment.Terrains>> terrainData = new ArrayList<ArrayList<Environment.Terrains>>();
            // The representation for each tile is 4 characters long, so we divide the length by 4 to get the map's width.
            // Height will scale with how many lines of file there are.
            for( int i = 0; i < line.length() / 4; i++ )
            {
              terrainData.add(new ArrayList<Environment.Terrains>());
            }

            // This boolean is broken out since yCoord makes sense as a for-loop-iterated variable.
            boolean moreLines = true;
            // Here, we're iterating down the lines of the file, and thus up the y coordinate scale.
            for( int yCoord = 0; moreLines; yCoord++ )
            {
              // Since we have the first line already when we enter here, we read in the next one at the end of this loop.
              // The inner loop iterates over the x coordinates.
              for( int xCoord = 0; xCoord * 4 < line.length(); xCoord++ )
              {
                // I don't think we'll ever support 99 teams. That'd be absurd.
                // 99 thus serves as a sentinel value, much like it does is movement costs.
                int sideNumber = 99;
                // Pull the side value out, so we can see if there's anything in it.
                // Note to future self: substring()'s second parameter is non-inclusive.
                String sideString = line.substring(xCoord * 4, xCoord * 4 + 2).trim();
                // If so, we use the value.
                if( sideString.length() > 0 )
                  sideNumber = Integer.parseInt(sideString);
                if( sideNumber < 4 ) // TODO: add more sides
                {
                  properties.get(sideNumber).add(new XYCoord(xCoord, yCoord));
                }
                // Terrain code comes after side number, so we grab that as well.
                String terrainCode = line.substring(xCoord * 4 + 2, xCoord * 4 + 4).trim();
                // And we place a new square in the proper column.
                terrainData.get(xCoord).add(stringToCode(terrainCode));
              }
              // We need to know if there's more lines before we ask for another line, or there's gonna be a problem.
              moreLines = scanner.hasNextLine();
              if( moreLines )
                line = scanner.nextLine();
            }
            // intermediate array to hold the flat arrays harvested from each column
            ArrayList<Environment.Terrains[]> terrainArrayArray = new ArrayList<Environment.Terrains[]>();
            for( int i = 0; i < terrainData.size(); i++ )
            {
              terrainArrayArray.add(terrainData.get(i).toArray(new Environment.Terrains[0]));
            }
            ArrayList<XYCoord[]> propertyArrayArray = new ArrayList<XYCoord[]>();
            for( int i = 0; i < properties.size(); i++ )
            {
              // We don't want to tell the game that there's more players than there are, so we make sure to only send sides who own properties.
              // This does lose the data of what each team was initially, but turn order should be preserved and I don't care beyond that.
              if( properties.get(i).size() > 0 )
                propertyArrayArray.add(properties.get(i).toArray(new XYCoord[0]));
            }
            // Finally, we make our map's container to put in importMaps.
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
    {
      System.out.println("WARNING: res/map directory does not exist.");
    }
    return importMaps;
  }

  private static Environment.Terrains stringToCode(String input)
  {
    // Regex for generating the cases in this switch statement via a line-separated list of the terrain codes.
    // In Notepad++, use the Find/Replace window in regex mode
    // Find: ^(..)$
    // Replace: case "\1":\nreturn \1;
    // It won't output the correct indentation, but that's what autoformatting is for.
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
}
