package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import Engine.GameInstance;
import UI.InGameMenu;
import UI.MapView;

public class SpriteMenuArtist
{
  private GameInstance myGame;
  private MapView myView;
  private InGameMenu<? extends Object> myCurrentMenu;
  private ArrayList<String> myCurrentMenuStrings;
  private int drawScale;

  private final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private final Color MENUBGCOLOR = new Color(234, 204, 154);
  private final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private int menuTextWidth;
  private int menuTextHeight;
  private int menuHBuffer; // Amount of visible menu to left and right of options;
  private int menuVBuffer; // Amount of visible menu above and below menu options;

  public SpriteMenuArtist(GameInstance game, SpriteMapView view)
  {
    myGame = game;
    myCurrentMenu = null;
    myCurrentMenuStrings = new ArrayList<String>();

    myView = view;

    // Get the draw scale, and figure out the resulting "real" text size, etc.
    drawScale = SpriteOptions.getDrawScale();
    menuTextWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth() * drawScale;
    menuTextHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight() * drawScale;
    menuHBuffer = 3 * drawScale; // Amount of visible menu to left and right of options;
    menuVBuffer = 4 * drawScale; // Amount of visible menu above and below menu options;
  }

  /**
   * Draw the menu, centered around the location of interest.
   */
  public void drawMenu(Graphics g, int mapViewX, int mapViewY)
  {
    InGameMenu<? extends Object> drawMenu = myView.getCurrentGameMenu();
    if( drawMenu != null )
    {
      // Check if we need to build the menu options again.
      if( drawMenu.wasReset() || myCurrentMenu != drawMenu )
      {
        myCurrentMenu = drawMenu;
        myCurrentMenuStrings.clear();
        getMenuStrings(myCurrentMenu, myCurrentMenuStrings);
      }

      // Find the dimensions of the menu we are drawing.
      int menuWidth = getMenuTextWidthPx(myCurrentMenuStrings) + menuHBuffer * 2;
      int menuHeight = getMenuTextHeightPx(myCurrentMenuStrings) + menuVBuffer * 2;

      // Center the menu over the current action target location, accounting for the position of the map view.
      int viewTileSize = myView.getTileSize(); // Grab this value for convenience.
      int drawX = myGame.getCursorX() * viewTileSize - (menuWidth / 2 - viewTileSize / 2);// - mapViewX;
      int drawY = myGame.getCursorY() * viewTileSize - (menuHeight / 2 - viewTileSize / 2);// - mapViewY;

      // Make sure the menu is fully contained in viewable space.
      Dimension dims = SpriteOptions.getScreenDimensions();
      drawX = (drawX < mapViewX) ? mapViewX : (drawX > (mapViewX+dims.width - menuWidth)) ? (mapViewX+dims.width - menuWidth) : drawX;
      drawY = (drawY < mapViewY) ? mapViewY : (drawY > (mapViewY+dims.height - menuHeight)) ? (mapViewY+dims.height - menuHeight) : drawY;

      // Draw the nice box for our text.
      drawMenuFrame(g, drawX, drawY, menuWidth, menuHeight);

      // Draw the highlight for the currently-selected option.
      // selY = drawY plus upper menu-frame buffer, plus (letter height, plus 1px-buffer, times number of options).
      int selY = drawY + menuVBuffer + (menuTextHeight + drawScale) * myCurrentMenu.getSelectionNumber();
      g.setColor(MENUHIGHLIGHTCOLOR);
      g.fillRect(drawX, selY, menuWidth, menuTextHeight);

      // Draw the actual menu text.
      for( int txtY = drawY + menuVBuffer, i = 0; i < myCurrentMenu.getNumOptions(); ++i, txtY += menuTextHeight + drawScale )
      {
        SpriteLibrary.drawTextSmallCaps(g, myCurrentMenuStrings.get(i), drawX + menuHBuffer, txtY, drawScale);
      }
    }
  }

  /**
   * Populate 'out' with the string versions of the options available through 'menu'.
   * @param menu
   * @param out
   */
  private void getMenuStrings(InGameMenu<? extends Object> menu, ArrayList<String> out)
  {
    for( int i = 0; i < menu.getNumOptions(); ++i )
    {
      String str = menu.getOptionString(i);
      out.add(str);
    }
  }

  private void drawMenuFrame(Graphics g, int x, int y, int w, int h)
  {
    int menuFrameHeight = menuVBuffer / 2; // Upper and lower bit can look framed.

    g.setColor(MENUBGCOLOR);
    g.fillRect(x, y, w, h); // Main menu body;
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(x, y, w, menuFrameHeight); // Upper frame;
    g.fillRect(x, y + h - menuFrameHeight, w, menuFrameHeight); // Lower frame;
  }

  private int getMenuTextWidthPx(ArrayList<String> menuOptions)
  {
    int maxWidth = 0;
    for( int i = 0; i < menuOptions.size(); ++i )
    {
      int optw = menuOptions.get(i).length() * menuTextWidth;
      maxWidth = (optw > maxWidth) ? optw : maxWidth;
    }

    return maxWidth;
  }

  private int getMenuTextHeightPx(ArrayList<String> menuOptions)
  {
    // Height of the letters plus 1 (for buffer between menu options), times the number of entries,
    // minus 1 because there is no buffer after the last entry.
    return (menuTextHeight + drawScale) * menuOptions.size() - drawScale;
  }
}
