package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Terrain.TerrainType;
import UI.UIUtils;

/**
 * Responsible for storing and organizing all image data associated with a specific map tile type, and drawing the
 * correct terrain subimage based on the map context.
 * TerrainSpriteSet is recursive - it may contain additional TerrainSpriteSet members which define transition
 * images for adjacent tiles of a different terrain type. For example, the SEA TerrainSpriteSet will contain a
 * GRASS TerrainSpriteSet which defines an overlay image to be drawn when a SEA tile is adjacent to a GRASS tile.
 */
public class TerrainSpriteSet
{
  /** This string tells us whence to load our sprites. */
  private final String resourceTemplateString;
  /** Map of Sprites by Weather, to allow for variations of the tile type. */
  private Map<Weathers, ArrayList<Sprite> > terrainSprites;
  private Color myTeamColor;
  private ArrayList<Sprite> fogTerrainSprites;
  private static Color FOG_COLOR = null;
  private Color myFogColor = null;
  private ArrayList<TerrainSpriteSet> tileTransitions;
  private boolean isTransition = false;
  public final TerrainType myTerrainType;
  private ArrayList<TerrainType> myTerrainAffinities;
  private ArrayList<TerrainType> myTerrainNonAffinities;

  private int spriteWidth;
  private int spriteHeight;
  private int drawOffsetx; // Large sprites (terrain objects) must be drawn a tile up and over to look right.
  private int drawOffsety;

  // Keys to the sprite array - logical OR the four cardinals to get the corresponding sprite index.
  // The four diagonal directions are just the index for that corner transition.
  private static final short NORTH = 0x1;
  private static final short EAST = 0x2;
  private static final short SOUTH = 0x4;
  private static final short WEST = 0x8;
  private static final short NW = 16;
  private static final short NE = 17;
  private static final short SE = 18;
  private static final short SW = 19;

  private static Map<TerrainType, TerrainType> terrainBases = null;
  private static Set<TerrainType> terrainObjects = null;

  private static Color backupSnowOverlayColor = new Color(240, 243, 219, 100);
  private static Color backupRainOverlayColor = new Color(10, 35, 73, 100);
  private static Color backupSandOverlayColor = new Color(243, 213, 85, 100);
  private static Map<Weathers, Color> backupOverlayColors;

  private static boolean logDetails = false;

  public TerrainSpriteSet(TerrainType terrain, String imageLocationTemplate, int spriteWidth, int spriteHeight)
  {
    this(terrain, imageLocationTemplate, spriteWidth, spriteHeight, false);
  }

  public TerrainSpriteSet(TerrainType terrain, String imageLocationTemplate, int spriteWidth, int spriteHeight, boolean isTransitionTileset)
  {
    resourceTemplateString = imageLocationTemplate;
    myTerrainType = terrain;
    terrainSprites = new HashMap<Weathers, ArrayList<Sprite> >();
    tileTransitions = new ArrayList<TerrainSpriteSet>();
    isTransition = isTransitionTileset;
    myTerrainAffinities = new ArrayList<TerrainType>();
    myTerrainNonAffinities = new ArrayList<TerrainType>();

    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;

    // We assume here that all sprites are sized in multiples of the base sprite size.
    drawOffsetx = spriteWidth / SpriteLibrary.baseSpriteSize - 1;
    drawOffsety = spriteHeight / SpriteLibrary.baseSpriteSize - 1;

    if( null == backupOverlayColors )
    {
      backupOverlayColors = new HashMap<Weathers, Color>();
      backupOverlayColors.put(Weathers.SNOW, backupSnowOverlayColor);
      backupOverlayColors.put(Weathers.RAIN, backupRainOverlayColor);
      backupOverlayColors.put(Weathers.SANDSTORM, backupSandOverlayColor);
    }
  }

  private ArrayList<Sprite> getSpritesByWeather(Weathers weather)
  {
    // If we already built the sprite, just return it.
    ArrayList<Sprite> sprites = terrainSprites.get(weather);
    if( sprites != null ) return sprites;

    // If not, try to load the file using the resource template string from SpriteLibrary.
    String spriteSheetFilename = String.format(resourceTemplateString, myTerrainType.toString().toLowerCase(), weather.toString().toLowerCase());
    BufferedImage spriteSheet = SpriteLibrary.loadSpriteSheetFile(spriteSheetFilename);

    // If we failed to load the weather-specific version, Just get the clear version (recursively!) and mask over it.
    if( null == spriteSheet && (weather != Weathers.CLEAR) )
    {
      ArrayList<Sprite> clearSprites = getSpritesByWeather(Weathers.CLEAR);
      ArrayList<Sprite> weatherOverlays = buildSpriteMasks(backupOverlayColors.get(weather));
      ArrayList<Sprite> weatherSprites = new ArrayList<Sprite>();
      for( int i = 0; i < clearSprites.size(); ++i )
      {
        // Copy the clear sprite, and get the relevant overlay.
        Sprite sprite = new Sprite(clearSprites.get(i));
        Sprite overlay = weatherOverlays.get(i);

        // If this is a capturable location and we are owned, colorize before applying the weather overlay.
        if( myTerrainType.isCapturable() && myTeamColor != null )
        {
          sprite.colorize(UIUtils.defaultMapColors, UIUtils.getBuildingColors(myTeamColor).paletteColors);
        }

        // Draw our faux weather effect.
        for( int f = 0; f < sprite.numFrames(); ++f )
        {
          BufferedImage img = sprite.getFrame(f);
          img.getGraphics().drawImage(overlay.getFrame(f), 0, 0, null);
        }

        // Add our new sprite to the weather list.
        weatherSprites.add(sprite);
      }
      // Put our shiny new sprites in the map for future reference.
      terrainSprites.put(weather, weatherSprites);
      return weatherSprites;
    }

    // Cut the sprite sheet into its individual frames.
    ArrayList<Sprite> spriteArray = new ArrayList<Sprite>();
    if( spriteSheet == null )
    {
      System.out.println("WARNING! Continuing with placeholder images.");
      // Create a new blank sprite image of the desired size.
      spriteArray.add(new Sprite(null, spriteWidth, spriteHeight));
      terrainSprites.put(weather, spriteArray);
    }
    else
    {
      // Cut the sprite-sheet up and populate spriteArray.
      int xOffset = 0;
      int yOffset = 0;
      int spriteNum = 0;
      int maxSpriteIndex = (NORTH | EAST | SOUTH | WEST) + 4; // 20 possible sprites per terrain type (0-15, plus four corners).

      // Loop through the sprite sheet and pull out all frames (first row), and all variations if provided (additional rows).
      try
      {
        // Loop until we get as many sprites as we expect or run out of runway.
        while (spriteNum <= maxSpriteIndex && ((spriteNum + 1) * spriteWidth <= spriteSheet.getWidth()))
        {
          spriteArray.add(new Sprite(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight)));
          xOffset += spriteWidth;
          spriteNum++;
        }

        if( spriteNum != 1 && spriteNum != 16 && spriteNum != 20 )
        {
          System.out.println("WARNING! TerrainSpriteSet detected a malformed sprite sheet!");
          System.out.println("WARNING!   Found " + spriteNum + " " + spriteWidth + "x" + spriteHeight + " sprites in a "
              + spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + " spritesheet");
          System.out.println("WARNING!   (There should be 1, 16, or 20 sprites in a terrain sprite sheet)");
          throw new RasterFormatException("Sprite sheet " + spriteSheetFilename + " is too small! " + myTerrainType + " sprites are " + spriteWidth + "x" + spriteHeight);
        }

        maxSpriteIndex = spriteNum - 1; // However many sprites we found, we won't find more than that on a second horizontal pass.

        // If this sprite has more vertical space, pull in alternate versions of the existing terrain tiles.
        while (yOffset + spriteHeight < spriteSheet.getHeight())
        {
          xOffset = 0;
          spriteNum = 0;

          while (spriteNum <= maxSpriteIndex && ((spriteNum + 1) * spriteWidth <= spriteSheet.getWidth()))
          {
            spriteArray.get(spriteNum).addFrame(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight));
            xOffset += spriteWidth;
            spriteNum++;
          }

          yOffset += spriteHeight;
        }
      }
      catch (RasterFormatException RFE) // This occurs if we go off the end of the sprite sheet.
      {
        System.out.println("WARNING! RasterFormatException while loading Sprite. Attempting to continue.");

        spriteArray.clear(); // Clear this in case of partially-created data.

        // Make a single blank frame of the specified size.
        spriteArray.add(new Sprite(null, spriteWidth, spriteHeight));
      }

      // If this is a capturable location and we are owned, colorize before applying the weather overlay.
      if( myTerrainType.isCapturable() && myTeamColor != null )
      {
        for( Sprite sprite : spriteArray )
          sprite.colorize(UIUtils.defaultMapColors, UIUtils.getBuildingColors(myTeamColor).paletteColors);
      }

      terrainSprites.put(weather, spriteArray);
      if( logDetails )
        System.out.println("INFO: Loaded " + (isTransition? "transition " : "") + "sprites for " + myTerrainType + ", " + weather + ".");
    } // spriteSheet != null

    return spriteArray;
  }

  /**
   * Returns the Sprite for the first variation of this terrain type, for clear weather.
   */
  public Sprite getTerrainSprite()
  {
    return getSpritesByWeather(Weathers.CLEAR).get(0);
  }

  /**
   * Tiles from 'spriteSheet' will be drawn on top of tiles from this sprite set when a tile is adjacent to one or more tiles
   * of type otherTerrain. This allows us to define smoother terrain transitions.
   */
  public void addTileTransition(TerrainType otherTerrain, String fileLocationTemplateString, int spriteWidth, int spriteHeight)
  {
    tileTransitions.add(new TerrainSpriteSet(otherTerrain, fileLocationTemplateString, spriteWidth, spriteHeight, true));
  }

  /** Declares another terrain type to be "like" this one - it will be treated as the same type as this when tiling.
      This allows e.g. letting roads and bridges merge seamlessly, and connecting roads to properties.               */
  public void addTerrainAffinity(TerrainType otherTerrain)
  {
    myTerrainAffinities.add(otherTerrain);
  }

  /** Declares another terrain type to be "unlike" this one - it will be treated as different than this type when tiling,
      even if its base type has an affinity with this type (e.g. BRIDGE should tile with GRASS, but not with RIVER). */
  public void denyTerrainAffinity(TerrainType otherTerrain)
  {
    myTerrainNonAffinities.add(otherTerrain);
  }

  /**
   * Draws the terrain at the indicated location, accounting for any defined tile transitions.
   * Does not draw terrain objects (buildings, trees, mountains, etc).
   */
  public void drawTerrain(Graphics g, GameMap map, int x, int y, boolean drawFog)
  {
    drawTile(g, map, x, y, drawFog, false);
  }

  /**
   * Draws any terrain object at the indicated location, accounting for any defined tile transitions.
   * Does not draw the underlying terrain (grass, water, etc).
   */
  public void drawTerrainObject(Graphics g, GameMap map, int x, int y, boolean drawFog)
  {
    drawTile(g, map, x, y, drawFog, true);
  }

  private void drawTile(Graphics g, GameMap map, int x, int y, boolean drawFog, boolean shouldDrawTerrainObject)
  {
    // Draw the base tile type, if needed. It's needed if we are not the base type, and if we are not drawing
    //   a terrain object. If this object is holding transition tiles, we also don't want to (re)draw the base type.
    TerrainType baseTerrainType = getBaseTerrainType(myTerrainType);
    if( baseTerrainType != myTerrainType && !shouldDrawTerrainObject && !isTransition )
    {
      if( logDetails )
        System.out.println("Drawing " + baseTerrainType + " as base of " + myTerrainType);
      TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(baseTerrainType);
      spriteSet.drawTile(g, map, x, y, drawFog, shouldDrawTerrainObject);
    }

    // Figure out if we need to draw this tile, based on this tile type and whether the caller
    //   wishes to draw terrain objects or base terrain.
    if( (shouldDrawTerrainObject && isTerrainObject(myTerrainType))
        || (!shouldDrawTerrainObject && !isTerrainObject(myTerrainType)) )
    {
      // Either: we are only drawing terrain objects, and this tile type does represent a terrain object,
      //     or: we are only drawing base terrain, and this tile type represents base terrain.
      int tileSize = SpriteLibrary.baseSpriteSize;
      int variation = (x + 1) * (y) + x; // Used to vary the specific sprite version drawn at each place in a repeatable way.

      // Get the tile we want to draw, based on weather.
      Weathers currentWeather = map.getEnvironment(x, y).weatherType;
      ArrayList<Sprite> clearSprites = getSpritesByWeather(currentWeather);

      // Figure out how to handle map-edge transitions; we only want to assume the "adjacent" off-map tile has the same type
      //   as the current tile if the current tile type matches the TerrainSpriteSet type (that is, this TerrainSpriteSet does
      //   not define a tile transition - it's the sprite for the actual terrain type of the current tile). This makes it so
      //   that GRASS or SEA will continue off the edge of the map, and we don't get strange cliff walls at the edge of the map.
      boolean assumeSameTileType = myTerrainType == map.getEnvironment(x, y).terrainType;
      short dirIndex = getTileImageIndex(map, x, y, assumeSameTileType);

      // Draw the current tile.
      BufferedImage frame = clearSprites.get(dirIndex).getFrame(variation);
      if( dirIndex != 0 || !isTransition ) // Don't bother drawing transition tiles with no transitions.
      {
        g.drawImage(frame, (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize, frame.getWidth(), frame.getHeight(), null);
      }

      // Handle drawing corner-case tile variations if needed.
      if( clearSprites.size() == 20 )
      {
        // If we didn't have a N or W transition, then look in the NW position
        if( (dirIndex & (NORTH | WEST)) == 0 && checkTileType(map, x - 1, y - 1, assumeSameTileType) )
        {
          g.drawImage(clearSprites.get(NW).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth(), frame.getHeight(), null);
        }
        if( (dirIndex & (NORTH | EAST)) == 0 && checkTileType(map, x + 1, y - 1, assumeSameTileType) )
        {
          g.drawImage(clearSprites.get(NE).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth(), frame.getHeight(), null);
        }
        if( (dirIndex & (SOUTH | EAST)) == 0 && checkTileType(map, x + 1, y + 1, assumeSameTileType) )
        {
          g.drawImage(clearSprites.get(SE).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth(), frame.getHeight(), null);
        }
        if( (dirIndex & (SOUTH | WEST)) == 0 && checkTileType(map, x - 1, y + 1, assumeSameTileType) )
        {
          g.drawImage(clearSprites.get(SW).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth(), frame.getHeight(), null);
        }
      }

      // Draw any tile transitions that are needed.
      for( TerrainSpriteSet tt : tileTransitions )
      {
        if( logDetails )
          System.out.println("Drawing transition from " + tt.myTerrainType + " onto " + myTerrainType);
        tt.drawTerrain(g, map, x, y, false);
      }

      // Draw the fog overlay if requested.
      if( drawFog && !isTransition )
      {
        if( (null == fogTerrainSprites) || (FOG_COLOR != myFogColor) )
        {
          fogTerrainSprites = buildSpriteMasks(FOG_COLOR);
          myFogColor = FOG_COLOR;
        }
        BufferedImage fogFrame = fogTerrainSprites.get(dirIndex).getFrame(variation);
        g.drawImage(fogFrame, (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize, frame.getWidth(), frame.getHeight(), null);
      }
    }
  }

  /**
   * Given a map, a location, and rules for how to treat off-map adjacent spaces, this function
   * returns the index of the sprite subimage that should be chosen when drawing this tile.
   * @param map
   * @param x
   * @param y
   * @param assumeSameTileType
   * @return
   */
  private short getTileImageIndex(GameMap map, int x, int y, boolean assumeSameTileType)
  {
    // Get the list of CLEAR-weather sprites, as they are guaranteed to define our transitions.
    ArrayList<Sprite> clearSprites = getSpritesByWeather(Weathers.CLEAR);

    short dirIndex = 0;
    if( clearSprites.size() > 1 ) // We expect the size to be either 1, 16, or 20.
    {
      // Figure out which neighboring tiles have the same terrain type as this one.
      dirIndex |= checkTileType(map, x, y - 1, assumeSameTileType) ? NORTH : 0;
      dirIndex |= checkTileType(map, x + 1, y, assumeSameTileType) ? EAST : 0;
      dirIndex |= checkTileType(map, x, y + 1, assumeSameTileType) ? SOUTH : 0;
      dirIndex |= checkTileType(map, x - 1, y, assumeSameTileType) ? WEST : 0;
    }

    // Normalize the index value just in case.
    if( dirIndex >= clearSprites.size() )
    {
      // We could print a warning here, but there should have been one when the sprites were loaded.
      // At this point we are just preventing an ArrayOutOfBoundsException.
      dirIndex = (short) (dirIndex % clearSprites.size());
    }
    return dirIndex;
  }

  /** Return true if this terrain type takes up more than one tile when drawn. */
  private boolean isTerrainObject(TerrainType terrainType)
  {
    if( null == terrainObjects )
    {
      terrainObjects = new HashSet<TerrainType>();
      terrainObjects.add(TerrainType.BUNKER);
      terrainObjects.add(TerrainType.CITY);
      terrainObjects.add(TerrainType.FACTORY);
      terrainObjects.add(TerrainType.FOREST);
      terrainObjects.add(TerrainType.AIRPORT);
      terrainObjects.add(TerrainType.SEAPORT);
      terrainObjects.add(TerrainType.HEADQUARTERS);
      terrainObjects.add(TerrainType.LAB);
      terrainObjects.add(TerrainType.MOUNTAIN);
      terrainObjects.add(TerrainType.PILLAR);
    }
    return terrainObjects.contains(terrainType);
  }

  /**
   * If position (x, y) is a valid location:
   *   Return true if (x, y) has myTerrainType terrain or is in the affinity list, else return false;
   * 
   * If position (x, y) is not a valid location (out of bounds): Return the value of assumeTrue. This has
   *   the effect of allowing us to assume that tiles out of sight are whatever terrain we prefer - enabling
   *   us to draw roads that go off the map, etc, but keep it from looking like there is always land across
   *   the water at the edge of the map due to unwanted cliff-face transitions.
   */
  private boolean checkTileType(GameMap map, int x, int y, boolean assumeTrue)
  {
    boolean terrainTypesMatch = false;

    if( map.isLocationValid(x, y) )
    {
      TerrainType otherTerrain = map.getEnvironment(x, y).terrainType;
      TerrainType otherBase = getBaseTerrainType(otherTerrain);

      if( ((otherTerrain == myTerrainType) // Terrain types match.
          || (otherBase == myTerrainType) // Terrain bases match.
          || myTerrainAffinities.contains(otherTerrain) // Affinity with other tile type.
          || myTerrainAffinities.contains(otherBase)) // Affinity with other tile base type.
          && !myTerrainNonAffinities.contains(otherTerrain) // No explicit denial of affinity.
          )
      {
        terrainTypesMatch = true;
      }
    }
    else
    {
      // Invalid location; the provided value determines if we assume it would match.
      terrainTypesMatch = assumeTrue;
    }

    return terrainTypesMatch;
  }

  /**
   * Determines the base terrain type for the provided environment terrain type.
   * For example, FOREST is a tile type, but the trees sit on a plain, so for drawing
   * purposes (esp. terrain transitions), the base tile type of FOREST is actually GRASS.
   */
  private static TerrainType getBaseTerrainType(TerrainType terrain)
  {
    if( null == terrainBases )
    {
      terrainBases = new HashMap<TerrainType, TerrainType>();
      terrainBases.put(TerrainType.CITY, TerrainType.GRASS);
      terrainBases.put(TerrainType.DUNES, TerrainType.GRASS);
      terrainBases.put(TerrainType.FACTORY, TerrainType.GRASS);
      terrainBases.put(TerrainType.AIRPORT, TerrainType.GRASS);
      terrainBases.put(TerrainType.FOREST, TerrainType.GRASS);
      terrainBases.put(TerrainType.HEADQUARTERS, TerrainType.GRASS);
      terrainBases.put(TerrainType.LAB, TerrainType.GRASS);
      terrainBases.put(TerrainType.MOUNTAIN, TerrainType.GRASS);
      terrainBases.put(TerrainType.GRASS, TerrainType.GRASS);
      terrainBases.put(TerrainType.RIVER, TerrainType.GRASS);
      terrainBases.put(TerrainType.ROAD, TerrainType.GRASS);
      terrainBases.put(TerrainType.PILLAR, TerrainType.GRASS);
      terrainBases.put(TerrainType.BUNKER, TerrainType.GRASS);

      terrainBases.put(TerrainType.BRIDGE, TerrainType.SHOAL);
      terrainBases.put(TerrainType.SEAPORT, TerrainType.SHOAL);
      terrainBases.put(TerrainType.SHOAL, TerrainType.SHOAL);

      terrainBases.put(TerrainType.REEF, TerrainType.SEA);
      terrainBases.put(TerrainType.SEA, TerrainType.SEA);

      if( TerrainType.TerrainTypeList.size() != terrainBases.size())
      {
        throw new RuntimeException("TerrainSpriteSet.terrainBases does not align with TerrainType.TerrainTypeList!");
      }
    }

    return terrainBases.get(terrain);
  }

  /** Set the color used to colorize capturable buildings. */
  public void setTeamColor(Color color)
  {
    myTeamColor = color;
  }

  /** Change the color used by all TerrainSpriteSets. */
  public static void setFogColor(Color color)
  {
    FOG_COLOR = color;
  }

  /** Build a set of fog-colored image masks so we can draw precise fog effects. */
  private ArrayList<Sprite> buildSpriteMasks(Color maskColor)
  {
    ArrayList<Sprite> spriteMasks = new ArrayList<Sprite>();
    for( Sprite s : getSpritesByWeather(Weathers.CLEAR) )
    {
      Sprite ns = new Sprite(s);
      ns.convertToMask(maskColor);
      spriteMasks.add(ns);
    }
    return spriteMasks;
  }
}
