package UI.Art.SpriteArtist;

import java.awt.Color;
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
  public void drawMenu(Graphics g)
  {
    InGameMenu<? extends Object> drawMenu = myView.getCurrentGameMenu();
    if( drawMenu != null )
    {
      // Check if we need to build the menu options again.
      if( drawMenu.wasReset() || myCurrentMenu != drawMenu )
      {
        myCurrentMenu = drawMenu;
        myCurrentMenuStrings.clear();
        SpriteUIUtils.getMenuStrings(myCurrentMenu, myCurrentMenuStrings);
      }

      SpriteUIUtils.drawTextMenu(g, MENUBGCOLOR, MENUFRAMECOLOR, MENUHIGHLIGHTCOLOR,
          myCurrentMenuStrings, myCurrentMenu.getSelectionNumber(), myGame.getCursorX(), myGame.getCursorY(), menuHBuffer, menuVBuffer);
    }
  }
}
