package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import Engine.XYCoord;
import UI.InGameMenu;

public class SpriteUIUtils
{
  /**
   * Calculate the distance to move an image make it look like it slides quickly into place instead of
   * just snapping at each button-press. Distance moved per frame is proportional to distance from goal location.
   * It is expected that currentNum and targetNum will not differ by more than 1.0.
   * NOTE: currentNum and targetNum correspond to (relative) positions, not to pixels.
   * NOTE: This is calibrated for 60fps, and changing the frame rate will change the closure speed.
   * 
   */
  public static double calculateSlideAmount(double currentNum, int targetNum)
  {
    double animMoveFraction = 0.3; // Movement cap to prevent over-speedy menu movement.
    double animSnapDistance = 0.05; // Minimum distance at which point we just snap into place.
    double slide = 0; // Return value; the distance we actually are going to move.
    double diff = Math.abs(targetNum - currentNum);
    int sign = (targetNum > currentNum) ? 1 : -1; // Since we took abs(), make sure we can correct the sign.
    if( diff < animSnapDistance )
    { // If we are close enough, just move the exact distance.
      slide = diff;
    }
    else
    { // Move a fixed fraction of the remaining distance.
      slide = diff * animMoveFraction;
    }

    return slide * sign;
  }

  public static void drawBasicTextFrame(Graphics g, String item, double mapX, double mapY, int hBuffer, int vBuffer)
  {
    drawTextFrame(g, SpriteMenuArtist.MENUBGCOLOR, SpriteMenuArtist.MENUFRAMECOLOR, item, mapX, mapY, hBuffer, vBuffer);
  }
  public static void drawTextFrame(Graphics g, Color bg, Color frame, String item, double mapX, double mapY, int hBuffer, int vBuffer)
  {
    ArrayList<String> items = new ArrayList<String>();
    items.add(item);
    drawTextMenu(g, bg, frame, bg, items, 0, mapX, mapY, hBuffer, vBuffer);
  }
  public static void drawBasicTextMenu(Graphics g, ArrayList<String> items, int selection, double mapX, double mapY)
  {
    drawTextMenu(g, SpriteMenuArtist.MENUBGCOLOR, SpriteMenuArtist.MENUFRAMECOLOR, SpriteMenuArtist.MENUHIGHLIGHTCOLOR, items,
        selection, mapX, mapY, SpriteMenuArtist.menuHBuffer, SpriteMenuArtist.menuVBuffer);
  }
  public static void drawTextMenu(Graphics g, Color bg, Color frame, Color focus, ArrayList<String> items, int selection, double mapX, double mapY, int hBuffer, int vBuffer)
  {
    {
      XYCoord visualOrigin = SpriteMapView.getVisualOrigin();
      // Find the dimensions of the menu we are drawing.
      int menuWidth = getMenuTextWidthPx(items) + hBuffer * 2;
      int menuHeight = getMenuTextHeightPx(items) + vBuffer * 2;

      // Center the menu over the current action target location, accounting for the position of the map view.
      int viewTileSize = getTileSize(); // Grab this value for convenience.
      int drawX = (int) (mapX * viewTileSize - (menuWidth / 2 - viewTileSize / 2));// - origin.xCoord;
      int drawY = (int) (mapY * viewTileSize - (menuHeight / 2 - viewTileSize / 2));// - origin.yCoord;

      // Make sure the menu is fully contained in viewable space.
      Dimension dims = SpriteOptions.getScreenDimensions();
      drawX = (drawX < visualOrigin.xCoord) ? visualOrigin.xCoord
          : (drawX > (visualOrigin.xCoord + dims.width - menuWidth)) ? (visualOrigin.xCoord + dims.width - menuWidth) : drawX;
      drawY = (drawY < visualOrigin.yCoord) ? visualOrigin.yCoord
          : (drawY > (visualOrigin.yCoord + dims.height - menuHeight)) ? (visualOrigin.yCoord + dims.height - menuHeight) : drawY;

      // Draw the nice box for our text.
      drawMenuFrame(g, bg, frame, drawX, drawY, menuWidth, menuHeight, vBuffer);

      // Draw the highlight for the currently-selected option.
      // selY = drawY plus upper menu-frame buffer, plus (letter height, plus 1px-buffer, times number of options).
      int selY = drawY + vBuffer
          + (SpriteMenuArtist.menuTextHeight + SpriteOptions.getDrawScale()) * selection;
      g.setColor(focus);
      g.fillRect(drawX, selY, menuWidth, SpriteMenuArtist.menuTextHeight);

      // Draw the actual menu text.
      for( int txtY = drawY + vBuffer, i = 0; i < items.size(); ++i, txtY += SpriteMenuArtist.menuTextHeight
          + SpriteOptions.getDrawScale() )
      {
        SpriteLibrary.drawTextSmallCaps(g, items.get(i), drawX + hBuffer, txtY,
            SpriteOptions.getDrawScale());
      }
    }
  }

  /**
   * Populate 'out' with the string versions of the options available through 'menu'.
   * @param menu
   * @param out
   */
  public static void getMenuStrings(InGameMenu<? extends Object> menu, ArrayList<String> out)
  {
    for( int i = 0; i < menu.getNumOptions(); ++i )
    {
      String str = menu.getOptionString(i);
      out.add(str);
    }
  }

  public static void drawMenuFrame(Graphics g, Color bg, Color frame, int x, int y, int w, int h, int vBuffer)
  {
    int menuFrameHeight = vBuffer / 2; // Upper and lower bit can look framed.

    g.setColor(bg);
    g.fillRect(x, y, w, h); // Main menu body;
    g.setColor(frame);
    g.fillRect(x, y, w, menuFrameHeight); // Upper frame;
    g.fillRect(x, y + h - menuFrameHeight, w, menuFrameHeight); // Lower frame;
  }

  public static int getMenuTextWidthPx(ArrayList<String> menuOptions)
  {
    int maxWidth = 0;
    for( int i = 0; i < menuOptions.size(); ++i )
    {
      int optw = menuOptions.get(i).length() * SpriteMenuArtist.menuTextWidth;
      maxWidth = (optw > maxWidth) ? optw : maxWidth;
    }

    return maxWidth;
  }

  public static int getMenuTextHeightPx(ArrayList<String> menuOptions)
  {
    // Height of the letters plus 1 (for buffer between menu options), times the number of entries,
    // minus 1 because there is no buffer after the last entry.
    return (SpriteMenuArtist.menuTextHeight + SpriteOptions.getDrawScale()) * menuOptions.size() - SpriteOptions.getDrawScale();
  }

  public static int getTileSize()
  {
    return SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale();
  }
}
