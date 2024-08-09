package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.GameInstance;
import UI.InGameMenu;
import UI.MapView;

public class MenuArtist
{
  private GameInstance myGame;
  private MapView myView;

  private int menuHBuffer; // Amount of visible menu to left and right of options;
  private int menuVBuffer; // Amount of visible menu above and below menu options;

  public MenuArtist(GameInstance game, SpriteMapView view)
  {
    myGame = game;

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
      BufferedImage menu = SpriteUIUtils.makeTextMenu(SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR,
          drawMenu.getAllOptions(), drawMenu.getSelectionNumber(), menuHBuffer, menuVBuffer);
      int menuWidth = menu.getWidth();
      int menuHeight = menu.getHeight();

      // Center the menu over the current action target location, accounting for the position of the map view.
      int viewTileSize = SpriteLibrary.baseSpriteSize; // Grab this value for convenience.
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
}
