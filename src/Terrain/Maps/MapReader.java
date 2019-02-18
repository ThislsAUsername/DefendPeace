package Terrain.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.TerrainType;
import Units.UnitModel;

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
            if( !mapName.endsWith(".map") )
              continue;
            // underscores->spaces makes it pretty
            mapName = mapName.replaceAll("_", " ");
            mapName = mapName.replaceAll("\\.map", "");
            System.out.println("INFO: Parsing map: " + mapName);

            // We need a list of who starts owning what properties. This is that list.
            // Each arraylist contains coordinates, and which list it is denotes who owns that property.
            Map<Integer,ArrayList<XYCoord>> landOwnershipMap = new TreeMap<Integer,ArrayList<XYCoord>>();

            Scanner scanner = new Scanner(fileEntry);
            // We need the first line so we can pre-fill our terrain data array with the proper number of sub-arrays.
            String line = scanner.nextLine();

            // Map tiles are accessed via mapArray[x][y]
            // Thus, each subarray contains a column.
            // Each row on the map consists of one value with the same index from each subarray.
            ArrayList<ArrayList<TerrainType>> terrainData = new ArrayList<ArrayList<TerrainType>>();
            // The representation for each tile is 4 characters long, so we divide the length by 4 to get the map's width.
            // Height will scale with how many lines of file there are.
            for( int i = 0; i < line.length() / 4; i++ )
            {
              terrainData.add(new ArrayList<TerrainType>());
            }

            // This boolean is broken out since yCoord makes sense as a for-loop-iterated variable.
            boolean moreLines = true;
            int numSides = 0;
            // Here, we're iterating down the lines of the file, and thus up the y coordinate scale.
            for( int yCoord = 0; moreLines; yCoord++ )
            {
              // Since we have the first line already when we enter here, we read in the next one at the end of this loop.
              // The inner loop iterates over the x coordinates.
              for( int xCoord = 0; xCoord * 4 < line.length(); xCoord++ )
              {
                int faction = -1;
                // Pull the side value out, so we can see if there's anything in it.
                // Note to future self: substring()'s second parameter is non-inclusive.
                String sideString = line.substring(xCoord * 4, xCoord * 4 + 2).trim();
                // If so, we use the value.
                if( sideString.length() > 0 )
                  faction = Integer.parseInt(sideString);
                if( faction != -1 )
                {
                  if( null == landOwnershipMap.get(faction) )
                  {
                    numSides++;
                    landOwnershipMap.put(faction, new ArrayList<XYCoord>());
                    landOwnershipMap.get(faction).add(new XYCoord(xCoord, yCoord));
                  }
                  else
                    landOwnershipMap.get(faction).add(new XYCoord(xCoord, yCoord));
                }
                // Terrain code comes after side number, so we grab that as well.
                String terrainCode = line.substring(xCoord * 4 + 2, xCoord * 4 + 4).trim();
                // And we place a new square in the proper column.
                terrainData.get(xCoord).add(stringToCode(terrainCode));
              }
              // We need to know if there's more lines before we ask for another line, or there's gonna be a problem.
              moreLines = scanner.hasNextLine();
              if( moreLines )
              {
                line = scanner.nextLine();
                if (terrainData.size()*4 > line.length())
                  moreLines = false;
              }
            }
            
            // intermediate array to hold the flat arrays harvested from each column
            ArrayList<TerrainType[]> terrainArrayArray = new ArrayList<TerrainType[]>();
            for( int i = 0; i < terrainData.size(); i++ )
            {
              terrainArrayArray.add(terrainData.get(i).toArray(new TerrainType[0]));
            }
            ArrayList<Integer> factionList = new ArrayList<Integer>(); 
            ArrayList<XYCoord[]> propertyArrayArray = new ArrayList<XYCoord[]>();
            for( Entry<Integer, ArrayList<XYCoord>> ownerEntry : landOwnershipMap.entrySet() )
            {
              // We don't want to tell the game that there's more players than there are, so we make sure to only send sides who own properties.
              // This does lose the data of what each team was initially, but turn order should be preserved and I don't care beyond that.
              propertyArrayArray.add(ownerEntry.getValue().toArray(new XYCoord[0]));
              factionList.add(ownerEntry.getKey());
            }
            
            // now that we've parsed the map, try to parse any units
            ArrayList<Map<XYCoord,UnitModel.UnitEnum>> units = new ArrayList<Map<XYCoord,UnitModel.UnitEnum>>();
            for (int i = 0; i < numSides; ++i)
            {
              units.add(new HashMap<XYCoord,UnitModel.UnitEnum>());
            }
            // we'll assume there's a useless line between the map and any units because I'm ~lazy~
            while (scanner.hasNextLine())
            {
              line = scanner.nextLine();
              String[] unitTokens = line.split(",");
              // team, unit type, x, y
              if (unitTokens.length == 4)
              {
                try
                {
                  int team = Integer.parseInt(unitTokens[0].trim());
                  UnitModel.UnitEnum type = UnitModel.UnitEnum.valueOf(unitTokens[1].trim());
                  int x = Integer.parseInt(unitTokens[2].trim());
                  int y = Integer.parseInt(unitTokens[3].trim());
                  units.get(factionList.indexOf(team)).put(new XYCoord(x, y), type);
                }
                catch (Exception e)
                {
                  System.out.println("Caught exception while parsing units: " + e.getMessage());
                }
              }
            }
            
            // Finally, we make our map's container to put in importMaps.
            MapInfo info = new MapInfo(mapName, terrainArrayArray.toArray(new TerrainType[0][0]),
                propertyArrayArray.toArray(new XYCoord[0][0]), units);
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

  private static TerrainType stringToCode(String input)
  {
    // Regex for generating the cases in this switch statement via a line-separated list of the terrain codes.
    // In Notepad++, use the Find/Replace window in regex mode
    // Find: ^(..)$
    // Replace: case "\1":\nreturn \1;
    // It won't output the correct indentation, but that's what autoformatting is for.
    switch (input)
    {
      case "BR":
        return BR;
      case "CT":
        return CT;
      case "DN":
        return DN;
      case "FC":
        return FC;
      case "AP":
        return AP;
      case "SP":
        return SP;
      case "FR":
        return FR;
      case "GR":
        return GR;
      case "HQ":
        return HQ;
      case "LB":
        return LB;
      case "MT":
        return MT;
      case "RF":
        return RF;
      case "RV":
        return RV;
      case "RD":
        return RD;
      case "SE":
        return SE;
      case "SH":
        return SH;
      case "PI":
        return PI;
      case "BK":
        return BK;
      case "SR":
        return SR;
      case "TW":
        return TW;
      default:
        return GR;
    }
  }
}