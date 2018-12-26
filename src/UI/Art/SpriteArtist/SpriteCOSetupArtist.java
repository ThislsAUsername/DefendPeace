package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.CommanderLibrary;
import Engine.XYCoord;
import Terrain.MapInfo;
import UI.COSetupController;

public class SpriteCOSetupArtist
{
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);

  private static double animHighlightedPlayer = 0;

//  private static long lastDrawTime = 0;

  private static COSetupController myControl = null;

  public static void draw(Graphics g, MapInfo mapInfo, COSetupController control)
  {
    // If control has changed, we just entered a new CO setup screen. We don't want to
    //   animate a menu transition based on the last time we were choosing COs, since
    //   this class is static, but the CO select screen is not.
    if(myControl != control)
    {
      animHighlightedPlayer = control.getHighlightedPlayer();
      myControl = control;
    }

    // Get the draw space
    Dimension dimensions = SpriteOptions.getScreenDimensions();

    // Draw fancy background effects.
    DiagonalBlindsBG.draw(g);

    /////////////////// MiniMap ////////////////////////
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage miniMap = SpriteMiniMapArtist.getMapImage( mapInfo );

    // Figure out how large to draw the minimap. Make it as large as possible, allowing
    // space for the CO selection area below.
    int COSelectionAreaHeight = SpriteLibrary.getCommanderSprites("strong").head.getHeight()*2*drawScale;
    int minimapAreaHeight = dimensions.height - COSelectionAreaHeight;

    // Figure out how large to draw the minimap. We want to make it as large as possible, but still
    //   fit inside the available space (with a minimum scale factor of 1).
    int maxMiniMapWidth = dimensions.width - 4; // Subtract 4 so we have room to draw a frame.
    int maxMiniMapHeight = dimensions.height - COSelectionAreaHeight - 4;
    int mmWScale = maxMiniMapWidth / miniMap.getWidth();
    int mmHScale = maxMiniMapHeight / miniMap.getHeight();
    int mmScale = (mmWScale > mmHScale)? mmHScale : mmWScale;
    if( mmScale > 10*drawScale) mmScale = 10*drawScale;

    // Draw a frame for the minimap.
    int mapWidth = miniMap.getWidth() * mmScale;
    int mapHeight = miniMap.getHeight() * mmScale;
    int mapLeft = (dimensions.width / 2) - (mapWidth / 2);
    int mapTop = (minimapAreaHeight / 2) - (mapHeight / 2);
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(mapLeft-(2*drawScale), mapTop-(2*drawScale), mapWidth+(4*drawScale), mapHeight+(4*drawScale));
    g.setColor(MENUBGCOLOR);
    g.fillRect(mapLeft-drawScale, mapTop-drawScale, mapWidth+(2*drawScale), mapHeight+(2*drawScale));

    // Draw the mini map.
    SpriteLibrary.drawImageCenteredOnPoint(g, miniMap, dimensions.width / 2, minimapAreaHeight / 2, mmScale);

    // Define the space to draw the list of player CO portraits.
    int drawYCenter = ((dimensions.height - minimapAreaHeight) / 2) + minimapAreaHeight;
    int coHighlightedPortraitXCenter = dimensions.width / 2; // Whichever player has focus should be centered.

    drawSelectorArrows(g, dimensions.width / 2, drawYCenter);

    /////////////////// CO Portraits ///////////////////////
    int numCOs = mapInfo.getNumCos();
    int highlightedPlayer = myControl.getHighlightedPlayer();

    double numCosOnScreen = 5.5; // Display 5 cos, and the edge of the portrait for any hanging off the screen edge.
    int xSpacing = (int)(dimensions.width / numCosOnScreen);

    // If we are moving from one option to another, calculate the intermediate draw location.
    if( animHighlightedPlayer != highlightedPlayer )
    {
      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedPlayer, highlightedPlayer);
      animHighlightedPlayer += slide;
    }

    // Find where the zeroth player CO should be drawn.
    // Shift from the center location by the spacing times the number of the highlighted option.
    int drawXCenter = (int)(coHighlightedPortraitXCenter - (animHighlightedPlayer * xSpacing)); 

    for(int i = 0; i < numCOs; ++i, drawXCenter += xSpacing)
    {
      int co = myControl.getPlayerCo(i);
      int col = myControl.getPlayerColor(i);
      // Draw the box, the color, and the CO portrait.
      drawCoPortrait(g, co, col, drawXCenter, drawYCenter);

      String faction = COSetupController.spriteSetKeys[myControl.getPlayerFaction(i)];
      
      int menuTextWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
      int menuTextHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();

      int signWidth = ((menuTextWidth * faction.length()) + 4) * drawScale;
      int signHeight = (menuTextHeight + 4) * drawScale;
      
      XYCoord signTopLeft = new XYCoord(drawXCenter - signWidth / 2, drawYCenter + COSelectionAreaHeight/4 + drawScale*5 - signHeight / 2);
      
      SpriteLibrary.drawTextSmallCaps(g, faction, signTopLeft.xCoord + 2 * drawScale, signTopLeft.yCoord + 2 * drawScale,
          drawScale);
    }
  }

  private static void drawSelectorArrows(Graphics g, int xCenter, int yCenter)
  {
    int drawScale = SpriteOptions.getDrawScale();
    // Polygons for arrows to indicate the focused player slot. CO face images are 32x32, plus two pixels
    // for the frame border, plus two pixels between the portrait frame and the arrows.
    int yBuffer = SpriteLibrary.getCommanderSprites("strong").head.getHeight() / 2 + 4;
    int[] upXPoints = {xCenter-(4*drawScale), xCenter, xCenter+drawScale, xCenter+(5*drawScale)};
    int[] upYPoints = {yCenter-(yBuffer*drawScale), yCenter-((yBuffer+4)*drawScale), yCenter-((yBuffer+4)*drawScale), yCenter-(yBuffer*drawScale)};
    int[] dnXPoints = {xCenter-(4*drawScale), xCenter, xCenter+drawScale, xCenter+(5*drawScale)};
    int[] dnYPoints = {yCenter+(yBuffer*drawScale)+ drawScale*3, yCenter+((yBuffer+4)*drawScale)+ drawScale*3, yCenter+((yBuffer+4)*drawScale)+ drawScale*3, yCenter+(yBuffer*drawScale)+ drawScale*3};

    // Draw the arrows around the focused player slot.
    g.setColor(MENUFRAMECOLOR);
    g.fillPolygon(upXPoints, upYPoints, upXPoints.length);
    g.fillPolygon(dnXPoints, dnYPoints, dnXPoints.length);
  }

  private static void drawCoPortrait(Graphics g, int co, int color, int xCenter, int yCenter)
  {
    // Fetch CO portrait
    BufferedImage portrait = SpriteLibrary.getCommanderSprites( CommanderLibrary.getCommanderList().get(co).name ).head;
    int drawScale = SpriteOptions.getDrawScale();

    int drawX = xCenter - ( (portrait.getWidth()*drawScale) / 2 );
    int drawY = yCenter - ( (portrait.getHeight()*drawScale) / 2);
    int drawW = portrait.getWidth() * drawScale;
    int drawH = portrait.getHeight() * drawScale;

    // Only bother to draw it if it is onscreen.
    if( (drawX+drawW > 0) && ( drawX < SpriteOptions.getScreenDimensions().getWidth() ) )
    {
      // Draw frame box
      g.setColor( MENUFRAMECOLOR );
      g.fillRect(drawX - (2*drawScale), drawY - (2*drawScale), drawW + (4*drawScale), drawH + (4*drawScale));

      // Draw team color box, nested inside the frame box.
      g.setColor( SpriteLibrary.coColorList[color] );
      g.fillRect(drawX - drawScale, drawY - drawScale, drawW + (2*drawScale), drawH + (2*drawScale));

      // Draw CO Portrait
      g.drawImage(portrait, drawX, drawY, drawW, drawH, null);
    }
  }
}
