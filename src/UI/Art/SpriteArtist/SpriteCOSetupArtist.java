package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.CommanderLibrary;
import Terrain.MapInfo;
import UI.COSetupController;

public class SpriteCOSetupArtist
{
  private static Dimension dimensions = null;
  private static int drawScale = 2;

  private static final Color BGCOLOR = new Color(234, 243, 255);
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);

  public static void setDimensions(Dimension d)
  {
    dimensions = d;
  }

  public static void draw(Graphics g, MapInfo mapInfo, COSetupController control)
  {
    ////////////////// Background /////////////////////////
    g.setColor(BGCOLOR);
    g.fillRect(0,0,dimensions.width, dimensions.height);

    // TODO: draw fancy background effects.

    /////////////////// MiniMap ////////////////////////
    BufferedImage miniMap = SpriteMiniMapArtist.getMapImage( mapInfo );
    // Figure out how large to draw the minimap. We aren't horizontally constrained, but we need
    // to fit the CO selection area below it. Make it as large as possible given those constraints.
    int vspace = 96*drawScale;
    int mmWScale = dimensions.width / miniMap.getWidth();
    int mmHScale = (vspace-4*drawScale) / miniMap.getHeight();
    int mmScale = (mmWScale > mmHScale)? mmHScale : mmWScale;

    // Draw a frame for the minimap.
    int mapWidth = miniMap.getWidth() * mmScale;
    int mapHeight = miniMap.getHeight() * mmScale;
    int mapLeft = (dimensions.width / 2) - (mapWidth / 2);
    int mapTop = (vspace / 2) - (mapHeight / 2);
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(mapLeft-(2*drawScale), mapTop-(2*drawScale), mapWidth+(4*drawScale), mapHeight+(4*drawScale));
    g.setColor(MENUBGCOLOR);
    g.fillRect(mapLeft-drawScale, mapTop-drawScale, mapWidth+(2*drawScale), mapHeight+(2*drawScale));

    // Draw the mini map.
    SpriteLibrary.drawImageCenteredOnPoint(g, miniMap, dimensions.width / 2, vspace / 2, mmScale);

    /////////////////// CO Portraits ///////////////////////
    int numCOs = mapInfo.getNumCos();
    int highlightedPlayer = control.getHighlightedPlayer();

    // Figure out where to draw the COs
    int drawYCenter = ((dimensions.height - vspace) / 2) + vspace;
    int coHighlightedPortraitXCenter = dimensions.width / 2; // Whichever player has focus will be centered.

    double numCosOnScreen = 5.5; // Display 5 cos, and the edge of the portrait for any hanging off the screen edge.
    int xSpacing = (int)(dimensions.width / numCosOnScreen);

    // Find where zero should be drawn.
    int drawXCenter = coHighlightedPortraitXCenter - highlightedPlayer * xSpacing; 

    for(int i = 0; i < numCOs; ++i, drawXCenter += xSpacing)
    {
      // Only draw CO portraits that will be on screen.
      if( Math.abs(highlightedPlayer - i) < Math.ceil(numCosOnScreen / 2))
      {
        int co = control.getPlayerCo(i);
        int col = control.getPlayerColor(i);
        // Draw the box, the color, and the CO portrait.
        drawCoPortrait(g, co, col, drawXCenter, drawYCenter);
      }
    }
  }

  private static void drawCoPortrait(Graphics g, int co, int color, int xCenter, int yCenter)
  {
    // Fetch CO portrait
    BufferedImage portrait = SpriteLibrary.getCommanderSprites( CommanderLibrary.getCommanderList().get(co).cmdrEnum ).head;

    int drawX = xCenter - ( (portrait.getWidth()*drawScale) / 2 );
    int drawY = yCenter - ( (portrait.getHeight()*drawScale) / 2);
    int drawW = portrait.getWidth() * drawScale;
    int drawH = portrait.getHeight() * drawScale;
    
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
