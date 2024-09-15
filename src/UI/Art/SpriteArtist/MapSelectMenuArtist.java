package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import Terrain.MapInfo;
import Terrain.MapInfo.MapNode;
import Terrain.TerrainType;
import UI.MapSelectController;

public class MapSelectMenuArtist
{
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);
  private static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private static MapInfo selectedMapInfo = null;
  private static SpriteArrows vArrows = new SpriteArrows(true);

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
    int drawableWidth = dimensions.width/drawScale;
    int drawableHeight = dimensions.height/drawScale;
    BufferedImage menuImage = SpriteLibrary.createDefaultBlankSprite(drawableWidth, drawableHeight);
    Graphics menuGraphics = menuImage.getGraphics();
    PixelFont font = SpriteLibrary.getFontStandard();

    int highlightedOption = gameSetup.getSelectedOption();

    /////////////// Map selection pane ///////////////////////
    // Paint the whole area over in our background color.
    menuGraphics.setColor(MENUBGCOLOR);
    menuGraphics.fillRect(0,0,drawableWidth,drawableHeight);

    // Draw the frame for the list of maps, with the highlight for the current option.
    int frameBorderHeight = 3;
    int menuTextYStart = frameBorderHeight;
    int nameSectionDrawWidth = drawableWidth / 3;
    menuGraphics.setColor(MENUFRAMECOLOR);
    menuGraphics.fillRect(nameSectionDrawWidth, 0, 1, drawableHeight); // sidebar
    menuGraphics.fillRect(0,0,nameSectionDrawWidth,frameBorderHeight); // top bar
    menuGraphics.fillRect(0, drawableHeight-frameBorderHeight, nameSectionDrawWidth, frameBorderHeight); // bottom bar

    // Draw the current node's path at the top, in its own little box, if applicable
    String uri = gameSetup.currentNode.uri();
    if( !uri.isEmpty() )
    {
      int uriHeightPlusBorder = font.getHeight()+frameBorderHeight;
      menuGraphics.fillRect(0, uriHeightPlusBorder, nameSectionDrawWidth, frameBorderHeight); // top bar
      int drawX = 2; // Offset from the edge of the window slightly.
      int drawY = (menuTextYStart + 1);
      while (font.getWidth(uri) > nameSectionDrawWidth - drawX)
      {
        uri = uri.substring(0, uri.length() - 1);
      }

      menuGraphics.setColor(Color.BLACK);
      SpriteUIUtils.drawText(menuGraphics, uri, drawX, drawY);

      menuTextYStart += uriHeightPlusBorder;
    }

    // Draw the highlight for the selected option.
    menuGraphics.setColor(MENUHIGHLIGHTCOLOR);
    int menuOptionHeight = font.getHeight();
    int selectedOptionYOffset = menuTextYStart + highlightedOption * (menuOptionHeight);

    // Get the list of selectable maps (possibly specifying a filter (#players, etc).
    ArrayList<MapNode> mapInfos = gameSetup.currentNode.children;
    int verticalShift = 0; // How many map names we skip drawing "off the top"
    int displayableCount = drawableHeight / menuOptionHeight; // how many maps we can cram on the screen
    while (selectedOptionYOffset > drawableHeight/2 && // Loop until either the cursor's bumped up to the center of the screen...
        displayableCount+verticalShift < mapInfos.size()) //  or we'll already show the last map 
    {
      verticalShift++;
      selectedOptionYOffset -= menuOptionHeight;
    }
    
    menuGraphics.fillRect(0, selectedOptionYOffset, nameSectionDrawWidth, menuOptionHeight);

    if( mapInfos.size() > displayableCount ) // Throw some up/down scrolly arrows on, if relevant
    {
      vArrows.set(
          nameSectionDrawWidth - SpriteArrows.ARROW_SIZE, drawableHeight/2,
          drawableHeight - (frameBorderHeight*2 + SpriteArrows.ARROW_SIZE), true);
      vArrows.draw(menuGraphics);
    }

    // Display the names from the list
    for(int i = 0; i < displayableCount; ++i)
    {
      int drawX = 2; // Offset from the edge of the window slightly.
      int drawY = (menuTextYStart+1) + i * (menuOptionHeight);

      // Draw visible map names in the list.
      if (mapInfos.size() > i + verticalShift)
      {
        String str = mapInfos.get(i + verticalShift).name;
        while(font.getWidth(str) > nameSectionDrawWidth - drawX)
        {
          str = str.substring(0, str.length()-1);
        }

        menuGraphics.setColor(Color.BLACK);
        SpriteUIUtils.drawText(menuGraphics, str, drawX, drawY);
      }
    }

    /////////////// MiniMap ///////////////////////
    // The mini map and map info panes split the vertical space, so first define some boundaries.

    // Calculate the map info pane height: numPlayers text height, plus property draw size (2x2).
    int sqSize = SpriteLibrary.baseSpriteSize; // "1 tile" in pixels.
    int MapInfoPaneDrawHeight = font.getHeight() + (sqSize*2);

    // Figure out how large the map can be based on the border divisions.
    int maxMiniMapWidth = drawableWidth - nameSectionDrawWidth - 1;
    int maxMiniMapHeight = drawableHeight - MapInfoPaneDrawHeight;

    // Find the center of the minimap display.
    int miniMapCenterX = nameSectionDrawWidth + ((maxMiniMapWidth+1) / 2);
    int miniMapCenterY = maxMiniMapHeight / 2;

    // Draw a line to separate the minimap image and the player/property info.
    menuGraphics.setColor(MENUFRAMECOLOR);
    menuGraphics.fillRect(nameSectionDrawWidth, maxMiniMapHeight, drawableWidth-nameSectionDrawWidth, 1);

    // Draw the mini-map representation of the highlighted map.
    MapNode selNode = mapInfos.get(highlightedOption);
    while (null == selNode.result)
      selNode = selNode.children.get(0);
    selectedMapInfo = selNode.result;
    BufferedImage miniMap = MiniMapArtist.getMapImage(selectedMapInfo, drawScale*maxMiniMapWidth, drawScale*maxMiniMapHeight);

    // Figure out how large to draw the minimap. We want to make it as large as possible, but still fit inside the available space.
    int mmWScale = drawScale*maxMiniMapWidth / miniMap.getWidth();
    int mmHScale = drawScale*maxMiniMapHeight / miniMap.getHeight();
    int mmScale = (mmWScale > mmHScale)? mmHScale : mmWScale;

    /////////////// Map Information ///////////////////////
    int buffer = 3;

    int numPlayers = selectedMapInfo.getNumCos(); // Get the number of players the map supports
    StringBuilder sb = new StringBuilder().append(numPlayers).append(" Players"); // Build a string to say that.
    SpriteUIUtils.drawText(menuGraphics, sb.toString(), nameSectionDrawWidth+buffer, maxMiniMapHeight+buffer);

    sb.setLength(0); // Clear so we can build the map dimensions string.
    sb.append(selectedMapInfo.getWidth()).append("x").append(selectedMapInfo.getHeight());
    int dimsDrawX = drawableWidth - (font.getWidth(sb.toString())) - 1;
    SpriteUIUtils.drawText(menuGraphics, sb.toString(), dimsDrawX, maxMiniMapHeight+buffer);

    // Draw the number of each type of property on this map.

    int propsDrawX = nameSectionDrawWidth + (2*buffer) - sqSize; // Map name pane plus generous buffer, minus one square (building sprites are 2x2).
    int propsDrawY = maxMiniMapHeight+buffer+(font.getHeight())+buffer - (sqSize/2); // Map  pane plus "# players" string plus buffer, minus 1/2sq.

    // Prepopulate zeroes for some specific property types, so the first few sprites are consistent.
    Map<TerrainType, Integer> propCounts = new LinkedHashMap<>();
    propCounts.put(TerrainType.CITY, 0);
    propCounts.put(TerrainType.FACTORY, 0);
    propCounts.put(TerrainType.AIRPORT, 0);
    propCounts.put(TerrainType.SEAPORT, 0);
    countCapturables(selectedMapInfo, propCounts);
    propCounts.remove(TerrainType.HEADQUARTERS);
    propCounts.remove(TerrainType.LAB);
    for(Entry<TerrainType, Integer> propEntry : propCounts.entrySet())
    {
      TerrainType terrain = propEntry.getKey();
      // Get the first image of the first variation of the specified terrain type.
      BufferedImage image = SpriteLibrary.getTerrainSpriteSet(terrain).getTerrainSprite().getFrame(0);
      // Draw it, with the number of times it occurs on the map.
      menuGraphics.drawImage(image, propsDrawX, propsDrawY, sqSize*2, sqSize*2, null);

      drawPropertyNumber(menuGraphics, propEntry.getValue(), propsDrawX, propsDrawY);

      // Increment x draw location a fair bit for the next one.
      propsDrawX += sqSize + 3*buffer;
    }

    // Draw to the window at scale.
    g.drawImage(menuImage, 0, 0, menuImage.getWidth()*drawScale, menuImage.getHeight()*drawScale, null);

    // Draw the mini map on top.
    SpriteUIUtils.drawImageCenteredOnPoint(g, miniMap, drawScale*miniMapCenterX, drawScale*miniMapCenterY, mmScale);
  }

  private static void countCapturables(MapInfo mapInfo, Map<TerrainType, Integer> propCounts)
  {
    for(int y = 0; y < mapInfo.getHeight(); ++y)
    {
      for(int x = 0; x < mapInfo.getWidth(); ++x)
      {
        TerrainType tt = mapInfo.terrain[x][y];
        if( tt.isCapturable() )
        {
          int oldCount = propCounts.getOrDefault(tt, 0);
          propCounts.put(tt, ++oldCount);
        }
      }
    }
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
    Sprite nums = SpriteLibrary.getMapUnitNumberSprites();
    int numWidth = nums.getFrame(0).getWidth();
    int numHeight = nums.getFrame(0).getHeight();

    // Property sprites are 2 tiles by two tiles, so we adjust sideways.
    int propSize = SpriteLibrary.baseSpriteSize * 2;
    x += propSize - numWidth; // right-justify
    if( num / 10 > 0 )
    {
      x -= numWidth;
      if( num / 100 > 0 )
      {
        x -= numWidth;
        x += propSize / 8; // shift over a bit so we stay centered with 3 digits
      }
    }
    y += propSize - numHeight; // Bottom-justify - no double-digit adjustment.

    BufferedImage numImage = SpriteUIUtils.getNumberAsImage(num);

    g.drawImage(numImage, x, y, numImage.getWidth(), numImage.getHeight(), null);
  }
}
