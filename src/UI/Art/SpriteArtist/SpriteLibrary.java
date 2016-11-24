package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderLibrary;
import Terrain.Environment;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;

/**
 * Responsible for loading all game images from disk. All methods are static, and resources are loaded the first time they are needed.
 */
public class SpriteLibrary
{
  // This is the physical size of a single map square in pixels.
  public static final int baseSpriteSize = 16;

  // Define extra colors as needed.
  private static final Color PURPLE = new Color(231, 123, 255 );

  // Map Building colors.
  public static final Color[] defaultMapColors = { new Color(70, 70, 70), new Color(110, 110, 110), new Color(160, 160, 160),
      new Color(200, 200, 200), new Color(245, 245, 245) };
  private static Color[] pinkMapBuildingColors = { new Color(255, 219, 74), new Color(190, 90, 90), new Color(240, 140, 140),
      new Color(250, 190, 190), new Color(255, 245, 245) };
  private static Color[] cyanMapBuildingColors = { new Color(255, 219, 74), new Color(77, 157, 157), new Color(130, 200, 200),
      new Color(200, 230, 230), new Color(245, 255, 255) };
  private static Color[] orangeMapBuildingColors = { new Color(255, 237, 29), new Color(139, 77, 20), new Color(231, 139, 41),
      new Color(243, 186, 121), new Color(255, 234, 204) };
  private static Color[] purpleMapBuildingColors = { new Color(255, 207, 95), new Color(133, 65, 130), new Color(174, 115, 189),
    new Color(222, 171, 240), new Color(255, 231, 255) };

  // Map Unit colors.
  private static Color[] pinkMapUnitColors = { new Color(177, 62, 62), new Color(255, 100, 100), new Color(255, 136, 136),
      new Color(255, 175, 175), new Color(255, 230, 230) };
  private static Color[] cyanMapUnitColors = { new Color(0, 105, 105), new Color(0, 170, 170), new Color(0, 215, 215),
      new Color(0, 255, 255), new Color(195, 255, 255), };
  private static Color[] orangeMapUnitColors = { new Color(163, 77, 0), new Color(252, 139, 7), new Color(255, 160, 65),
      new Color(255, 186, 97), new Color(255, 225, 183), };
  private static Color[] purpleMapUnitColors = { new Color(90, 56, 99), new Color(181, 73, 198), new Color(201, 98, 223),
    new Color(222, 171, 240), new Color(243, 210, 255), };

  private static HashMap<Color, ColorPalette> buildingColorPalettes = new HashMap<Color, ColorPalette>(){
    private static final long serialVersionUID = 1L;
    {
      // Create a mapping of game colors to the fine-tuned colors that will be used for map sprites.
      put(Color.PINK, new ColorPalette(pinkMapBuildingColors));
      put(Color.CYAN, new ColorPalette(cyanMapBuildingColors));
      put(Color.ORANGE, new ColorPalette(orangeMapBuildingColors));
      put(PURPLE, new ColorPalette(purpleMapBuildingColors));
    }
  };
  private static HashMap<Color, ColorPalette> mapUnitColorPalettes = new HashMap<Color, ColorPalette>(){
    private static final long serialVersionUID = 1L;
    {
      // Create a mapping of game colors to the fine-tuned colors that will be used for map sprites.
      put(Color.PINK, new ColorPalette(pinkMapUnitColors));
      put(Color.CYAN, new ColorPalette(cyanMapUnitColors));
      put(Color.ORANGE, new ColorPalette(orangeMapUnitColors));
      put(PURPLE, new ColorPalette(purpleMapUnitColors));
    }
  };

  public static final Color[] coColorList = { Color.PINK, Color.CYAN, Color.ORANGE, PURPLE };

  // TODO: Account for weather?
  private static HashMap<SpriteSetKey, TerrainSpriteSet> spriteSetMap = new HashMap<SpriteSetKey, TerrainSpriteSet>();
  // TODO: Consider templatizing the key types, and then combining these two maps.
  private static HashMap<UnitSpriteSetKey, UnitSpriteSet> unitMapSpriteSetMap = new HashMap<UnitSpriteSetKey, UnitSpriteSet>();

  // Sprites to hold the images for drawing tentative moves on the map.
  private static Sprite moveCursorLineSprite = null;
  private static Sprite moveCursorArrowSprite = null;

  // HP numbers to overlay on map units when damaged.
  private static Sprite mapUnitHPSprites = null;

  // Cargo icon for when transports are holding other units.
  private static BufferedImage mapUnitCargoIcon = null;

  // Letters for writing in menus.
  private static Sprite letterSpritesUppercase = null;
  private static Sprite letterSpritesLowercase = null;
  private static Sprite numberSprites = null;

  // Letters for writing in menus.
  private static Sprite letterSpritesSmallCaps = null;
  private static Sprite numberSpritesSmallCaps = null;

  // Commander overlay backdrops (shows commander name and funds) for each Commander in the game.
  private static HashMap<Commander, Sprite> coOverlays = new HashMap<Commander, Sprite>();

  private static BufferedImage actionCursor = null;

  // Text images for main menu options.
  private static Sprite menuOptionsSprite = null;

  // Victory/Defeat text for game end.
  private static BufferedImage gameOverDefeatText = null;
  private static BufferedImage gameOverVictoryText = null;

  /**
   * Retrieve (loading if needed) the sprites associated with the given terrain type. For ownable terrain types
   * (e.g. cities), the unowned variant of the sprite will be returned.
   */
  public static TerrainSpriteSet getTerrainSpriteSet(Environment.Terrains terrain)
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
  public static TerrainSpriteSet getTerrainSpriteSet(Location loc)
  {
    SpriteSetKey spriteKey = SpriteSetKey.instance(loc.getEnvironment().terrainType, loc.getOwner());
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
   * @param terrainType
   */
  private static void createTerrainSpriteSet(SpriteSetKey spriteKey)
  {
    Environment.Terrains terrainType = spriteKey.terrainKey;
    System.out.println("INFO: Loading terrain sprites for " + terrainType);

    TerrainSpriteSet ss = null;
    int w = baseSpriteSize;
    int h = baseSpriteSize;
    switch (terrainType)
    {
      case CITY:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/city_clear.png"), w * 2, h * 2);
        break;
      case DUNES:
        break;
      case FACTORY:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/factory_clear.png"), w * 2, h * 2);
        break;
      case FOREST:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/forest_clear.png"), w * 2, h * 2);
        break;
      case GRASS:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/grass_clear.png"), w, h);
        break;
      case HQ:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/hq_clear.png"), w * 2, h * 2);
        break;
      case MOUNTAIN:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/mountain_clear.png"), w * 2, h * 2);
        break;
      case SEA:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/sea_clear.png"), w, h);
        ss.addTileTransition(Environment.Terrains.GRASS, loadSpriteSheetFile("res/tileset/sea_grass_clear.png"), w, h);
        break;
      case REEF:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/reef_clear.png"), w, h);
        ss.addTileTransition(Environment.Terrains.SEA, loadSpriteSheetFile("res/tileset/reef_clear.png"), w, h);
        break;
      case ROAD:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/road_clear.png"), w, h);
        break;
      case SHOAL:
        ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/shoal_clear.png"), w, h);
        ss.addTileTransition(Environment.Terrains.SEA, loadSpriteSheetFile("res/tileset/shoal_sea_clear.png"), w, h);
        ss.addTileTransition(Environment.Terrains.GRASS, loadSpriteSheetFile("res/tileset/shoal_grass_clear.png"), w, h);
        break;
      default:
        System.out.println("ERROR! [SpriteLibrary.loadTerrainSpriteSet] Unknown terrain type " + terrainType);
    }

    if( spriteKey.commanderKey != null )
    {
      ss.colorize(defaultMapColors, getBuildingColors(spriteKey.commanderKey.myColor).paletteColors);
    }
    spriteSetMap.put(spriteKey, ss);
  }

  private static BufferedImage loadSpriteSheetFile(String filename)
  {
    BufferedImage bi = null;
    try
    {
      bi = ImageIO.read(new File(filename));
    }
    catch (IOException ioex)
    {
      System.out.println("WARNING! Exception loading resource file " + filename);
      bi = null;
    }
    return bi;
  }

  public static Sprite getMoveCursorLineSprite()
  {
    if( moveCursorLineSprite == null )
    {
      moveCursorLineSprite = new Sprite(loadSpriteSheetFile("res/tileset/moveCursorLine.png"), baseSpriteSize, baseSpriteSize);
    }

    return moveCursorLineSprite;
  }

  public static Sprite getMoveCursorArrowSprite()
  {
    if( moveCursorArrowSprite == null )
    {
      moveCursorArrowSprite = new Sprite(loadSpriteSheetFile("res/tileset/moveCursorArrow.png"), baseSpriteSize, baseSpriteSize);
    }

    return moveCursorArrowSprite;
  }

  private static ColorPalette getBuildingColors(Color colorKey)
  {
    return buildingColorPalettes.get(colorKey);
  }

  private static ColorPalette getMapUnitColors(Color colorKey)
  {
    return mapUnitColorPalettes.get(colorKey);
  }

  private static class SpriteSetKey
  {
    public final Environment.Terrains terrainKey;
    public final Commander commanderKey;
    private static ArrayList<SpriteSetKey> instances = new ArrayList<SpriteSetKey>();

    private SpriteSetKey(Environment.Terrains terrain, Commander co)
    {
      terrainKey = terrain;
      commanderKey = co;
    }

    public static SpriteSetKey instance(Environment.Terrains terrain, Commander co)
    {
      SpriteSetKey key = null;
      for( int i = 0; i < instances.size(); ++i )
      {
        if( instances.get(i).terrainKey == terrain && instances.get(i).commanderKey == co )
        {
          key = instances.get(i);
          break;
        }
      }
      if( key == null )
      {
        key = new SpriteSetKey(terrain, co);
        instances.add(key);
      }
      return key;
    }
  }

  ///////////////////////////////////////////////////////////////////
  //  Below is code for dealing with unit sprites.
  ///////////////////////////////////////////////////////////////////

  private static class UnitSpriteSetKey
  {
    public final UnitModel.UnitEnum unitTypeKey;
    public final Commander commanderKey;
    private static ArrayList<UnitSpriteSetKey> instances = new ArrayList<UnitSpriteSetKey>();

    private UnitSpriteSetKey(UnitModel.UnitEnum unitType, Commander co)
    {
      unitTypeKey = unitType;
      commanderKey = co;
    }

    public static UnitSpriteSetKey instance(UnitModel.UnitEnum unitType, Commander co)
    {
      UnitSpriteSetKey key = null;
      for( int i = 0; i < instances.size(); ++i )
      {
        if( instances.get(i).unitTypeKey == unitType && instances.get(i).commanderKey == co )
        {
          key = instances.get(i);
          break;
        }
      }
      if( key == null )
      {
        key = new UnitSpriteSetKey(unitType, co);
        instances.add(key);
      }
      return key;
    }
  }

  public static UnitSpriteSet getUnitMapSpriteSet(Unit unit)
  {
    UnitSpriteSetKey key = UnitSpriteSetKey.instance(unit.model.type, unit.CO);
    if( !unitMapSpriteSetMap.containsKey(key) )
    {
      // We don't have it? Go load it.
      createUnitMapSpriteSet(key);
    }
    // We either found it or created it; it had better be there.
    return unitMapSpriteSetMap.get(key);
  }

  private static void createUnitMapSpriteSet(UnitSpriteSetKey key)
  {
    System.out.println("creating " + key.unitTypeKey.toString() + " spriteset for CO " + key.commanderKey.myColor.toString());
    String filestr = getUnitSpriteFilename(key.unitTypeKey);
    UnitSpriteSet spriteSet = new UnitSpriteSet(loadSpriteSheetFile(filestr), baseSpriteSize, baseSpriteSize,
        getMapUnitColors(key.commanderKey.myColor));
    unitMapSpriteSetMap.put(key, spriteSet);
  }

  private static String getUnitSpriteFilename(UnitModel.UnitEnum unitType)
  {
    String spriteFile = "";
    switch (unitType)
    {
      case ANTI_AIR:
        break;
      case APC:
        spriteFile = "res/unit/apc_map.png";
        break;
      case ARTILLERY:
        break;
      case B_COPTER:
        break;
      case BATTLESHIP:
        break;
      case BOMBER:
        break;
      case CRUISER:
        break;
      case FIGHTER:
        break;
      case INFANTRY:
        spriteFile = "res/unit/infantry_map.png";
        break;
      case LANDER:
        break;
      case MD_TANK:
        break;
      case MECH:
        spriteFile = "res/unit/mech_map.png";
        break;
      case MISSILES:
        break;
      case RECON:
        break;
      case ROCKETS:
        break;
      case SUB:
        break;
      case T_COPTER:
        break;
      case TANK:
        break;
    }
    return spriteFile;
  }

  public static Sprite getMapUnitHPSprites()
  {
    if( null == mapUnitHPSprites )
    {
      mapUnitHPSprites = new Sprite(loadSpriteSheetFile("res/unit/unit_hp.png"), 8, 8);
    }
    return mapUnitHPSprites;
  }

  public static BufferedImage getCargoIcon()
  {
    if( null == mapUnitCargoIcon )
    {
      mapUnitCargoIcon = loadSpriteSheetFile("res/unit/icon/cargo.png");
    }
    return mapUnitCargoIcon;
  }

  ///////////////////////////////////////////////////////////////////
  //  Below is code for dealing with drawing in-game menus.
  ///////////////////////////////////////////////////////////////////

  /**
   * Returns a Sprite containing every uppercase letter, one letter per frame. The image
   * is loaded, and the Sprite is created, on the first call to this function, and simply
   * returned thereafter. Letters are stored in order starting from 'A'.
   * @return A Sprite object containing the in-game menu font for uppercase letters.
   */
  public static Sprite getLettersUppercase()
  {
    if( null == letterSpritesUppercase )
    {
      letterSpritesUppercase = new Sprite(loadSpriteSheetFile("res/ui/main/letters_uppercase.png"), 5, 11);
    }
    return letterSpritesUppercase;
  }

  /**
   * Returns a Sprite containing every lowercase letter, one letter per frame. The image
   * is loaded, and the Sprite is created, on the first call to this function, and simply
   * returned thereafter. Letters are stored in order starting from 'a'. 
   * @return A Sprite object containing the in-game menu font for lowercase letters.
   */
  public static Sprite getLettersLowercase()
  {
    if( null == letterSpritesLowercase )
    {
      letterSpritesLowercase = new Sprite(loadSpriteSheetFile("res/ui/main/letters_lowercase.png"), 5, 11);
    }
    return letterSpritesLowercase;
  }

  /**
   * Returns a Sprite containing the digits 0-9, one number per frame. The image
   * is loaded, and the Sprite is created, on the first call to this function, and simply
   * returned thereafter. Numbers are stored in order starting from 0.
   * @return A Sprite object containing the in-game menu font for numbers.
   */
  public static Sprite getNumbers()
  {
    if( null == numberSprites )
    {
      numberSprites = new Sprite(loadSpriteSheetFile("res/ui/main/numbers.png"), 5, 11);
    }
    return numberSprites;
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
      letterSpritesSmallCaps = new Sprite(loadSpriteSheetFile("res/ui/letters.png"), 5, 6);
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
      numberSpritesSmallCaps = new Sprite(loadSpriteSheetFile("res/ui/numbers.png"), 5, 6);
    }
    return numberSpritesSmallCaps;
  }

  /**
   * Draws the provided text at the provided location, using the standard alphanumeric sprite set.
   * @param g Graphics object to draw the text.
   * @param text Text to be drawn as sprited letters.
   * @param x X-coordinate of the top-left corner of the first letter to be drawn.
   * @param y Y-coordinate of the top-left corner of the first letter to be drawn.
   * @param scale Scaling factor to be applied when drawing.
   */
  public static void drawText(Graphics g, String text, int x, int y, int scale)
  {
    Sprite uppercase = getLettersUppercase();
    Sprite lowercase = getLettersLowercase();
    int menuTextWidth = uppercase.getFrame(0).getWidth() * scale;
    int menuTextHeight = uppercase.getFrame(0).getHeight() * scale;

    for( int i = 0; i < text.length(); ++i, x += menuTextWidth )
    {
      char thisChar = text.charAt(i);
      if( Character.isAlphabetic(thisChar) )
      {
        if( Character.isUpperCase(thisChar) )
        {
          int letterIndex = thisChar - 'A';
          g.drawImage(uppercase.getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
        }
        else
        {
          int letterIndex = thisChar - 'a';
          g.drawImage(lowercase.getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
        }
      }
      else if( Character.isDigit(thisChar) )
      {
        int letterIndex = thisChar - '0';
        g.drawImage(getNumbers().getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
      }
    }
  }

  /**
   * Draws the provided text at the provided location, using the small-caps alphanumeric sprite set.
   * @param g Graphics object to draw the text.
   * @param text Text to be drawn as sprited letters.
   * @param x X-coordinate of the top-left corner of the first letter to be drawn.
   * @param y Y-coordinate of the top-left corner of the first letter to be drawn.
   * @param scale Scaling factor to be applied when drawing.
   */
  public static void drawTextSmallCaps(Graphics g, String text, int x, int y, int scale)
  {
    Sprite smallCaps = getLettersSmallCaps();
    int menuTextWidth = smallCaps.getFrame(0).getWidth() * scale;
    int menuTextHeight = smallCaps.getFrame(0).getHeight() * scale;
    text = text.toUpperCase(); // SmallCaps is all uppercase.

    for( int i = 0; i < text.length(); ++i, x += menuTextWidth )
    {
      if( Character.isAlphabetic(text.charAt(i)) )
      {
        int letterIndex = text.charAt(i) - 'A';
        g.drawImage(smallCaps.getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
      }
      else if( Character.isDigit(text.charAt(i)) )
      {
        int letterIndex = text.charAt(i) - '0';
        g.drawImage(getNumbersSmallCaps().getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
      }
    }
  }

  /**
   * Draws the provided image, centered around x, y.
   */
  public static void drawImageCenteredOnPoint(Graphics g, BufferedImage image, int x, int y, int drawScale)
  {
    // Calculate the size to draw.
    int drawWidth = image.getWidth() * drawScale;
    int drawHeight = image.getHeight() * drawScale;

    // Center over the target location.
    int drawX = x - drawWidth / 2;
    int drawY = y - drawHeight / 2;

    g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
  }

  /**
   * Returns the overlay image for the HUD, which serves as a backdrop for the commander
   * name and the currently-available funds.
   * @param co The Commander whose overlay we are drawing. This allows us to colorize it appropriately.
   * @param leftSide Whether we want the overlay image for the left-side corner (false is right side).
   */
  public static BufferedImage getCoOverlay(Commander co, boolean leftSide)
  {
    if( !coOverlays.containsKey(co) )
    {
      final int OVERLAY_WIDTH = 97;
      final int OVERLAY_HEIGHT = 20;

      // If we don't already have this overlay, go load and store it.
      Sprite overlay = new Sprite(loadSpriteSheetFile("res/ui/co_overlay.png"), OVERLAY_WIDTH, OVERLAY_HEIGHT);
      overlay.colorize(defaultMapColors, mapUnitColorPalettes.get(co.myColor).paletteColors);

      // Draw the Commander's mug on top of the overlay.
      BufferedImage coMug = getCommanderSprites(co.coInfo.cmdrEnum).eyes;
      int mugW = coMug.getWidth();
      Graphics g = overlay.getFrame(0).getGraphics();
      g.drawImage(coMug, mugW, 1, -mugW, coMug.getHeight(), null);
      Graphics g1 = overlay.getFrame(1).getGraphics();
      g1.drawImage(coMug, OVERLAY_WIDTH-mugW, 1, null);

      coOverlays.put(co, overlay);
    }
    // Figure out which sub-image they want, and give it to them.
    int index = (leftSide) ? 0 : 1;
    return coOverlays.get(co).getFrame(index);
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
      actionCursor = loadSpriteSheetFile("res/tileset/cursor_action.png");
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
      menuOptionsSprite = new Sprite(createBlankImageIfNull(loadSpriteSheetFile("res/ui/main/newgame.png")));
      menuOptionsSprite.addFrame(createBlankImageIfNull(loadSpriteSheetFile("res/ui/main/options.png")));
      menuOptionsSprite.addFrame(createBlankImageIfNull(loadSpriteSheetFile("res/ui/main/quit.png")));
    }
    return menuOptionsSprite;
  }

  public static BufferedImage getGameOverDefeatText()
  {
    if(null == gameOverDefeatText)
    {
      gameOverDefeatText = loadSpriteSheetFile("res/ui/defeat.png");
    }
    return gameOverDefeatText;
  }

  public static BufferedImage getGameOverVictoryText()
  {
    if(null == gameOverVictoryText)
    {
      gameOverVictoryText = loadSpriteSheetFile("res/ui/victory.png");
    }
    return gameOverVictoryText;
  }
  ///////////////////////////////////////////////////////////////////
  //  Below is code for loading Commander sprite images.
  ///////////////////////////////////////////////////////////////////

  private static HashMap<CommanderLibrary.CommanderEnum, CommanderSpriteSet> coSpriteSets = new HashMap<CommanderLibrary.CommanderEnum, CommanderSpriteSet>();

  public static CommanderSpriteSet getCommanderSprites( CommanderLibrary.CommanderEnum whichCo )
  {
    CommanderSpriteSet css = null;

    if(!coSpriteSets.containsKey(whichCo))
    {
      // We don't have it, so we need to load it.
      String baseFileName = getCommanderBaseSpriteName( whichCo );

      BufferedImage body = createBlankImageIfNull(loadSpriteSheetFile(baseFileName + ".png"));
      BufferedImage head = createBlankImageIfNull(loadSpriteSheetFile(baseFileName + "_face.png"));
      BufferedImage eyes = createBlankImageIfNull(loadSpriteSheetFile(baseFileName + "_eyes.png"));

      coSpriteSets.put(whichCo, new CommanderSpriteSet(body, head, eyes));
    }

    css = coSpriteSets.get(whichCo);

    return css;
  }

  private static String getCommanderBaseSpriteName( CommanderLibrary.CommanderEnum whichCo )
  {
    String str = "res/co/";
    switch(whichCo)
    {
      case LION:
        str += "lion";
        break;
      case PATCH:
        str += "patch";
        break;
      case STRONG:
        str += "strong";
        break;
      case NOONE:
        default:
          // Not a real Commander. Gonna fall back to placeholder images.
    }
    return str;
  }
}
