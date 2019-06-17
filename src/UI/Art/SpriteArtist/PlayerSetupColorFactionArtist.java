package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.IController;
import UI.PlayerSetupColorFactionController;
import UI.SlidingValue;
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

  private static IController myControl;
  private static SlidingValue gridX = new SlidingValue(0);
  private static SlidingValue gridY = new SlidingValue(0);
  private static SpriteCursor spriteCursor = new SpriteCursor(0, 0, SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize, UIUtils.getCOColors()[0]);

  public static void draw(Graphics g, IController controller)
  {
    PlayerSetupColorFactionController control = (PlayerSetupColorFactionController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupColorFactionArtist was given the wrong controller!");
    }
    boolean snapCursor = myControl != control;
    myControl = control;

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
    int marginFrac = 8;
    int maxX = myWidth - myWidth/marginFrac - unitSizePx;
    int maxY = myHeight - myHeight/marginFrac - unitSizePx;
    int minX = myWidth/marginFrac;
    int minY = myHeight/marginFrac;
    int selX = color*(unitSizePx + unitBuffer);
    int selY = faction*(unitSizePx + unitBuffer);

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

    gridX.set(startX, snapCursor);
    gridY.set(startY, snapCursor);

    // Render it in true size, and then we'll scale it to window size as we draw it.
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    for( int c = 0; c < UIUtils.getCOColors().length; ++c)
      for(int f = 0; f < UIUtils.getFactions().length; ++f)
      {
        // Figure out where this should be drawn.
        int xOff = c*unitSizePx + unitBuffer*(c) + gridX.geti();
        int yOff = f*unitSizePx + unitBuffer*(f) + gridY.geti();

        // Only draw on-screen options.
        if( (xOff < myWidth) && (yOff < myHeight) && (xOff > -unitSizePx) && (yOff > -unitSizePx))
        {
          if( null == unitArray[f][c] )
            unitArray[f][c] = SpriteLibrary.getMapUnitSpriteSet(UnitModel.UnitEnum.INFANTRY, UIUtils.getFactions()[f], UIUtils.getCOColors()[c]).sprites[0].getFrame(0);
          myG.drawImage(unitArray[f][c], xOff, yOff, null);
        }
      }

    // Draw the cursors around the selected dude.
    spriteCursor.set(startX+selX, startY+selY, snapCursor);
    spriteCursor.set(UIUtils.getCOColors()[control.getSelectedColor()]);
    spriteCursor.draw(myG);

    // Render our image to the screen at the properly-scaled size.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
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

  private static int nextFac = 0;
  private static int nextCol = 0;
  private static boolean donePreloading = false;
  /** This can be called repeatedly on the sly to get all infantry sprites
   * in memory and make this option screen less sluggish on first entry. */
  public static boolean preloadOneInfantrySprite()
  {
    if( !donePreloading )
    {
      SpriteLibrary.getMapUnitSpriteSet(UnitModel.UnitEnum.INFANTRY, UIUtils.getFactions()[nextFac], UIUtils.getCOColors()[nextCol]);
      nextFac++;
      if( nextFac >= UIUtils.getFactions().length )
      {
        nextFac = 0;
        nextCol++;
        if( nextCol >= UIUtils.getCOColors().length )
          donePreloading = true;
      }
    }
    return donePreloading;
  }
}
