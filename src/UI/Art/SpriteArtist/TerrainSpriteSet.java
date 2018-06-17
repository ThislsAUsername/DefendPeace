package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;
import java.util.EnumSet;

import Terrain.Environment;
import Terrain.GameMap;

/**
 * Responsible for storing and organizing all image data associated with a specific map tile type, and drawing the
 * correct terrain subimage based on the map context.
 * TerrainSpriteSet is recursive - it may contain additional TerrainSpriteSet members which define transition
 * images for adjacent tiles of a different terrain type. For example, the SEA TerrainSpriteSet will contain a
 * GRASS TerrainSpriteSet which defines an overlay image to be drawn when a SEA tile is adjacent to a GRASS tile.
 */
public class TerrainSpriteSet
{
  /** List of Sprites, to allow for variations of the tile type. */
  private ArrayList<Sprite> terrainSprites;
  private ArrayList<TerrainSpriteSet> tileTransitions;
  private boolean isTransition = false;
  public final Environment.Terrains myTerrainType;
  private EnumSet<Environment.Terrains> myTerrainAffinities;

  int drawOffsetx;
  int drawOffsety;

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

  public TerrainSpriteSet(Environment.Terrains terrainType, BufferedImage spriteSheet, int spriteWidth, int spriteHeight)
  {
    this(terrainType, spriteSheet, spriteWidth, spriteHeight, false);
  }

  public TerrainSpriteSet(Environment.Terrains terrainType, BufferedImage spriteSheet, int spriteWidth, int spriteHeight, boolean isTransitionTileset)
  {
    myTerrainType = terrainType;
    terrainSprites = new ArrayList<Sprite>();
    tileTransitions = new ArrayList<TerrainSpriteSet>();
    isTransition = isTransitionTileset;
    myTerrainAffinities = EnumSet.noneOf(Environment.Terrains.class);

    // We assume here that all sprites are sized in multiples of the base sprite size.
    drawOffsetx = spriteWidth / SpriteLibrary.baseSpriteSize - 1;
    drawOffsety = spriteHeight / SpriteLibrary.baseSpriteSize - 1;

    if( spriteSheet == null )
    {
      System.out.println("WARNING! Continuing with placeholder images.");
      // Just make a single frame of the specified size.
      drawOffsetx = 0;
      drawOffsety = 0;
      // Create a new blank sprite image of the desired size.
      terrainSprites.add(new Sprite(null, SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize));
    }
    else
    {
      // Cut the sprite-sheet up and populate terrainSprites.
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
          terrainSprites.add(new Sprite(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight)));
          xOffset += spriteWidth;
          spriteNum++;
        }

        if( spriteNum != 1 && spriteNum != 16 && spriteNum != 20 )
        {
          System.out.println("WARNING! TerrainSpriteSet detected a malformed sprite sheet!");
          System.out.println("WARNING!   Found " + spriteNum + " " + spriteWidth + "x" + spriteHeight + " sprites in a "
              + spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + " spritesheet");
          System.out.println("WARNING!   (There should be 1, 16, or 20 sprites in a terrain sprite sheet)");
        }

        maxSpriteIndex = spriteNum - 1; // However many sprites we found, we won't find more than that on a second horizontal pass.

        // If this sprite has more vertical space, pull in alternate versions of the existing terrain tiles.
        while (yOffset + spriteHeight <= spriteSheet.getHeight())
        {
          xOffset = 0;
          spriteNum = 0;

          while (spriteNum <= maxSpriteIndex && ((spriteNum + 1) * spriteWidth <= spriteSheet.getWidth()))
          {
            terrainSprites.get(spriteNum).addFrame(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight));
            xOffset += spriteWidth;
            spriteNum++;
          }

          yOffset += spriteHeight;
        }
      }
      catch (RasterFormatException RFE) // This occurs if we go off the end of the sprite sheet.
      {
        System.out.println("WARNING! RasterFormatException while loading Sprite. Attempting to continue.");

        terrainSprites.clear(); // Clear this in case of partially-created data.

        // Make a single blank frame of the specified size.
        terrainSprites.add(new Sprite(null, spriteWidth, spriteHeight));
      }
    } // spriteSheet != null
    System.out.println("INFO: Created TerrainSpriteSheet with " + terrainSprites.size() + " sprites.");
  }

  /**
   * Returns the Sprite for the first variation of this terrain type.
   */
  public Sprite getTerrainSprite()
  {
    return terrainSprites.get(0);
  }

  /**
   * Tiles from 'spriteSheet' will be drawn on top of tiles from this sprite set when a tile is adjacent to one or more tiles
   * of type otherTerrain. This allows us to define smoother terrain transitions.
   */
  public void addTileTransition(Environment.Terrains otherTerrain, BufferedImage spriteSheet, int spriteWidth, int spriteHeight)
  {
    tileTransitions.add(new TerrainSpriteSet(otherTerrain, spriteSheet, spriteWidth, spriteHeight, true));
  }

  public void colorize(Color[] oldColors, Color[] newColors)
  {
    for( Sprite s : terrainSprites )
    {
      s.colorize(oldColors, newColors);
    }
  }

  /** Declares another terrain type to be "like" this one - it will be treated as the same type as this when tiling.
      This allows e.g. letting roads and bridges merge seamlessly, and connecting roads to properties.               */
  public void addTerrainAffinity(Environment.Terrains otherTerrain)
  {
    myTerrainAffinities.add(otherTerrain);
  }

  /**
   * Draws the terrain at the indicated location, accounting for any defined tile transitions.
   * Does not draw terrain objects (buildings, trees, mountains, etc).
   */
  public void drawTerrain(Graphics g, GameMap map, int x, int y, int scale)
  {
    drawTile(g, map, x, y, scale, false);
  }

  /**
   * Draws any terrain object at the indicated location, accounting for any defined tile transitions.
   * Does not draw the underlying terrain (grass, water, etc).
   */
  public void drawTerrainObject(Graphics g, GameMap map, int x, int y, int scale)
  {
    drawTile(g, map, x, y, scale, true);
  }

  private void drawTile(Graphics g, GameMap map, int x, int y, int scale, boolean shouldDrawTerrainObject)
  {
    // Draw the base tile type, if needed. It's needed if we are not the base type, and if we are not drawing
    //   a terrain object. If this object is holding transition tiles, we also don't want to (re)draw the base type.
    Environment.Terrains baseTerrainType = getBaseTerrainType(myTerrainType);
    if( baseTerrainType != myTerrainType && !shouldDrawTerrainObject && !isTransition )
    {
      System.out.println("Drawing " + baseTerrainType + " as base of " + myTerrainType);
      TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(baseTerrainType);
      spriteSet.drawTile(g, map, x, y, scale, shouldDrawTerrainObject);
    }

    // Figure out if we need to draw this tile, based on this tile type and whether the caller
    //   wishes to draw terrain objects or base terrain.
    if( (shouldDrawTerrainObject && isTerrainObject(myTerrainType))
        || (!shouldDrawTerrainObject && !isTerrainObject(myTerrainType)) )
    {
      // Either: we are only drawing terrain objects, and this tile type does represent a terrain object,
      //     or: we are only drawing base terrain, and this tile type represents base terrain.
      int tileSize = SpriteLibrary.baseSpriteSize * scale;
      int variation = (x + 1) * (y) + x; // Used to vary the specific sprite version drawn at each place in a repeatable way.

      // Figure out how to handle map-edge transitions; we only want to assume the "adjacent" off-map tile has the same type
      //   as the current tile if the current tile type matches the TerrainSpriteSet type (that is, this TerrainSpriteSet does
      //   not define a tile transition - it's the sprite for the actual terrain type of the current tile). This makes it so
      //   that GRASS or SEA will continue off the edge of the map, and we don't get strange cliff walls at the edge of the map.
      boolean assumeSameTileType = myTerrainType == map.getEnvironment(x, y).terrainType;
      short dirIndex = getTileImageIndex(map, x, y, assumeSameTileType);

      // Draw the current tile.
      BufferedImage frame = terrainSprites.get(dirIndex).getFrame(variation);
      g.drawImage(frame, (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize, frame.getWidth() * scale, frame.getHeight()
          * scale, null);

      // Handle drawing corner-case tile variations if needed.
      if( terrainSprites.size() == 20 )
      {
        // If we didn't have a N or W transition, then look in the NW position
        if( (dirIndex & (NORTH | WEST)) == 0 && checkTileType(map, x - 1, y - 1, assumeSameTileType) )
        {
          g.drawImage(terrainSprites.get(NW).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth() * scale, frame.getHeight() * scale, null);
        }
        if( (dirIndex & (NORTH | EAST)) == 0 && checkTileType(map, x + 1, y - 1, assumeSameTileType) )
        {
          g.drawImage(terrainSprites.get(NE).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth() * scale, frame.getHeight() * scale, null);
        }
        if( (dirIndex & (SOUTH | EAST)) == 0 && checkTileType(map, x + 1, y + 1, assumeSameTileType) )
        {
          g.drawImage(terrainSprites.get(SE).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth() * scale, frame.getHeight() * scale, null);
        }
        if( (dirIndex & (SOUTH | WEST)) == 0 && checkTileType(map, x - 1, y + 1, assumeSameTileType) )
        {
          g.drawImage(terrainSprites.get(SW).getFrame(variation), (x - drawOffsetx) * tileSize, (y - drawOffsety) * tileSize,
              frame.getWidth() * scale, frame.getHeight() * scale, null);
        }
      }

      // Draw any tile transitions that are needed.
      for( TerrainSpriteSet tt : tileTransitions )
      {
        System.out.println("Drawing transition from " + tt.myTerrainType + " onto " + myTerrainType);
        tt.drawTerrain(g, map, x, y, scale);
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
    short dirIndex = 0;
    if( terrainSprites.size() > 1 ) // We expect the size to be either 1, 16, or 20.
    {
      // Figure out which neighboring tiles have the same terrain type as this one.
      dirIndex |= checkTileType(map, x, y - 1, assumeSameTileType) ? NORTH : 0;
      dirIndex |= checkTileType(map, x + 1, y, assumeSameTileType) ? EAST : 0;
      dirIndex |= checkTileType(map, x, y + 1, assumeSameTileType) ? SOUTH : 0;
      dirIndex |= checkTileType(map, x - 1, y, assumeSameTileType) ? WEST : 0;
    }

    // Normalize the index value just in case.
    if( dirIndex >= terrainSprites.size() )
    {
      // We could print a warning here, but there should have been one when the sprites were loaded.
      // At this point we are just preventing an ArrayOutOfBoundsException.
      dirIndex = (short) (dirIndex % terrainSprites.size());
    }
    return dirIndex;
  }

  private boolean isTerrainObject(Environment.Terrains terrainType)
  {
    switch (terrainType)
    {
      case CITY:
      case FACTORY:
      case FOREST:
      case AIRPORT:
      case SEAPORT:
      case HQ:
      case LAB:
      case MOUNTAIN:
        return true;
      case DUNES:
      case GRASS:
      case REEF:
      case ROAD:
      case SEA:
      case SHOAL:
      default:
        return false;
    }
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
      Environment.Terrains otherTerrain = map.getEnvironment(x, y).terrainType;
      Environment.Terrains otherBase = getBaseTerrainType(map.getEnvironment(x, y).terrainType);

      if( (otherTerrain == myTerrainType) || // Terrain types match.
          (otherBase == myTerrainType) || // Terrain bases match.
          myTerrainAffinities.contains(otherTerrain) || // Affinity with other tile type.
          myTerrainAffinities.contains(otherBase) ) // Affinity with other tile base type.
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
   * purposes (esp. terrain transitions), the base tile type of FOREST is actually PLAIN.
   */
  private static Environment.Terrains getBaseTerrainType(Environment.Terrains terrainType)
  {
    Environment.Terrains baseTerrain = terrainType;
    switch (baseTerrain)
    {
      case CITY:
      case DUNES:
      case FACTORY:
      case AIRPORT:
      case FOREST:
      case HQ:
      case LAB:
      case MOUNTAIN:
      case GRASS:
      case ROAD:
        baseTerrain = Environment.Terrains.GRASS;
        break;
      case BRIDGE:
      case SEAPORT:
      case SHOAL:
        baseTerrain = Environment.Terrains.SHOAL;
        break;
      case REEF:
      case SEA:
        baseTerrain = Environment.Terrains.SEA;
        break;
      default:
        System.out.println("ERROR! [TerrainSpriteSet.getBaseTerrainType] Invalid terrain type " + baseTerrain);
    }

    return baseTerrain;
  }
}
