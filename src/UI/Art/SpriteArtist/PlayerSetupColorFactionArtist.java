package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.IController;
import UI.PlayerSetupColorFactionController;
import UI.UIUtils;
import Units.UnitModel;

public class PlayerSetupColorFactionArtist
{
  // Keep track of all the unit images we need to juggle.
  private static BufferedImage[][] unitArray;
  private static int unitSizePx; // Units are square
  private static int unitBuffer;
  private static int numFactions;
  private static int numColors;
  private static int gridWidth;
  private static int gridHeight;

  public static void draw(Graphics g, IController controller)
  {
    PlayerSetupColorFactionController control = (PlayerSetupColorFactionController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupColorFactionArtist was given the wrong controller!");
    }

    // Make sure we have all the things we need to draw.
    initialize();

    int color = control.getSelectedColor();
    int faction = control.getSelectedFaction();

    // Get the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;

    // Start by assuming we can center the unit grid.
    int startX = myWidth / 2 - gridWidth / 2;
    int startY = myHeight / 2 - gridHeight / 2;

    // Figure out if we need to realign things.
    int maxX = myWidth - myWidth/3;
    int maxY = myHeight - myHeight/3;
    int minX = myWidth/3;
    int minY = myHeight/3;
    int selX = color*unitSizePx + unitBuffer*(color);
    int selY = faction*unitSizePx + unitBuffer*(faction);

    if( (startX + selX) > maxX )
    {
      startX -= (startX + selX) - maxX;
    }
    if( (startX + selX) < minX )
    {
      startX += minX - (startX + selX );
    }
    if( (startY + selY) > maxY )
    {
      startY -= (startY + selY) - maxY;
    }
    if( (startY + selY) < minY )
    {
      startY += minY - (startY + selY );
    }

    // Render it in true size, and then we'll scale it to window size as we draw it.
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    for( int c = 0; c < UIUtils.getCOColors().length; ++c)
      for(int f = 0; f < UIUtils.getFactions().length; ++f)
      {
        // Figure out where this should be drawn.
        int xOff = c*unitSizePx + unitBuffer*(c) + startX;
        int yOff = f*unitSizePx + unitBuffer*(f) + startY;

        // Only draw on-screen options.
        if( (xOff < myWidth) && (yOff < myWidth) && (xOff > -unitSizePx) && (yOff > -unitSizePx))
        {
          if( null == unitArray[f][c] )
            unitArray[f][c] = SpriteLibrary.getMapUnitSpriteSet(UnitModel.UnitEnum.INFANTRY, UIUtils.getFactions()[f], UIUtils.getCOColors()[c]).sprites[0].getFrame(0);
          myG.drawImage(unitArray[f][c], xOff, yOff, null);
        }
      }

    // Draw the cursors around the selected dude.
    SpriteUIUtils.drawCursor(myG, startX + selX, startY + selY, unitSizePx, unitSizePx, UIUtils.getCOColors()[control.getSelectedColor()]);

    // Render our image to the screen at the properly-scaled size.
    g.drawImage(image, 0, 0, dimensions.width, dimensions.height, null);
  }

  private static void initialize()
  {
    if( unitArray == null )
    {
      unitArray = new BufferedImage[UIUtils.getFactions().length][UIUtils.getCOColors().length];
    }

    // Just pull one sprite to start with. Loading everything at once might take a while.
    unitArray[0][0] = SpriteLibrary.getMapUnitSpriteSet(UnitModel.UnitEnum.INFANTRY, UIUtils.getFactions()[0], UIUtils.getCOColors()[0]).sprites[0].getFrame(0);

    unitSizePx = unitArray[0][0].getHeight(); // Units are square.
    unitBuffer = unitSizePx / 3; // Space between options in the grid.
    numFactions = UIUtils.getFactions().length;
    numColors = UIUtils.getCOColors().length;
    gridWidth = numColors*unitSizePx + unitBuffer*(numColors-1);
    gridHeight = numFactions*unitSizePx + unitBuffer*(numFactions-1);
  }
}
