package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Terrain.MapLocation;
import Terrain.TerrainType;
import UI.UIUtils;
import UI.UIUtils.COSpriteSpec;
import UI.UIUtils.Faction;
import UI.UIUtils.SourceGames;
import Units.Unit;
import Units.UnitModel;

/**
 * Responsible for loading all game images from disk. All methods are static, and resources are loaded the first time they are needed.
 */
public class SpriteLibrary
{
  // This is the physical size of a single map square in pixels.
  public static final int baseSpriteSize = 16;

  public static final int baseIconSize = 8;

  public static final String charKey = "%./-~,;:!?'&()_";
  public static final String DEFAULT_FACTION = "Thorn";

  private static HashMap<SpriteSetKey, TerrainSpriteSet> spriteSetMap = new HashMap<SpriteSetKey, TerrainSpriteSet>();
  private static HashMap<UnitSpriteSetKey, UnitSpriteSet> mapUnitSpriteSetMap = new HashMap<UnitSpriteSetKey, UnitSpriteSet>();

  // Sprites to hold the images for drawing tentative moves on the map.
  private static Sprite moveCursorLineSprite = null;
  private static Sprite moveCursorArrowSprite = null;

  // Numbers and letters to overlay on map units.
  private static Sprite mapUnitNumberSprites = null;
  private static Sprite mapUnitLetterSprites = null;
  private static Sprite mapUnitSymbolSprites = null;
  private static Map<Color,Map<Character,BufferedImage>> mapUnitTextSprites = null;

  // Unit icons for various activities.
  private static HashMap<Color, BufferedImage> mapUnitCargoIcons = new HashMap<Color, BufferedImage>();
  private static HashMap<Color, BufferedImage> mapUnitCaptureIcons = new HashMap<Color, BufferedImage>();
  private static HashMap<Color, BufferedImage> mapUnitHideIcons = new HashMap<Color, BufferedImage>();

  // Letters for writing in menus.
  private static PixelFont fontStandard;

  // Letters for writing in menus.
  private static Sprite letterSpritesSmallCaps = null;
  private static Sprite numberSpritesSmallCaps = null;
  private static Sprite symbolSpritesSmallCaps = null;

  // Cursor for highlighting things in-game.
  private static Sprite cursorSprites = null;
  private static Sprite arrowheadSprites = null;
  private static Sprite previewArrow = null;

  // Commander overlay backdrops (shows army name and funds) for each Commander in the game.
  private static HashMap<Commander, Sprite> coOverlays = new HashMap<Commander, Sprite>();
  private static HashMap<Commander, Sprite> coPowerBarPieces = new HashMap<Commander, Sprite>();

  private static BufferedImage actionCursor = null;

  // Text images for main menu options.
  private static Sprite menuOptionsSprite = null;

  // Victory/Defeat text for game end.
  private static BufferedImage gameOverDefeatText = null;
  private static BufferedImage gameOverVictoryText = null;

  /**
   * Loads the image at the given file location and returns it as a BufferedImage.
   * @param filename The full file-path to an image on disk.
   * @return The file as a BufferedImage, or null if the file cannot be read.
   */
  public static BufferedImage loadSpriteSheetFile(String filename)
  {
    BufferedImage bi = null;
    try
    {
      File imgFile = new File(filename);
      if( imgFile.exists() && !imgFile.isDirectory() )
        bi = ImageIO.read(imgFile);
      // Missing sprites are common (e.g. for units with missing animations),
      // and normally benign, so don't print a warning.
    }
    catch (IOException ioex)
    {
      System.out.println("WARNING! Exception loading resource file " + filename);
      bi = null;
    }
    return bi;
  }

  /**
   * Retrieve (loading if needed) the sprites associated with the given terrain type. For ownable terrain types
   * (e.g. cities), the unowned variant of the sprite will be returned.
   */
  public static TerrainSpriteSet getTerrainSpriteSet(TerrainType terrain)
  {
    SpriteSetKey spriteKey = SpriteSetKey.instance(terrain, null);
    if( !spriteSetMap.containsKey(spriteKey) )
    {
      // Don't have it? Create it.
      createTerrainSpriteSet(spriteKey);
    }
    // Now we must have an entry for that terrain type.
    return spriteSetMap.get(spriteKey);
  }

  /**
   * Retrieve (loading if needed) the sprites associated with the terrain type at the specified location.
   * This function returns the owner-appropriate version of the tile for ownable terrain types.
   */
  public static TerrainSpriteSet getTerrainSpriteSet(MapLocation loc)
  {
    SpriteSetKey spriteKey = SpriteSetKey.instance(loc.getEnvironment().terrainType, COSpriteSpec.instance(loc.getOwner()));
    if( !spriteSetMap.containsKey(spriteKey) )
    {
      // Don't have it? Create it.
      createTerrainSpriteSet(spriteKey);
    }
    // Now we must have an entry for that terrain type.
    return spriteSetMap.get(spriteKey);
  }

  /**
   * Retrieve (loading if needed) the sprites associated with the given terrain type and faction/color
   */
  public static TerrainSpriteSet getTerrainSpriteSet(TerrainType terrain, COSpriteSpec spec)
  {
    SpriteSetKey spriteKey = SpriteSetKey.instance(terrain, spec);
    if( !spriteSetMap.containsKey(spriteKey) )
    {
      // Don't have it? Create it.
      createTerrainSpriteSet(spriteKey);
    }
    // Now we must have an entry for that terrain type.
    return spriteSetMap.get(spriteKey);
  }

  /**
   * Loads the images and builds the TerrainSpriteSet for the terrain type passed in.
   * If we are unable to load the correct images for any reason, make a blank TerrainSpriteSet.
   * @param spriteKey Specifies the terrain type and owner, so we can build the correct image.
   */
  private static void createTerrainSpriteSet(SpriteSetKey spriteKey)
  {
    TerrainType terrainType = spriteKey.terrainKey;

    String tileSpritesheetLocations = Engine.Driver.JAR_DIR + "res/tileset";
    String formatString = tileSpritesheetLocations + "/%s_%s.png";

    // Transition format Strings are e.g. "res/tileset/grass_bridge_clear.png", for
    // the transition from bridge onto grass. We'll drop in the first parameter here,
    // then pass the result to the TerrainSpriteSet of type bridge.
    String transitionFormatString = tileSpritesheetLocations + "/%s_%s_%s.png";

    TerrainSpriteSet ss = null;
    int w = baseSpriteSize;
    int h = baseSpriteSize;

    if( TerrainSpriteSet.isTerrainObject(terrainType))
      ss = new TerrainSpriteSet(terrainType, formatString, w * 2, h * 2);
    else
      ss = new TerrainSpriteSet(terrainType, formatString, w, h);

    // Load the appropriate images and define the necessary relationships for each tile type.
    if( terrainType == TerrainType.BRIDGE )
    {
      ss.addTerrainAffinity(TerrainType.GRASS); // No need to also add ROAD, since GRASS is the base of ROAD.
      ss.denyTerrainAffinity(TerrainType.RIVER); // RIVER has a base of GRASS, but we don't want bridge to tile with it.
    }
    else if( terrainType == TerrainType.GRASS )
    {
      ss.addTileTransition(TerrainType.BRIDGE, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
    }
    else if( terrainType == TerrainType.SEA )
    {
      ss.addTileTransition(TerrainType.GRASS, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
      ss.addTileTransition(TerrainType.BRIDGE, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
      ss.addTileTransition(TerrainType.RIVER, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
    }
    else if( terrainType == TerrainType.REEF )
    {
      ss.addTileTransition(TerrainType.SEA, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
    }
    else if( terrainType == TerrainType.RIVER )
    {
      ss.addTileTransition(TerrainType.BRIDGE, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
      ss.addTerrainAffinity(TerrainType.SHOAL);
      ss.addTerrainAffinity(TerrainType.SEA);
    }
    else if( terrainType == TerrainType.ROAD )
    {
      ss.addTerrainAffinity(TerrainType.BRIDGE);
    }
    else if( terrainType == TerrainType.SHOAL )
    {
      ss.addTileTransition(TerrainType.SEA, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
      ss.addTileTransition(TerrainType.GRASS, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
      ss.addTileTransition(TerrainType.BRIDGE, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
      ss.addTileTransition(TerrainType.RIVER, String.format(transitionFormatString, terrainType.toString().toLowerCase(), "%s", "%s"), w, h);
    }

    // If this tile is owned by someone, fly their colors.
    if( spriteKey.commanderKey != null )
    {
      ss.setTeamColor(spriteKey.commanderKey.color);
    }
    spriteSetMap.put(spriteKey, ss);
  }

  public static Sprite getMoveCursorLineSprite()
  {
    if( moveCursorLineSprite == null )
    {
      moveCursorLineSprite = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/tileset/moveCursorLine.png"), baseSpriteSize, baseSpriteSize);
    }

    return moveCursorLineSprite;
  }

  public static Sprite getMoveCursorArrowSprite()
  {
    if( moveCursorArrowSprite == null )
    {
      moveCursorArrowSprite = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/tileset/moveCursorArrow.png"), baseSpriteSize, baseSpriteSize);
    }

    return moveCursorArrowSprite;
  }

  private static class SpriteSetKey
  {
    public final TerrainType terrainKey;
    public final COSpriteSpec commanderKey;
    private static HashMap<Integer, SpriteSetKey> instances = new HashMap<>();

    private SpriteSetKey(TerrainType terrain, COSpriteSpec spec)
    {
      terrainKey = terrain;
      commanderKey = spec;
    }

    public static SpriteSetKey instance(TerrainType terrain, COSpriteSpec spec)
    {
      int hash = myHash(terrain, spec);
      SpriteSetKey key = instances.getOrDefault(hash, null);
      if( key == null )
      {
        key = new SpriteSetKey(terrain, spec);
        instances.put(hash, key);
      }
      return key;
    }

    public static int myHash(TerrainType terrain, COSpriteSpec spec)
    {
      final int prime = 160091;
      int result = 1;
      result = prime * result + terrain.hashCode();
      result = prime * result;
      if(null != spec)
        result += spec.faction.name.hashCode();
      result = prime * result;
      if(null != spec)
        result += spec.color.hashCode();
      return result;
    }
  }

  ///////////////////////////////////////////////////////////////////
  //  Below is code for dealing with unit sprites.
  ///////////////////////////////////////////////////////////////////

  private static class UnitSpriteSetKey
  {
    public final String unitTypeKey;
    public final COSpriteSpec spriteSpec;
    private static HashMap<Integer, UnitSpriteSetKey> instances = new HashMap<>();

    private UnitSpriteSetKey(String unitType, Faction faction, Color color)
    {
      unitTypeKey = unitType;
      spriteSpec = new COSpriteSpec(faction, color);
    }

    public static UnitSpriteSetKey instance(String unitType, Faction faction, Color color)
    {
      String stdType = UnitModel.standardizeID(unitType);
      int hash = myHash(stdType, faction, color);
      UnitSpriteSetKey key = instances.getOrDefault(hash, null);
      if( key == null )
      {
        key = new UnitSpriteSetKey(stdType, faction, color);
        instances.put(hash, key);
      }
      return key;
    }

    public static int myHash(String unitTypeKey, Faction faction, Color color)
    {
      final int prime = 160091;
      int result = 1;
      result = prime * result + unitTypeKey.hashCode();
      result = prime * result + faction.name.hashCode();
      result = prime * result + color.hashCode();
      return result;
    }
  }

  public static UnitSpriteSet getMapUnitSpriteSet(Unit unit)
  {
    return getMapUnitSpriteSet(unit.model.name, unit.CO.faction, unit.CO.myColor);
  }

  public static UnitSpriteSet getMapUnitSpriteSet(String type, Faction faction, Color color)
  {
    UnitSpriteSetKey key = UnitSpriteSetKey.instance(type, faction, color);
    if( !mapUnitSpriteSetMap.containsKey(key) )
    {
      // We don't have it? Go load it.
      createMapUnitSpriteSet(key);
    }
    // We either found it or created it; it had better be there.
    return mapUnitSpriteSetMap.get(key);
  }

  private static void createMapUnitSpriteSet(UnitSpriteSetKey key)
  {
    UnitSpriteSet spriteSet = new UnitSpriteSet( key.unitTypeKey, key.spriteSpec.faction, UIUtils.getMapUnitColors(key.spriteSpec.color) );
    mapUnitSpriteSetMap.put(key, spriteSet);
  }

  public static Sprite getMapUnitNumberSprites()
  {
    if( null == mapUnitNumberSprites )
    {
      mapUnitNumberSprites = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/unit/icon/numbers.png"), 8, 8);
    }
    return mapUnitNumberSprites;
  }

  public static Sprite getMapUnitLetterSprites()
  {
    if( null == mapUnitLetterSprites )
    {
      mapUnitLetterSprites = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/unit/icon/alphabet.png"), 8, 8);
    }
    return mapUnitLetterSprites;
  }

  public static Sprite getMapUnitSymbolSprites()
  {
    if( null == mapUnitSymbolSprites )
    {
      mapUnitSymbolSprites = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/unit/icon/symbols.png"), 8, 8);
    }
    return mapUnitSymbolSprites;
  }

  public static Map<Character,BufferedImage> getColoredMapTextSprites(Color color)
  {
    if( null == mapUnitTextSprites )
    {
      mapUnitTextSprites = new HashMap<Color,Map<Character,BufferedImage>>();
    }
    if( null == mapUnitTextSprites.get(color) )
    {
      Map<Character,BufferedImage> colorMap = new HashMap<Character,BufferedImage>();

      // Colorize the characters...
      Sprite letters = new Sprite(getMapUnitLetterSprites());
      letters.colorize(Color.WHITE, color);
      // ...and put them into our map
      for (char ch = 'A'; ch <= 'Z'; ch++)
      {
        int letterIndex = ch - 'A';
        colorMap.put(ch, letters.getFrame(letterIndex));
      }
      
      // Do the same for numbers
      Sprite numbers = new Sprite(getMapUnitNumberSprites());
      numbers.colorize(Color.WHITE, color);
      for (char ch = '0'; ch <= '9'; ch++)
      {
        int letterIndex = ch - '0';
        colorMap.put(ch, numbers.getFrame(letterIndex));
      }
      
      // Put our new map into the general collection, so we don't have to do this again
      mapUnitTextSprites.put(color, colorMap);
    }
    
    return mapUnitTextSprites.get(color);
  }

  public static enum MapIcons
  {
    STUN, HEART, SHIELD, ENERGY, FUNDS, FUEL, AMMO;

    private BufferedImage myIcon = null;
    public BufferedImage getIcon()
    {
      if( null == myIcon )
      {
        myIcon = SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/unit/icon/"+this.toString().toLowerCase()+".png");
      }
      return myIcon;
    }
  }

  private static BufferedImage getColoredSprite(HashMap<Color, BufferedImage> map, String filename, Color color)
  {
    if( !map.containsKey(color) )
    {
      BufferedImage icon = SpriteLibrary.loadSpriteSheetFile(filename);
      BufferedImage bi = SpriteLibrary.createTransparentSprite(icon.getWidth(), icon.getHeight());
      Graphics g = bi.getGraphics();
      g.setColor(color);
      g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
      g.drawImage(icon, 0, 0, null);
      map.put(color, bi);
    }
    return map.get(color);
  }

  public static BufferedImage getCargoIcon(Color color)
  {
    return getColoredSprite( mapUnitCargoIcons, Engine.Driver.JAR_DIR + "res/unit/icon/cargo.png", color);
  }

  public static BufferedImage getCaptureIcon(Color color)
  {
    return getColoredSprite( mapUnitCaptureIcons, Engine.Driver.JAR_DIR + "res/unit/icon/capture.png", color);
  }

  public static BufferedImage getHideIcon(Color color)
  {
    return getColoredSprite( mapUnitHideIcons, Engine.Driver.JAR_DIR + "res/unit/icon/hide.png", color);
  }

  ///////////////////////////////////////////////////////////////////
  //  Below is code for dealing with drawing in-game menus.
  ///////////////////////////////////////////////////////////////////

  public static PixelFont getFontStandard()
  {
    if( null == fontStandard )
    {
      try
      {
        final File folder = new File(Engine.Driver.JAR_DIR + "res/");
        if( folder.canRead() )
        {
          for( final File fileEntry : folder.listFiles() )
          {
            String filepath = fileEntry.getAbsolutePath();
            // Look for files with our extension
            if( !fileEntry.isDirectory() && filepath.endsWith(".ttf") )
            {
              // The name should be formatted as fontname_wxh.ttf
              String[] nameparts = fileEntry.getName().split("[_x.]");

              if( nameparts.length != 4 ) continue;  // Not formatted correctly.

              int w = Integer.parseInt(nameparts[1]);
              int h = Integer.parseInt(nameparts[2]);

              Font temp = Font.createFont(Font.TRUETYPE_FONT, fileEntry);
              fontStandard = new PixelFont(temp, w, h);
              System.out.println("Using font " + fileEntry.getName());
              break;
            }
          }
        }
      }
      catch(Exception ex)
      {
        System.out.println("WARNING! Exception loading font file.");
        ex.printStackTrace();
      }
    }
    if( null == fontStandard )
    {
      // If our first attempt to load a font file failed, just use a system font.
      System.out.println("WARNING! Failed to load pixel font. Using fallback.");
      Font temp = new Font("Consolas", 0, 10);
      fontStandard = new PixelFont(temp);
    }
    return fontStandard;
  }


  /**
   * Returns a Sprite containing every small-uppercase letter, one letter per frame. The image
   * is loaded, and the Sprite is created, on the first call to this function, and simply
   * returned thereafter. Letters are all uppercase, and are stored in order starting from 'A'.
   * @return A Sprite object containing the in-game menu font for small-caps letters.
   */
  public static Sprite getLettersSmallCaps()
  {
    if( null == letterSpritesSmallCaps )
    {
      letterSpritesSmallCaps = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/letters.png"), 5, 6);
    }
    return letterSpritesSmallCaps;
  }

  /**
   * This function returns numbers which match the font returned from getSmallLetters().
   * Returns a Sprite containing the digits 0-9, one number per frame. The image
   * is loaded, and the Sprite is created, on the first call to this function, and simply
   * returned thereafter. Numbers are stored in order starting from 0.
   * @return A Sprite object containing the in-game menu font for small-caps numbers.
   */
  public static Sprite getNumbersSmallCaps()
  {
    if( null == numberSpritesSmallCaps )
    {
      numberSpritesSmallCaps = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/numbers.png"), 5, 6);
    }
    return numberSpritesSmallCaps;
  }

  /**
   * This function returns the sprite sheet for symbol characters that go along with
   * the letter sprites from getLettersSmallCaps(). The image is loaded on the first
   * call to this function, and simply returned thereafter.
   * @return A Sprite object containing the in-game menu font for small-caps symbols.
   */
  public static Sprite getSymbolsSmallCaps()
  {
    if( null == symbolSpritesSmallCaps )
    {
      symbolSpritesSmallCaps = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/symbols.png"), 5, 6);
    }
    return symbolSpritesSmallCaps;
  }

  private static HashMap<Color, Sprite> coloredCursors;
  public static Sprite getCursorSprites(Color color)
  {
    if( null == coloredCursors )
    {
      coloredCursors = new HashMap<Color, Sprite>();
    }
    if( !coloredCursors.containsKey(color) )
    {
      Sprite newCursor = new Sprite(SpriteLibrary.getCursorSprites());
      newCursor.colorize(UIUtils.defaultMapColors[4], color);
      coloredCursors.put(color, newCursor);
    }
    return coloredCursors.get(color);
  }
  public static Sprite getCursorSprites()
  {
    if( null == cursorSprites )
    {
      cursorSprites = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/cursor.png"), 6, 6);
    }
    return cursorSprites;
  }

  static Sprite getArrowheadSprites()
  {
    if( null == arrowheadSprites )
    {
      arrowheadSprites = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/arrowheads.png"), 10, 10);
    }
    return arrowheadSprites;
  }

  public static BufferedImage getPreviewArrow(Color key)
  {
    if( null == previewArrow )
    {
      previewArrow = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/preview_arrow.png"), baseSpriteSize, baseSpriteSize);
    }
    Sprite newArrow = new Sprite(previewArrow);
    newArrow.colorize(UIUtils.defaultMapColors, UIUtils.getMapUnitColors(key).paletteColors);
    return newArrow.getFrame(0);
  }

  public static final String OVERLAY_FILE_PATH = Engine.Driver.JAR_DIR + "res/ui/co_overlay.png";
  /**
   * Returns the overlay image for the HUD, which serves as a backdrop for the army
   * name and the currently-available funds.
   * @param co The Commander whose overlay we are drawing. This allows us to colorize it appropriately.
   * @param leftSide Whether we want the overlay image for the left-side corner (false is right side).
   */
  public static BufferedImage getCoOverlay(Commander co, boolean leftSide)
  {
    if( !coOverlays.containsKey(co) )
    {
      // If we don't already have this overlay, go load and store it.
      final BufferedImage sprite = SpriteLibrary.loadSpriteSheetFile(OVERLAY_FILE_PATH);
      Sprite overlay = new Sprite(sprite, sprite.getWidth()/2, sprite.getHeight());
      overlay.colorize(UIUtils.defaultMapColors, UIUtils.getMapUnitColors(co.myColor).paletteColors);

      // Draw the Commander's mug on top of the overlay.
      BufferedImage coMug = getCommanderSprites(co.coInfo).eyes;
      int mugW = coMug.getWidth();
      Graphics g = overlay.getFrame(0).getGraphics();
      g.drawImage(coMug, mugW, 1, -mugW, coMug.getHeight(), null);
      Graphics g1 = overlay.getFrame(1).getGraphics();
      g1.drawImage(coMug, overlay.getFrame(1).getWidth()-mugW, 1, null);

      coOverlays.put(co, overlay);
    }
    // Figure out which sub-image they want, and give it to them.
    int index = (leftSide) ? 0 : 1;
    return coOverlays.get(co).getFrame(index);
  }

  /** Draw and return an image with the CO's power bar. */
  public static BufferedImage getCoOverlayPowerBar(Commander co, int maxAP, double currentAP, double pixelsPerPowerPlanck)
  {
    BufferedImage bar = SpriteLibrary.createDefaultBlankSprite((int) (maxAP*pixelsPerPowerPlanck), 5);
    Graphics barGfx = bar.getGraphics();

    // Get the CO's colors
    Color[] palette = UIUtils.getMapUnitColors(co.myColor).paletteColors;

    // Draw the bar
    barGfx.setColor(Color.BLACK);               // Outside edge
    barGfx.drawRect(0, 0, bar.getWidth(), 4);
    barGfx.setColor(palette[5]);                // Inside - empty
    barGfx.fillRect(0, 1, bar.getWidth(), 3);
    barGfx.setColor(palette[2]);                // Inside - full
    barGfx.drawLine(0, 1, (int)(Math.floor(currentAP) * pixelsPerPowerPlanck), 1);
    barGfx.drawLine(0, 2, (int)(currentAP * pixelsPerPowerPlanck), 2);
    barGfx.drawLine(0, 3, (int)(Math.ceil(currentAP * pixelsPerPowerPlanck)), 3);

    return bar;
  }

  /** Return a Sprite with the various Ability Point images. */
  public static Sprite getCoOverlayPowerBarAPs(Commander co)
  {
    final int POWERBAR_FRAME_WIDTH = 7;
    final int POWERBAR_FRAME_HEIGHT = 9;

    // If we don't already have an image, load and colorize one for this Commander.
    if( !coPowerBarPieces.containsKey(co) )
    {
      // Image should be: empty bar, full bar, empty point, 1/3 point, 2/3 point, full point, large point.
      // Image should be: empty point, 1/3 point, 2/3 point, full point, large point.
      Sprite overlay = new Sprite(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/powerbar_pieces.png"), POWERBAR_FRAME_WIDTH, POWERBAR_FRAME_HEIGHT);
      overlay.colorize(UIUtils.defaultMapColors, UIUtils.getMapUnitColors(co.myColor).paletteColors);

      coPowerBarPieces.put(co, overlay);
    }
    return coPowerBarPieces.get(co);
  }

  /**
   * Creates a new blank (all black) image of the given size. This is used to generate placeholder
   * assets on the fly when we fail to load resources from disk.
   * @param w Desired width of the placeholder image.
   * @param h Desired height of the placeholder image.
   * @return A new BufferedImage of the specified size, filled in with all black.
   */
  public static BufferedImage createDefaultBlankSprite(int w, int h)
  {
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics big = bi.getGraphics();
    big.setColor(Color.BLACK);
    big.fillRect(0, 0, w, h);
    return bi;
  }

  /**
   * Creates a new transparent image of the given size. This is used as
   * a base for building generated image assets on the fly.
   * @param w Desired width of the image.
   * @param h Desired height of the image.
   * @return A new transparent BufferedImage of the specified size.
   */
  public static BufferedImage createTransparentSprite(int w, int h)
  {
    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
  }

  /**
   * If in is null, set it to a newly-generated blank image of default size. Return in.
   */
  public static BufferedImage createBlankImageIfNull(BufferedImage in)
  {
    if( null == in )
    {
      in = createDefaultBlankSprite(baseSpriteSize, baseSpriteSize);
    }
    return in;
  }

  /**
   * Get the image used to indicate the current action target.
   */
  public static BufferedImage getActionCursor()
  {
    if( null == actionCursor )
    {
      actionCursor = SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/tileset/cursor_action.png");
      if( null == actionCursor )
      {
        actionCursor = createDefaultBlankSprite(baseSpriteSize, baseSpriteSize);
      }
    }
    return actionCursor;
  }

  /**
   * Returns the image text for the different options in the main menu, as frames in a Sprite.
   * The options shall be ordered within the Sprite to match the menuOptions array in MainController.
   * @return
   */
  public static Sprite getMainMenuOptions()
  {
    if( null == menuOptionsSprite )
    {
      menuOptionsSprite = new Sprite(createBlankImageIfNull(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/main/newgame.png")));
      menuOptionsSprite.addFrame(createBlankImageIfNull(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/main/continue.png")));
      menuOptionsSprite.addFrame(createBlankImageIfNull(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/main/options.png")));
      menuOptionsSprite.addFrame(createBlankImageIfNull(SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/main/quit.png")));
    }
    return menuOptionsSprite;
  }

  public static BufferedImage getGameOverDefeatText()
  {
    if(null == gameOverDefeatText)
    {
      gameOverDefeatText = SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/defeat.png");
    }
    return gameOverDefeatText;
  }

  public static BufferedImage getGameOverVictoryText()
  {
    if(null == gameOverVictoryText)
    {
      gameOverVictoryText = SpriteLibrary.loadSpriteSheetFile(Engine.Driver.JAR_DIR + "res/ui/victory.png");
    }
    return gameOverVictoryText;
  }
  ///////////////////////////////////////////////////////////////////
  //  Below is code for loading Commander sprite images.
  ///////////////////////////////////////////////////////////////////

  private static HashMap<String, CommanderSpriteSet> coSpriteSets = new HashMap<String, CommanderSpriteSet>();

  public static CommanderSpriteSet getCommanderSprites( CommanderInfo whichCo )
  {
    CommanderSpriteSet css = null;

    if(!coSpriteSets.containsKey(whichCo.name))
    {
      // We don't have it, so we need to load it.
      String formatBase = Engine.Driver.JAR_DIR + "res/co/"+whichCo.name+"%s%s.png";

      BufferedImage body = SpriteLibrary.loadSpriteSheetFile(String.format(formatBase, whichCo.discriminator, ""));
      if( null == body )                      body = getCommanderSpriteAlt(formatBase, "");
      BufferedImage head = SpriteLibrary.loadSpriteSheetFile(String.format(formatBase, whichCo.discriminator, "_face"));
      if( null == head )                      head = getCommanderSpriteAlt(formatBase, "_face");
      BufferedImage eyes = SpriteLibrary.loadSpriteSheetFile(String.format(formatBase, whichCo.discriminator, "_eyes"));
      if( null == eyes )                      eyes = getCommanderSpriteAlt(formatBase, "_eyes");

      coSpriteSets.put(whichCo.name, new CommanderSpriteSet(body, head, eyes));
    }

    css = coSpriteSets.get(whichCo.name);

    return css;
  }
  private static BufferedImage getCommanderSpriteAlt(String format, String mugType)
  {
    String formatPlaceholder = Engine.Driver.JAR_DIR + "res/co/placeholder%s.png";

    // Try all the possible discriminators to see if we get an alternate. If no, use a placeholder instead.
    BufferedImage output = SpriteLibrary.loadSpriteSheetFile(String.format(formatPlaceholder, mugType));
    for( SourceGames game : SourceGames.values() )
    {
      String discrim = (game.discriminator.length() > 0 ? "_" : "") + game.discriminator;
      BufferedImage temp = SpriteLibrary.loadSpriteSheetFile(String.format(format, discrim, mugType));
      if( null != temp )
      {
        output = temp;
        break;
      }
    }
    return output;
  }
}
