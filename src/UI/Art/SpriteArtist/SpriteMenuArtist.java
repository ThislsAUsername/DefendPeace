package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import Engine.GameInstance;
import Engine.XYCoord;
import UI.InGameMenu;
import UI.MapView;

public class SpriteMenuArtist
{
  private GameInstance myGame;
  private MapView myView;
  private InGameMenu<? extends Object> myCurrentMenu;
  private ArrayList<String> myCurrentMenuStrings;
  private int drawScale;

  public static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  public static final Color MENUBGCOLOR = new Color(234, 204, 154);
  public static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  public static int menuTextWidth;
  public static int menuTextHeight;
  public static int menuHBuffer; // Amount of visible menu to left and right of options;
  public static int menuVBuffer; // Amount of visible menu above and below menu options;

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
      
      SpriteUIUtils.drawBasicTextMenu(g, myCurrentMenuStrings, myCurrentMenu.getSelectionNumber(), myGame.getCursorX(), myGame.getCursorY());
    }
  }
}
