package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import Engine.IController;
import UI.PlayerSetupColorFactionController;
import UI.SlidingValue;
import UI.UIUtils;

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

  private static PlayerSetupColorFactionController myControl;
  private static SlidingValue gridX = new SlidingValue(0);
  private static SlidingValue gridY = new SlidingValue(0);
  private static SpriteCursor spriteCursor = new SpriteCursor(0, 0, SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize, UIUtils.getCOColors()[0]);

  private static boolean donePreloading = false;
  private static InfantrySpritePreloader isp;
  public static void startSpritePreloader(String name)
  {
    if( !donePreloading && (null == isp) )
    isp = new InfantrySpritePreloader(name);
  }

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
    boolean flipUnits = control.getShouldFlip();

    // Get the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;

    // Start by assuming we can center the unit grid.
    int startX = myWidth / 2 - gridWidth / 2;
    int startY = myHeight / 2 - gridHeight / 2;

    // Figure out if we need to realign things.
    int marginFrac = 2;
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

    if( isp != null )
    {
      isp.join();
      isp = null;
    }

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
            unitArray[f][c] = SpriteLibrary.getMapUnitSpriteSet(myControl.iconicUnitName, UIUtils.getFactions()[f], UIUtils.getCOColors()[c]).sprites[0].getFrame(0);
          int iW = unitArray[f][c].getWidth(), iH = unitArray[f][c].getHeight();
          if( flipUnits )
          {
            myG.drawImage(unitArray[f][c], xOff + iW, yOff, -iW, iH, null);
          }
          else
          {
            myG.drawImage(unitArray[f][c], xOff, yOff, null);
          }
        }
      }

    // Draw the cursors around the selected dude.
    spriteCursor.set(startX+selX, startY+selY, snapCursor);
    final Color playerColor = UIUtils.getCOColors()[color];
    spriteCursor.set(playerColor);
    spriteCursor.draw(myG);

    /////////////// Tag Picker Panels //////////////////////
    if( myControl.shouldSelectMultiCO )
    {
      drawTagPickerPanels(myG, myWidth, playerColor, flipUnits);
    }

    // Draw the currently-selected army's name
    String colorName = UIUtils.getPaletteName(playerColor);
    String factionName = UIUtils.getFactions()[faction].name;

    BufferedImage armyNameImage = SpriteUIUtils.makeTextFrame(UIUtils.getCanonicalFactionName(colorName, factionName), 2, 2);

    int drawNameX = myWidth / 2;
    int drawNameY = armyNameImage.getHeight() / 2 + 2;
    SpriteUIUtils.drawImageCenteredOnPoint(myG, armyNameImage, drawNameX, drawNameY);

    BufferedImage flipTipImage = SpriteUIUtils.makeTextFrame("Q to flip", 2, 2);

    int flipTipY = drawNameY + armyNameImage.getHeight() - 1;
    SpriteUIUtils.drawImageCenteredOnPoint(myG, flipTipImage, drawNameX, flipTipY);

    // Render our image to the screen at the properly-scaled size.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
  }

  public static void drawTagPickerPanels(Graphics myG, int myWidth, Color playerColor, boolean flipUnits)
  {
    // Calculate the vertical space each player panel will consume.
    final int panelThickness = 1;
    final int panelBuffer = 2*panelThickness;
    final int panelWidth  = unitSizePx+panelBuffer;
    final int panelHeight = unitSizePx+panelBuffer;

    // Take over the top of the screen
    SpriteUIUtils.drawMenuFrame(myG, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR,
        0, -1, myWidth, panelHeight*3/2, 2);

    final int panelSpacing = 1;
    final int panelXShift = panelWidth + panelSpacing;

    final int drawY = 4;
    for(int tagToDraw = 0; tagToDraw < myControl.colorPicks.length; ++tagToDraw)
    {
      BufferedImage pic = unitArray[myControl.factionPicks[tagToDraw]][myControl.colorPicks[tagToDraw]];
      int drawX = 4 + (tagToDraw*panelXShift);
      int dx = drawX+panelThickness, dy = drawY+panelThickness;

      int iW = pic.getWidth(), iH = pic.getHeight();
      if( flipUnits )
      {
        myG.drawImage(pic, dx + iW, dy, -iW, iH, null);
      }
      else
      {
        myG.drawImage(pic, dx, dy, null);
      }

      // Draw the cursor over the selected option.
      if( tagToDraw == myControl.tagIndex )
        SpriteCursor.draw(myG, dx, dy, iW, iH, playerColor);
    }

  }

  private static void initialize()
  {
    if( unitArray == null )
    {
      unitArray = new BufferedImage[UIUtils.getFactions().length][UIUtils.getCOColors().length];
    }

    // Just pull one sprite to start with. Loading everything at once might take a while.
    unitArray[0][0] = SpriteLibrary.getMapUnitSpriteSet(myControl.iconicUnitName, UIUtils.getFactions()[0], UIUtils.getCOColors()[0]).sprites[0].getFrame(0);

    unitSizePx = unitArray[0][0].getHeight(); // Units are square.
    unitBuffer = unitSizePx / 3; // Space between options in the grid.
    numFactions = UIUtils.getFactions().length;
    numColors = UIUtils.getCOColors().length;
    gridWidth = numColors*unitSizePx + unitBuffer*(numColors-1);
    gridHeight = numFactions*unitSizePx + unitBuffer*(numFactions-1);
  }

  public static class InfantrySpritePreloader implements Runnable
  {
    private Thread myThread;
    private String myUnitName;

    public InfantrySpritePreloader(String unitName)
    {
      myThread = new Thread(this);
      myThread.start();
      myUnitName = unitName;
    }

    @Override
    public void run()
    {
      for( int nextCol = 0; nextCol < UIUtils.getCOColors().length; ++nextCol )
      {
        for( int nextFac = 0; nextFac < UIUtils.getFactions().length; ++nextFac )
        {
          SpriteLibrary.getMapUnitSpriteSet(myUnitName, UIUtils.getFactions()[nextFac], UIUtils.getCOColors()[nextCol]);
        }
      }
      donePreloading = true;
    }

    public void join()
    {
      try
      {
        myThread.join();
      }
      catch(InterruptedException ie)
      {
        System.out.println("[InfantrySpritePreloader] Exception! Details:\n  " + ie.toString());
      }
    }
  }
}
