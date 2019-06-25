package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Engine.GameInstance;
import UI.InGameMenu;
import UI.MapView;

public class MenuArtist
{
  private GameInstance myGame;
  private MapView myView;
  private InGameMenu<? extends Object> myCurrentMenu;
  private ArrayList<String> myCurrentMenuStrings;

  private int menuHBuffer; // Amount of visible menu to left and right of options;
  private int menuVBuffer; // Amount of visible menu above and below menu options;

  public MenuArtist(GameInstance game, SpriteMapView view)
  {
    myGame = game;
    myCurrentMenu = null;
    myCurrentMenuStrings = new ArrayList<String>();

    myView = view;

    // Get the draw scale, and figure out the resulting "real" text size, etc.
    menuHBuffer = 3; // Amount of visible menu to left and right of options;
    menuVBuffer = 4; // Amount of visible menu above and below menu options;
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

      BufferedImage menu = SpriteUIUtils.makeTextMenu(SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR,
          myCurrentMenuStrings, myCurrentMenu.getSelectionNumber(), menuHBuffer, menuVBuffer);
      int menuWidth = menu.getWidth();
      int menuHeight = menu.getHeight();

      // Center the menu over the current action target location, accounting for the position of the map view.
      int viewTileSize = myView.getTileSize(); // Grab this value for convenience.
      int drawX = myGame.getCursorX() * viewTileSize - (menuWidth / 2 - viewTileSize / 2);
      int drawY = myGame.getCursorY() * viewTileSize - (menuHeight / 2 - viewTileSize / 2);

      // Make sure the menu is fully contained in viewable space.
      Dimension dims = SpriteOptions.getScreenDimensions();
      // Keep X inside the view
      drawX = Math.max(drawX, mapViewX * viewTileSize);                                   // left
      drawX = Math.min(drawX, mapViewX * viewTileSize + dims.width - menuWidth);          // right 
      // Keep Y inside the view
      drawY = Math.max(drawY, mapViewY * viewTileSize);                                   // top
      drawY = Math.min(drawY, mapViewY * viewTileSize + dims.height - menuHeight);        // bottom
      // Keep X/Y inside the map; this is needed since the view can be larger than the map
      drawX -= Math.max(0, drawX + menuWidth - myGame.gameMap.mapWidth * viewTileSize);   // right
      drawY -= Math.max(0, drawY + menuHeight - myGame.gameMap.mapHeight * viewTileSize); // bottom

      g.drawImage(menu, drawX, drawY, menuWidth, menuHeight, null);
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
