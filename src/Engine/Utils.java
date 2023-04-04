package Engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;

import CommandingOfficers.Commander;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapInfo;
import Terrain.TerrainType;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.UnitSpriteSet;
import UI.Art.SpriteArtist.SpriteUIUtils.ImageFrame;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

public class Utils
{

  /** Returns a list of all locations between 1 and maxRange tiles away from origin, inclusive. */
  public static ArrayList<XYCoord> findLocationsInRange(GameMap map, XYCoord origin, int maxRange)
  {
    return findLocationsInRange(map, origin, 1, maxRange);
  }

  /** Returns a list of all locations between minRange and maxRange tiles away from origin, inclusive. */
  public static ArrayList<XYCoord> findLocationsInRange(GameMap map, XYCoord origin, int minRange, int maxRange)
  {
    ArrayList<XYCoord> locations = new ArrayList<XYCoord>();

    // Loop through all the valid x and y offsets, as dictated by the max range, and add valid spaces to our collection.
    for( int yOff = -maxRange; yOff <= maxRange; ++yOff )
    {
      for( int xOff = -maxRange; xOff <= maxRange; ++xOff )
      {
        int currentRange = Math.abs(xOff) + Math.abs(yOff);
        XYCoord coord = new XYCoord(origin.xCoord + xOff, origin.yCoord + yOff);
        if( currentRange >= minRange && currentRange <= maxRange && map.isLocationValid(coord) )
        {
          // Add this location to the set.
          locations.add(coord);
        }
      }
    }

    return locations;
  }

  /** Returns a list of locations of all enemy units that weapon could hit from attackerPosition. */
  public static ArrayList<XYCoord> findTargetsInRange(GameMap map, Commander co, XYCoord attackerPosition, Weapon weapon)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, attackerPosition, weapon.model.minRange, weapon.model.maxRange);
    ArrayList<XYCoord> targets = new ArrayList<XYCoord>();
    for( XYCoord loc : locations )
    {
      if( map.getLocation(loc).getResident() != null && // Someone is there.
          map.getLocation(loc).getResident().CO.isEnemy(co) && // They are not friendly.
          weapon.getDamage(map.getLocation(loc).getResident().model) > 0 ) // We can shoot them.
      {
        targets.add(loc);
      }
    }
    return targets;
  }

  /** Returns a list of locations at distance 1 from transportLoc that cargo can move on. */
  public static ArrayList<XYCoord> findUnloadLocations(GameMap map, Unit transport, XYCoord moveLoc, Unit cargo)
  {
    ArrayList<XYCoord> locations = findLocationsInRange(map, moveLoc, 1);
    ArrayList<XYCoord> dropoffLocations = new ArrayList<XYCoord>();
    for( XYCoord loc : locations )
    {
      // Add any location that is empty and supports movement of the cargo unit.
      if( (map.isLocationEmpty(loc) || map.getLocation(loc).getResident() == transport)
          && cargo.model.movePower >= cargo.model.propulsion.getMoveCost(map.getEnvironment(loc.xCoord, loc.yCoord)) )
      {
        dropoffLocations.add(loc);
      }
    }
    return dropoffLocations;
  }

  /** Sets the highlight of all tiles in the provided list, and unsets the highlight on all others. */
  public static void highlightLocations(GameMap map, ArrayList<XYCoord> locations)
  {
    map.clearAllHighlights();
    for( XYCoord loc : locations )
    {
      map.getLocation(loc).setHighlight(true);
    }
  }

  /**
   * Sets the highlight for myGame.gameMap.getLocation(x, y) to true if unit can reach (x, y), and false otherwise.
   */
  public static ArrayList<XYCoord> findPossibleDestinations(Unit unit, GameMap gameMap)
  {
    ArrayList<XYCoord> reachableTiles = new ArrayList<XYCoord>();

    // set all locations to false/remaining move = 0
    int[][] movesLeftGrid = new int[gameMap.mapWidth][gameMap.mapHeight];
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        movesLeftGrid[i][j] = 0;
      }
    }
    // set up our search
    SearchNode root = new SearchNode(unit.x, unit.y);
    movesLeftGrid[unit.x][unit.y] = Math.min(unit.model.movePower, unit.fuel);
    Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(movesLeftGrid));
    searchQueue.add(root);
    // do search
    while (!searchQueue.isEmpty())
    {
      // pull out the next search node
      SearchNode currentNode = searchQueue.poll();
      // if the space is empty or holds the current unit, highlight
      Unit obstacle = gameMap.getLocation(currentNode.x, currentNode.y).getResident();
      if( obstacle == null || obstacle == unit || (obstacle.CO == unit.CO && obstacle.hasCargoSpace(unit.model.type)) )
      {
        reachableTiles.add(new XYCoord(currentNode.x, currentNode.y));
      }

      expandSearchNode(unit, gameMap, currentNode, searchQueue, movesLeftGrid);

      currentNode = null;
    }

    return reachableTiles;
  }

  /**
   * Determines whether the Location (x, y), can be added to the search queue.
   */
  private static boolean checkSpace(Unit unit, GameMap myMap, SearchNode currentNode, int x, int y, int[][] movesLeftGrid)
  {
    // if we're past the edges of the map
    if( x < 0 || y < 0 || x >= myMap.mapWidth || y >= myMap.mapHeight )
    {
      return false;
    }
    // if there is a unit in that space
    if( myMap.getLocation(x, y).getResident() != null )
    { // if that unit is an enemy
      if( unit.CO.isEnemy(myMap.getLocation(x, y).getResident().CO) )
      {
        return false;
      }
    }
    // if we have more movepower left than the other route there does
    boolean betterRoute = false;
    final int moveLeft = movesLeftGrid[currentNode.x][currentNode.y];
    int moveCost = findMoveCost(unit, x, y, myMap);
    if( moveLeft - moveCost >= movesLeftGrid[x][y] )
    {
      betterRoute = true;
      movesLeftGrid[x][y] = moveLeft - moveCost;
    }
    return betterRoute;
  }

  private static int findMoveCost(Unit unit, int x, int y, GameMap map)
  {
    return unit.model.propulsion.getMoveCost(map.getEnvironment(x, y));
  }

  public static boolean isPathValid(Unit unit, Path path, GameMap map)
  {
    if( (null == path) || (null == unit) )
    {
      return false;
    }

    //System.out.println("Checking path validity. Length: " + (path.getPathLength()-1));
    boolean canReach = true;

    // Make sure the first waypoint is under the Unit.
    if( path.getPathLength() <= 0 || path.getWaypoint(0).x != unit.x || path.getWaypoint(0).y != unit.y )
    {
      canReach = false;
    }

    int movePower = Math.min(unit.model.movePower, unit.fuel);

    // Index from 1 so we don't count the space the unit is on.
    for( int i = 1; canReach && (i < path.getPathLength()); ++i )
    {
      int wayX = path.getWaypoint(i).x;
      int wayY = path.getWaypoint(i).y;
      Location loc = map.getLocation(wayX, wayY);
      Unit resident = loc.getResident();

      movePower -= findMoveCost(unit, wayX, wayY, map);
      if( movePower < 0 || (resident != null && resident.CO.isEnemy(unit.CO)) )
      {
        canReach = false;
      }
    }

    return canReach;
  }

  /**
   * Calculate the shortest path for unit to take from its current location to map(x, y), and populate
   * the path parameter with those waypoints.
   * If no valid path is found, an empty Path will be returned.
   */
  public static Path findShortestPath(Unit unit, XYCoord destination, GameMap map)
  {
    return findShortestPath(unit, destination.xCoord, destination.yCoord, map);
  }

  /**
   * Calculate the shortest path for unit to take from its current location to map(x, y), and populate
   * the path parameter with those waypoints.
   * If no valid path is found, an empty Path will be returned.
   */
  public static Path findShortestPath(Unit unit, int x, int y, GameMap map)
  {
    if( null == unit || null == map )
    {
      return null;
    }

    Path aPath = new Path(100);
    if( map.mapWidth < unit.x || map.mapHeight < unit.y || unit.x < 0 || unit.y < 0 )
    {
      // Unit is not in a valid place. No path can be found.
      System.out.println("WARNING! Cannot find path for a unit that is not on the map.");
      aPath.clear();
      return aPath;
    }

    //System.out.println("Finding new path for " + unit.model.type + " from " + unit.x + ", " + unit.y + " to " + x + ", " + y);
    // Set all locations to false/remaining move = 0
    int[][] movesLeftGrid = new int[map.mapWidth][map.mapHeight];

    // Set up search parameters.
    SearchNode root = new SearchNode(unit.x, unit.y);
    movesLeftGrid[unit.x][unit.y] = Math.min(unit.model.movePower, unit.fuel);
    Queue<SearchNode> searchQueue = new java.util.PriorityQueue<SearchNode>(13, new SearchNodeComparator(movesLeftGrid, x, y));
    searchQueue.add(root);

    ArrayList<SearchNode> waypointList = new ArrayList<SearchNode>();

    // Find optimal route.
    while (!searchQueue.isEmpty())
    {
      // Retrieve the next search node.
      SearchNode currentNode = searchQueue.poll();

      // If this node is our destination, we are done.
      if( currentNode.x == x && currentNode.y == y )
      {
        // Add all of the points on the route to our waypoint list.
        while (currentNode.parent != null)
        {
          waypointList.add(currentNode);
          currentNode = currentNode.parent;
        }
        // Don't forget the starting node (no parent).
        waypointList.add(currentNode);
        break;
      }

      expandSearchNode(unit, map, currentNode, searchQueue, movesLeftGrid);

      currentNode = null;
    }

    // Clear and Populate the Path object.
    aPath.clear();
    // We added the waypoints to the list from end to beginning, so populate the Path in reverse order.
    if( !waypointList.isEmpty() )
    {
      for( int j = waypointList.size() - 1; j >= 0; --j )
      {
        //System.out.println("Waypoint " + waypointList.get(j).x + ", " + waypointList.get(j).y + " over " + map.getEnvironment(waypointList.get(j).x, waypointList.get(j).y).terrainType);
        aPath.addWaypoint(waypointList.get(j).x, waypointList.get(j).y);
      }
    }

    return aPath;
  }

  private static void expandSearchNode(Unit unit, GameMap map, SearchNode currentNode, Queue<SearchNode> searchQueue,
      int[][] movesLeftGrid)
  {
    // right
    if( checkSpace(unit, map, currentNode, currentNode.x + 1, currentNode.y, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x + 1, currentNode.y, currentNode));
    }
    // left
    if( checkSpace(unit, map, currentNode, currentNode.x - 1, currentNode.y, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x - 1, currentNode.y, currentNode));
    }
    // down
    if( checkSpace(unit, map, currentNode, currentNode.x, currentNode.y + 1, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x, currentNode.y + 1, currentNode));
    }
    // up
    if( checkSpace(unit, map, currentNode, currentNode.x, currentNode.y - 1, movesLeftGrid) )
    {
      searchQueue.add(new SearchNode(currentNode.x, currentNode.y - 1, currentNode));
    }
  }

  /**
   * Utility class used for pathfinding. Optionally holds a
   *   reference to a parent node for path reconstruction.
   */
  private static class SearchNode
  {
    public int x, y;
    public SearchNode parent;

    public SearchNode(int x, int y)
    {
      this(x, y, null);
    }

    public SearchNode(int x, int y, SearchNode parent)
    {
      this.x = x;
      this.y = y;
      this.parent = parent;
    }
  }

  /**
   * Compares SearchNodes based on the amount of movePower they possess, and optionally
   *   the remaining distance to a destination.
   */
  private static class SearchNodeComparator implements Comparator<SearchNode>
  {
    int[][] movesLeftGrid;
    private final boolean hasDestination;
    private int xDest;
    private int yDest;

    public SearchNodeComparator(int[][] movesLeftGrid)
    {
      this.movesLeftGrid = movesLeftGrid;
      hasDestination = false;
      xDest = 0;
      yDest = 0;
    }

    public SearchNodeComparator(int[][] movesLeftGrid, int x, int y)
    {
      this.movesLeftGrid = movesLeftGrid;
      hasDestination = true;
      xDest = x;
      yDest = y;
    }

    @Override
    public int compare(SearchNode o1, SearchNode o2)
    {
      int firstDist = Math.abs(o1.x - xDest) + Math.abs(o1.y - yDest);
      int secondDist = Math.abs(o2.x - xDest) + Math.abs(o2.y - yDest);

      int firstPow = movesLeftGrid[o1.x][o1.y] - ((hasDestination) ? firstDist : 0);
      int secondPow = movesLeftGrid[o2.x][o2.y] - ((hasDestination) ? secondDist : 0);
      return secondPow - firstPow;
    }
  }

  /**
   * Returns a list of all vacant industries a commander owns
   */
  public static ArrayList<XYCoord> findUsableProperties(Commander co, GameMap map)
  {
    ArrayList<XYCoord> industries = new ArrayList<XYCoord>();
    // We don't want to bother if we're trying to find nobody's properties
    if( null != co )
    {
      // Add all vacant, <co>-owned industries to the list
      for( Location loc : co.ownedProperties )
      {
        Unit resident = loc.getResident();
        // We only want industries we can act on, which means they need to be empty
        // TODO: maybe calculate whether the CO has enough money to buy something at this industry
        if( null == resident && loc.getOwner() == co )
        {
          if( co.getShoppingList(loc.getEnvironment().terrainType).size() > 0 )
          {
            industries.add(loc.getCoordinates());
          }
        }
      }
    }
    return industries;
  }

  static ArrayList<String> importFactions = new ArrayList<String>();
  public static String[] fetchFactionNames()
  {

//    importFactions.add(Commander.DEFAULT_SPRITE_KEY);

    return importFactions.toArray(new String[importFactions.size()]);
  }

  public static void paintAllFactions(String inPath, String outPath, boolean flip)
  {
    final File folder = new File(inPath);
    importFactions.add(Commander.DEFAULT_SPRITE_KEY);

    for( final File fileEntry : folder.listFiles() )
    {
      if( fileEntry.isDirectory() )
      {
        String faction = fileEntry.getName();
        System.out.println("Now painting faction:\n" + faction);
        // Faction names always have spaces
        if( faction.contains(" ") )
        {
          Matcher matcher = SpriteLibrary.factionNameToKey.matcher(faction);
          String facAbbrev;
          // if the faction is a real faction, pull out the first two initials, otherwise use the whole faction as key
          if( matcher.find() )
            facAbbrev = (("" + matcher.group(1).charAt(0)) + matcher.group(2).charAt(0)).toLowerCase();
          else
            facAbbrev = faction;

          importFactions.add(matcher.group(2));
          Set<Color> palette = paintFaction(inPath + faction, outPath + matcher.group(2), facAbbrev, flip);

//          try
//          {
//            String fileOutStr = (outPath + matcher.group(1) +".png");
//            ImageIO.write(SpriteLibrary.createPaletteImage(palette, 1, 1), "png", new File(fileOutStr));
//          }
//          catch (IOException e)
//          {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//          }
        }
      }
    }
  }
  
  public static Set<Color> paintFaction(String facInPath, String facOutPath, String facAbbrev, boolean flip)
  {
    final File folder = new File(facInPath);
    new File(facOutPath).mkdirs();

    Set<Color> palette = new HashSet<>();

    ArrayList<Color[]> allColors = new ArrayList<>();
    allColors.add( new Color[] // OS
        {
            new Color(128,   0,  16),
            new Color(192,   0,   0),
            new Color(240,   0,   8),
            new Color(248,  88,   0),
            new Color(248, 152, 104),
            new Color(248, 184, 120),
        });
    allColors.add( new Color[] // AR
        {
            new Color( 49,  63,   7),
            new Color( 82, 104,  13),
            new Color( 97, 124,  14),
            new Color(134, 172,  20),
            new Color(158, 196,  43),
            new Color(192, 230,  76),
        });
    allColors.add( new Color[] // AB
        {
            new Color(103,  56,   1),
            new Color(173,  94,   0),
            new Color(231, 135,  22),
            new Color(252, 163,  57),
            new Color(254, 192, 120),
            new Color(255, 222, 182),
        });
    allColors.add( new Color[] // BH
        {
            new Color( 24,   8,  40),
            new Color( 72,  40, 120),
            new Color( 96,  56, 160),
            new Color(152, 152, 136),
            new Color(192, 192, 168),
            new Color(216, 224, 216),
        });
    allColors.add( new Color[] // BM
        {
            new Color(  0,  24, 168),
            new Color(  0, 104, 232),
            new Color(  0, 152, 248),
            new Color( 72, 200, 248),
            new Color(104, 224, 248),
            new Color(184, 240, 248),
        });
    allColors.add( new Color[] // BD
        {
            new Color( 81,  46,  12),
            new Color(148,  88,  28),
            new Color(188, 130,  72),
            new Color(181, 141, 100),
            new Color(220, 180, 141),
            new Color(252, 217, 183),
        });
    allColors.add( new Color[] // CI
        {
            new Color( 11,  32, 112),
            new Color( 22,  48, 150),
            new Color( 35,  66, 186),
            new Color( 69, 100, 219),
            new Color(104, 129, 228),
            new Color(144, 163, 237),
        });
    allColors.add( new Color[] // GE
        {
            new Color(  0,  56,   0),
            new Color(  0, 144,   0),
            new Color(  0, 192,  16),
            new Color( 48, 248,  48),
            new Color( 96, 248,  72),
            new Color(216, 248, 200),
        });
    allColors.add( new Color[] // GS
        {
            new Color( 48,  48,  48),
            new Color( 64,  64,  64),
            new Color( 93,  93,  93),
            new Color(124, 124, 124),
            new Color(172, 172, 172),
            new Color(184, 183, 182),
        });
    allColors.add( new Color[] // JS
        {
            new Color( 49,  53,  46),
            new Color( 77,  84,  72),
            new Color(133, 146, 123),
            new Color(160, 175, 149),
            new Color(196, 215, 180),
            new Color(232, 255, 214),
        });
    // OS
    allColors.add( new Color[] // RF / Maroon
        {
            new Color(123,  25,   0),
            new Color(165,  29,   8),
            new Color(193,  70,  61),
            new Color(196, 102, 117),
            new Color(202, 154, 155),
            new Color(226, 201, 196),
        });
    allColors.add( new Color[] // PC
        {
            new Color(102,   0,  51),
            new Color(204,   0, 153),
            new Color(255,  51, 204),
            new Color(255, 102, 204),
            new Color(255, 153, 204),
            new Color(248, 208, 200),
        });
    allColors.add( new Color[] // PL
        {
            new Color( 69,   1, 102),
            new Color(101,   6, 149),
            new Color(205,   0, 254),
            new Color(205, 103, 253),
            new Color(204, 153, 252),
            new Color(255, 204, 253),
        });
    allColors.add( new Color[] // TG
        {
            new Color(  3,  59,  54),
            new Color( 11,  90,  81),
            new Color( 32, 162, 152),
            new Color( 58, 207, 193),
            new Color(120, 224, 214),
            new Color(187, 240, 232),
        });
    allColors.add( new Color[] // WN
        {
            new Color(136,   0,  21),
            new Color(200, 117, 105),
            new Color(226, 174, 147),
            new Color(242, 227, 209),
            new Color(249, 239, 219),
            new Color(250, 242, 221),
        });
    allColors.add( new Color[] // WN foot
        {
            new Color(146,  60,  53),
            new Color(193,  95,  87),
            new Color(224, 174, 148),
            new Color(239, 228, 208),
            new Color(249, 239, 219),
            new Color(249, 251, 248),
        });
    allColors.add( new Color[] // WN foot 2
        {
            new Color(146,  60,  53),
            new Color(202, 116, 106),
            new Color(224, 174, 148),
            new Color(239, 228, 208),
            new Color(249, 239, 219),
            new Color(249, 251, 248),
        });
    allColors.add( new Color[] // YC
        {
            new Color( 80,  64,   0),
            new Color(184, 128,   0),
            new Color(208, 128,   0),
            new Color(248, 192,   0),
            new Color(248, 248,  64),
            new Color(248, 248, 152),
        });

    for( final File fileEntry : folder.listFiles() )
    {
      String filestr = fileEntry.getName();
      if( !fileEntry.isDirectory() && filestr.contains(".gif") )
      {
        String unitName = standardizeID(
            filestr
            .replaceFirst(facAbbrev, "")
            .replaceFirst("_mup",   "_mapmovenorth")
            .replaceFirst("_mdown", "_mapmovesouth")
            .replaceFirst("_mside", "_mapmoveeast")
            .replace(".gif", "")
            );
        if( !unitName.contains("_map") )
          unitName += "_map";
        System.out.println("  " + unitName);
        ImageFrame[] frames = SpriteLibrary.loadAnimation(facInPath+"/"+filestr);

        for( Color[] teamColors : allColors )
          colorize(frames, teamColors, SpriteLibrary.defaultMapColors);

        try
        {
          BufferedImage joinedImage = SpriteLibrary.joinBufferedImage(frames, frames[0].getImage().getWidth(), frames[0].getImage().getHeight(), flip);
          String fileOutStr = (facOutPath + "/" + unitName + ".png");
          ImageIO.write(
              joinedImage,
              "png", new File(fileOutStr));
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    return palette;
  }

  /**
   * For every image contained in this sprite, change each pixel with a value in oldColors to the corresponding value in newColors.
   */
  public static void colorize(ImageFrame[] spriteImages, Color[] oldColors, Color[] newColors)
  {
    for( ImageFrame frame : spriteImages )
    {
      BufferedImage bi = frame.getImage();
      for( int x = 0; x < bi.getWidth(); ++x )
      {
        for( int y = 0; y < bi.getHeight(); ++y )
        {
          int colorValue = bi.getRGB(x, y);
          for( int c = 0; c < oldColors.length; ++c )
          {
            if( oldColors[c].getRGB() == colorValue )
            {
              bi.setRGB(x, y, newColors[c].getRGB());
            }
          }
        }
      }
    }
  }

  public static String standardizeID(String input)
  {
    return input.toLowerCase().replaceAll(" ", "_").replaceAll("-", "_");
  }
}
