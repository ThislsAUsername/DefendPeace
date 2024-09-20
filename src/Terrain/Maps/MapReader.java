package Terrain.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.MapInfo.MapNode;
import Terrain.TerrainType;
import lombok.var;

public class MapReader extends IMapBuilder
{
  /**
   * Tells the MapReader to read in the maps.
   */
  public static MapNode readMapData()
  {
    MapNode root = new MapNode(null, "", null);

    // This try{} is to safeguard us from exceptions if the res/map folder doesn't exist.
    // If it fails, we don't need to do anything in the catch{} since we just won't have anything in our list.
    try
    {
      var nodes = new ArrayDeque<MapNode>();
      nodes.add(root);
      while (!nodes.isEmpty())
      {
        MapNode parent = nodes.poll();
        final File folder = new File(Engine.Driver.JAR_DIR + "res/map/" + parent.uri());

        for( final File fileEntry : folder.listFiles() )
        {
          String name = fileEntry.getName();
          if( fileEntry.isDirectory() )
          {
            MapNode dirNode = new MapNode(parent, name, null);
            nodes.add(dirNode);
            parent.children.add(dirNode);
            continue;
          }
          // We just don't want to try to interpret the python script as a map. That'd be weird.
          if( !name.endsWith(".map") )
            continue;
          MapInfo readMap = readSingleMap(parent.uri(), fileEntry.getAbsolutePath());
          if( null != readMap )
            parent.children.add(new MapNode(parent, readMap.mapName, readMap));
        }

        // If this directory node has no viable children, get rid of it
        if( parent.parent != null &&
            parent.children.isEmpty() )
        {
          ArrayList<MapNode> ppc = parent.parent.children;
          ppc.remove(parent);
          continue;
        }

        // List children alphabetically, directories first
        parent.children.sort((nodeA, nodeB) -> {
          boolean isMapA = (null != nodeA.result);
          boolean isMapB = (null != nodeB.result);
          if( isMapA ^ isMapB ) // One of them is a directory
            return (isMapA) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
          return nodeA.name.compareTo(nodeB.name);
        });

        // If this directory node has only one child or directory children, get rid of it and prepend its name to the children's names
        if( parent.parent != null && (
              parent.children.size() == 1 ||
              parent.children.stream().allMatch((node) -> node.result == null)
            )
          )
        {
          ArrayList<MapNode> ppc = parent.parent.children;
          int index = ppc.indexOf(parent);
          ppc.remove(parent);
          // Give my children to my parent, in my spot, in order.
          for( var child : parent.children )
          {
            child.parent = parent.parent;
            child.name   = parent.name +"/"+ child.name;
            ppc.add(index, child);
            ++index;
          }
          continue;
        }
      }
    }
    catch (NullPointerException e)
    {
      System.out.println("WARNING: res/map directory does not exist.");
      e.printStackTrace(System.out);
    }
    return root;
  }

  public static MapInfo readSingleMap(final String filePath)
  {
    File fileEntry = new File(filePath);
    // Grab the directory name, relative to the parent
    String dirPath = fileEntry.getParent();
    dirPath = dirPath.replaceAll(".*res/map/?", "");
    return readSingleMap(dirPath, filePath);
  }
  public static MapInfo readSingleMap(final String dirPath, final String filePath)
  {
    try
    {
      File fileEntry = new File(filePath);
      // We get the filename, and make it look nice for our map list.
      String mapName = fileEntry.getName();
      // underscores->spaces makes it pretty
      mapName = mapName.replaceAll("_", " ");
      mapName = mapName.replaceAll("\\.map", "");

      // We need a list of who starts owning what properties. This is that list.
      // Each arraylist contains coordinates, and which list it is denotes who owns that property.
      Map<Integer, ArrayList<XYCoord>> landOwnershipMap = new TreeMap<Integer, ArrayList<XYCoord>>();

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
          if( terrainData.size() * 4 != line.length() )
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
      ArrayList<Map<XYCoord, String>> units = new ArrayList<Map<XYCoord, String>>();
      for( int i = 0; i < numSides; ++i )
      {
        units.add(new HashMap<XYCoord, String>());
      }
      // we'll assume there's a useless line between the map and any units because I'm ~lazy~
      while (scanner.hasNextLine())
      {
        line = scanner.nextLine();
        String[] unitTokens = line.split(",");
        // team, unit type, x, y
        if( unitTokens.length == 4 )
        {
          try
          {
            int team = Integer.parseInt(unitTokens[0].trim());
            String type = unitTokens[1].trim();
            int x = Integer.parseInt(unitTokens[2].trim());
            int y = Integer.parseInt(unitTokens[3].trim());
            // Add last in case of parsing errors
            units.get(factionList.indexOf(team)).put(new XYCoord(x, y), type);
          }
          catch (Exception e)
          {
            System.out.println("Caught exception while parsing units from "+filePath);
            e.printStackTrace(System.out);
          }
        }
      }

      // Finally, we make our map's container to put in importMaps.
      MapInfo info = new MapInfo(dirPath, mapName, terrainArrayArray.toArray(new TerrainType[0][0]),
          propertyArrayArray.toArray(new XYCoord[0][0]), units);

      scanner.close();
      return info;
    }
    // skip the file if you can't find it
    catch (FileNotFoundException e)
    {
      System.out.println("WARNING: Could not find map file " + filePath);
      e.printStackTrace(System.out);
    }
    return null;
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
      case "TA":
        return TA;
      case "SP":
        return SP;
      case "TS":
        return TS;
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
      case "TT":
        return TT;
      case "XX":
        return XX;
      case "PI":
        return PI;
      case "ME":
        return ME;
      case "BK":
        return BK;
      case "SR":
        return SR;
      case "TW":
        return TW;
      case "T3":
        return TW;
      case "T4":
        return T4;
      default:
        return GR;
    }
  }
}