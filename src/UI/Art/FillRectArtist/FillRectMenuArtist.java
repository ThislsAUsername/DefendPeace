package UI.Art.FillRectArtist;

import java.awt.Color;
import java.awt.Graphics;

import Engine.GameInstance;
import UI.InGameMenu;
import UI.MapView;
import Units.UnitModel.UnitEnum;

public class FillRectMenuArtist
{
  private int tileSizePx;
  private int mapViewWidth;
  private int mapViewHeight;

  GameInstance myGame;
  MapView myView;

  public static final Color COLOR_CURSOR = new Color(253, 171, 77, 200);

  public FillRectMenuArtist(GameInstance game)
  {
    myGame = game;
  }

  public void setView(FillRectMapView view)
  {
    myView = view;
    tileSizePx = view.getTileSize();
    mapViewWidth = view.getViewWidth();
    mapViewHeight = view.getViewHeight();
  }

  public void drawMenu(Graphics g)
  {
    int menuBorderLeft = mapViewWidth / 4;
    int menuBorderTop = mapViewHeight / 4;
    int menuWidth = mapViewWidth / 2;
    int menuHeight = mapViewHeight / 2;

    g.setColor(Color.black); // outer border
    g.fillRect(menuBorderLeft, menuBorderTop, menuWidth, menuHeight);
    g.setColor(Color.cyan); // inner fill
    g.fillRect(menuBorderLeft + 1, menuBorderTop + 1, menuWidth - 2, menuHeight - 2);
    String label;

    InGameMenu<? extends Object> currentMenu = myView.getCurrentGameMenu();
    if( null != currentMenu )
    {
      int yOffset = 1; // Helps to guide where we draw the menu options. Will offset further if we draw money first.
      if( currentMenu.getSelectedOption() instanceof Units.UnitModel )
      {
        // This is a PRODUCTION menu. Draw current funds.
        yOffset++; // Make room for the funds string.
        g.setColor( Color.MAGENTA );
        label = new String("Money: " + myGame.activeCO.money);
        g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft + 4, tileSizePx / 2 + menuBorderTop);
      }
      
      // Draw the highlight behind the currently-selected option.
      g.setColor(COLOR_CURSOR);
      g.fillRect(menuBorderLeft + 1, (currentMenu.getSelectionNumber() + yOffset - 1) * tileSizePx / 2 + menuBorderTop + 4,
          menuWidth - 2, tileSizePx / 2);

      // Draw menu options.
      g.setColor(Color.black);
      for( int i = 0; i < currentMenu.getNumOptions(); i++ )
      {
        label = currentMenu.getOptionString(i);
        g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft + 4, (i + yOffset) * tileSizePx / 2 + menuBorderTop);
      }
    }
    else
    { // No menu is currently active. We should not be here.
        g.setColor(Color.black);
        System.out.println("WARNING! FillRectMenuArtist was given a null menu!!");
        label = new String("Undefined Menu!");
        g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft + 4, tileSizePx / 2 + menuBorderTop);
    }
  }
}
