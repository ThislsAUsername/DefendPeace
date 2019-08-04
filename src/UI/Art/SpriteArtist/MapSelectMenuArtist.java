package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.TerrainType;
import UI.MapSelectController;

public class MapSelectMenuArtist
{
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);
  private static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private static final int characterWidth = SpriteLibrary.getLettersUppercase().getFrame(0).getWidth();
  private static final int characterHeight = SpriteLibrary.getLettersUppercase().getFrame(0).getHeight();
  private static final int maxNameDisplayLength = 14;
  // The map-name section shall be wide enough for a 14-character name, plus two-pixel buffer on each side.
  // (characterWidth+1)*maxNameDisplayLength + 3 (3 not 4, because the kerning buffer for the final letter is 1 already).
  private static final int nameSectionWidth = ((characterWidth+1) * maxNameDisplayLength) + 3;

  private static MapInfo selectedMapInfo = null;

  public static void draw(Graphics g, MapSelectController gameSetup)
  {
    if(!gameSetup.inSubmenu())
    { // Still in Game Setup. We draw.
      drawMapSelectMenu(g, gameSetup);
    }
    else
    { // Pass this along to the next Artist.
      GameOptionSetupArtist.draw(g, selectedMapInfo, gameSetup.getSubController());
    }
  }

  private static void drawMapSelectMenu(Graphics g, MapSelectController gameSetup)
  {
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int drawScale = SpriteOptions.getDrawScale();
    int menuWidth = dimensions.width/drawScale;
    int menuHeight = dimensions.height/drawScale;
    BufferedImage menuImage = SpriteLibrary.createDefaultBlankSprite(menuWidth, menuHeight);
    Graphics menuGraphics = menuImage.getGraphics();

    int highlightedOption = gameSetup.getSelectedOption();

    /////////////// Map selection pane ///////////////////////
    // Paint the whole area over in our background color.
    menuGraphics.setColor(MENUBGCOLOR);
    menuGraphics.fillRect(0,0,menuWidth,menuHeight);

    // Draw the frame for the list of maps, with the highlight for the current option.
    int frameBorderHeight = 3;
    int nameSectionDrawWidth = nameSectionWidth;
    menuGraphics.setColor(MENUFRAMECOLOR);
    menuGraphics.fillRect(nameSectionDrawWidth, 0, 1, menuHeight); // sidebar
    menuGraphics.fillRect(0,0,nameSectionDrawWidth,frameBorderHeight); // top bar
    menuGraphics.fillRect(0, menuHeight-frameBorderHeight, nameSectionDrawWidth, 3); // bottom bar
    //TODO: Draw little arrows on the top and bottom frame to show it can scroll.

    // Draw the highlight for the selected option.
    menuGraphics.setColor(MENUHIGHLIGHTCOLOR);
    int menuTextYStart = frameBorderHeight;
    int menuOptionHeight = (SpriteLibrary.getLettersUppercase().getFrame(0).getHeight() + 1); // +1 for a buffer between options.
    int selectedOptionYOffset = menuTextYStart + highlightedOption * (menuOptionHeight);

    // Get the list of selectable maps (possibly specifying a filter (#players, etc).
    ArrayList<MapInfo> mapInfos = MapLibrary.getMapList();
    int verticalShift = 0; // How many map names we skip drawing "off the top"
    int displayableCount = menuHeight / menuOptionHeight; // how many maps we can cram on the screen
    while (selectedOptionYOffset > menuHeight/2 && // Loop until either the cursor's bumped up to the center of the screen...
        displayableCount+verticalShift < mapInfos.size()) //  or we'll already show the last map 
    {
      verticalShift++;
      selectedOptionYOffset -= menuOptionHeight;
    }
    
    menuGraphics.fillRect(0, selectedOptionYOffset, nameSectionDrawWidth, menuOptionHeight);

    // Display the names from the list
    for(int i = 0; i < displayableCount; ++i)
    {
      int drawX = 2; // Offset from the edge of the window slightly.
      int drawY = menuTextYStart + 1 + i * (menuOptionHeight);

      // Draw visible map names in the list.
      if (mapInfos.size() > i + verticalShift)
      {
        String str = mapInfos.get(i + verticalShift).mapName;
        if(str.length() > maxNameDisplayLength)
        {
          str = str.substring(0, maxNameDisplayLength);
        }

        SpriteUIUtils.drawText(menuGraphics, str, drawX, drawY);
      }
    }

    /////////////// MiniMap ///////////////////////
    // The mini map and map info panes split the vertical space, so first define some boundaries.

    // Calculate the map info pane height: numPlayers text height, plus property draw size (2x2).
    int sqSize = SpriteLibrary.baseSpriteSize; // "1 tile" in pixels.
    int MapInfoPaneDrawHeight = characterHeight + (sqSize*2);

    // Figure out how large the map can be based on the border divisions.
    int maxMiniMapWidth = menuWidth - nameSectionDrawWidth;
    int maxMiniMapHeight = menuHeight - MapInfoPaneDrawHeight;

    // Find the center of the minimap display.
    int miniMapCenterX = nameSectionDrawWidth + (maxMiniMapWidth / 2);
    int miniMapCenterY = maxMiniMapHeight / 2;

    // Draw a line to separate the minimap image and the player/property info.
    menuGraphics.setColor(MENUFRAMECOLOR);
    menuGraphics.fillRect(nameSectionDrawWidth, maxMiniMapHeight, menuWidth-nameSectionDrawWidth, 1);

    // Draw the mini-map representation of the highlighted map.
    selectedMapInfo = mapInfos.get(highlightedOption);
    BufferedImage miniMap = MiniMapArtist.getMapImage( selectedMapInfo );

    // Figure out how large to draw the minimap. We want to make it as large as possible, but still
    //   fit inside the available space (with a minimum scale factor of 1).
    int mmWScale = maxMiniMapWidth / miniMap.getWidth();
    int mmHScale = maxMiniMapHeight / miniMap.getHeight();
    int mmScale = (mmWScale > mmHScale)? mmHScale : mmWScale;
    if( mmScale > 10 ) mmScale = 10;

    // Draw the mini map.
    SpriteUIUtils.drawImageCenteredOnPoint(menuGraphics, miniMap, miniMapCenterX, miniMapCenterY, mmScale);

    /////////////// Map Information ///////////////////////
    int buffer = 3;

    int numPlayers = selectedMapInfo.getNumCos(); // Get the number of players the map supports
    StringBuilder sb = new StringBuilder().append(numPlayers).append(" Players"); // Build a string to say that.
    SpriteUIUtils.drawText(menuGraphics, sb.toString(), nameSectionDrawWidth+buffer, maxMiniMapHeight+buffer);

    sb.setLength(0); // Clear so we can build the map dimensions string.
    sb.append(selectedMapInfo.getWidth()).append("x").append(selectedMapInfo.getHeight());
    int dimsDrawX = menuWidth - (characterWidth*7) - 1;
    SpriteUIUtils.drawText(menuGraphics, sb.toString(), dimsDrawX, maxMiniMapHeight+buffer);

    // Draw the number of each type of property on this map.

    int propsDrawX = nameSectionDrawWidth + (2*buffer) - sqSize; // Map name pane plus generous buffer, minus one square (building sprites are 2x2).
    int propsDrawY = maxMiniMapHeight+buffer+(characterHeight)+buffer - (sqSize/2); // Map  pane plus "# players" string plus buffer, minus 1/2sq.

    // Define an array with all the property types we care to enumerate.
    TerrainType[] propertyTypes = {TerrainType.CITY, TerrainType.FACTORY, TerrainType.AIRPORT, TerrainType.SEAPORT};
    for(int i = 0; i < propertyTypes.length; ++i)
    {
      TerrainType terrain = propertyTypes[i];
      int num = countTiles(selectedMapInfo, terrain);
      // Get the first image of the first variation of the specified terrain type.
      BufferedImage image = SpriteLibrary.getTerrainSpriteSet(terrain).getTerrainSprite().getFrame(0);
      // Draw it, with the number of times it occurs on the map.
      menuGraphics.drawImage(image, propsDrawX, propsDrawY, sqSize*2, sqSize*2, null);

      drawPropertyNumber(menuGraphics, num, propsDrawX, propsDrawY);

      // Increment x draw location a fair bit for the next one.
      propsDrawX += sqSize + 3*buffer;
    }

    // Draw to the window at scale.
    g.drawImage(menuImage, 0, 0, menuImage.getWidth()*drawScale, menuImage.getHeight()*drawScale, null);
  }

  /**
   * Count and return the number of tiles of the provided type in the given MapInfo.
   */
  private static int countTiles(MapInfo mapInfo, TerrainType tileType)
  {
    int count = 0;
    for(int y = 0; y < mapInfo.getHeight(); ++y)
    {
      for(int x = 0; x < mapInfo.getWidth(); ++x)
      {
        if(mapInfo.terrain[x][y] == tileType)
        {
          ++count;
        }
      }
    }
    return count;
  }

  /**
   * Utility function to draw a number on top of a property image.
   */
  private static void drawPropertyNumber(Graphics g, int num, int x, int y)
  {
    if(num > 999) // Cap it at 999 for now, and probably forever.
    {
      System.out.println("INFO: Maps are getting large, yo");
      num = 999;
    }

    // Get the number images, and grab the dimensions
    Sprite nums = SpriteLibrary.getMapUnitHPSprites();
    int numWidth = nums.getFrame(0).getWidth();
    int numHeight = nums.getFrame(0).getHeight();

    // Property sprites are 2 tiles by two tiles, so we adjust sideways.
    int propSize = SpriteLibrary.baseSpriteSize*2;
    x += propSize - numWidth; // right-justify
    if (num/100 > 0)
      x += propSize/8; // shift over a bit so we stay centered with 3 digits
    y += propSize - numHeight; // Bottom-justify - no double-digit adjustment.

    do // We divide by 10 and truncate each time; expect three loops max.
    {
      int frame = num % 10;
      g.drawImage(nums.getFrame(frame), x, y, numWidth, numHeight, null);
      num /= 10; // Shift to the next higher digit in the number.
      x -= numWidth; // Move the x-draw location to the left.
    }while(num > 0);
  }
}
