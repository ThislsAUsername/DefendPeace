package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Terrain.GameMap;
import Terrain.MapInfo;

public class SpriteGameSetupMenuArtist
{
  private static Dimension dimensions = null;
  private static int drawScale = 3;

  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);
  private static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private static final int maxNameDisplayLength = 14;
  // The map-name section shall be wide enough for a 14-character name, plus two-pixel buffer on each side.
  // (charWidth+1)*maxNameDisplayLength + 3 (3 not 4, because the kerning buffer for the final letter is 1 already).
  private static int nameSectionWidth =
      ((SpriteLibrary.getLettersUppercase().getFrame(0).getWidth()*drawScale+drawScale) * maxNameDisplayLength) + 3*drawScale;

  public static void setDimensions(Dimension d)
  {
    dimensions = d;
  }

  public static void draw(Graphics g, int highlightedOption)
  {
    if(null == dimensions )
    {
      // If we don't know how big we can draw, don't.
      System.out.println("Warning: SpriteGameSetupMenuArtist has no dimensions!");
      return;
    }

    // Paint the whole area over in our background color.
    g.setColor(MENUBGCOLOR);
    g.fillRect(0,0,dimensions.width,dimensions.height);

    // Draw the frame for the list of maps, with the highlight for the current option.
    int frameBorderHeight = 3*drawScale;
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(nameSectionWidth, 0, drawScale, dimensions.height); // sidebar
    g.fillRect(0,0,nameSectionWidth,frameBorderHeight); // top bar
    g.fillRect(0, dimensions.height-frameBorderHeight, nameSectionWidth, 3*drawScale); // bottom bar
    //TODO: Draw little arrows on the top and bottom frame to show it can scroll.
    g.fillRect(nameSectionWidth, 101*drawScale, dimensions.width-nameSectionWidth, drawScale); // line between map and map info.

    // Draw the highlight for the selected option.
    g.setColor(MENUHIGHLIGHTCOLOR);
    int menuTextYStart = frameBorderHeight + drawScale; // Upper frame border plus 1 pixel buffer.
    int menuOptionHeight = SpriteLibrary.getLettersUppercase().getFrame(0).getHeight()*drawScale;
    int selectedOptionYOffset = menuTextYStart + highlightedOption * (menuOptionHeight+1);
    g.fillRect(0, selectedOptionYOffset, nameSectionWidth, menuOptionHeight);

    // Get the list of selectable maps (possibly specifying a filter (#players, etc).
    ArrayList<MapInfo> mapInfos = new ArrayList<MapInfo>(); // = MapLibrary.getMapNames();
    mapInfos.add(new MapInfo("Sample Map", null)); // TODO: replace this with real stuff.

    // Display the names from the list, highlighting the one that is currently chosen.
    for(int i = 0; i < mapInfos.size(); ++i)
    {
      int drawX = 2; // Offset from the edge of the window slightly.
      int drawY = menuTextYStart + i*(menuOptionHeight+drawScale); // +drawScale for a buffer between options.

      // Draw visible map names in the list.
      String str = mapInfos.get(i).mapName;
      if(str.length() > maxNameDisplayLength)
      {
        str = str.substring(0, maxNameDisplayLength);
      }

      SpriteLibrary.drawText(g, str, drawX, drawY, drawScale);
    }

    // Draw the mini-map representation of the highlighted map
    // Draw any map-specific details (#players, size, facilities, etc.)
  }
}
