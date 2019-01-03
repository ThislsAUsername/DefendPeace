package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
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

      BufferedImage menu = SpriteUIUtils.makeTextMenu(MENUBGCOLOR, MENUFRAMECOLOR, MENUHIGHLIGHTCOLOR,
          myCurrentMenuStrings, myCurrentMenu.getSelectionNumber(), menuHBuffer, menuVBuffer);
      int menuWidth = menu.getWidth();
      int menuHeight = menu.getHeight();

      // Center the menu over the current action target location, accounting for the position of the map view.
      int viewTileSize = myView.getTileSize(); // Grab this value for convenience.
      int drawX = myGame.getCursorX() * viewTileSize - (menu.getWidth() / 2 - viewTileSize / 2);
      int drawY = myGame.getCursorY() * viewTileSize - (menu.getHeight() / 2 - viewTileSize / 2);

      // Make sure the menu is fully contained in viewable space.
      Dimension dims = SpriteOptions.getScreenDimensions();
      drawX = (drawX < mapViewX) ? mapViewX : (drawX > (mapViewX+dims.width - menuWidth)) ? (mapViewX+dims.width - menuWidth) : drawX;
      drawY = (drawY < mapViewY) ? mapViewY : (drawY > (mapViewY+dims.height - menuHeight)) ? (mapViewY+dims.height - menuHeight) : drawY;

      g.drawImage(menu, drawX, drawY, null);
    }
  }

  /**
   * Populate 'out' with the string versions of the options available through 'menu'.
   * @param menu
   * @param out
   */
  private static void getMenuStrings(InGameMenu<? extends Object> menu, ArrayList<String> out)
  {
    for( int i = 0; i < menu.getNumOptions(); ++i )
    {
      String str = menu.getOptionString(i);
      out.add(str);
    }
  }
}
