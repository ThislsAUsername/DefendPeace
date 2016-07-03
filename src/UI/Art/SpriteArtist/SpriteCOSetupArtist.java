package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import CommandingOfficers.CommanderLibrary;
import Terrain.MapInfo;
import UI.COSetupController;

public class SpriteCOSetupArtist
{
  private static Dimension dimensions = null;
  private static int drawScale = 2;

  private static final Color BGCOLOR = new Color(234, 243, 255); // Light blue background.
  private static final Color BGSHIMMER= new Color(248, 248, 255); // Lighter blue shimmer effect.
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);

  private static double animHighlightedPlayer = 0;

  // variables for background shimmer effect
  private static int shimmerStretch = 160*drawScale; // TODO: This should really just be screen height.
  private static int shimmerThickness = 25*drawScale;
  private static int shimmerSpacing = 100*drawScale; // Distance between adjacent bands.
  private static int[] xPoints = {0, shimmerStretch, shimmerStretch+shimmerThickness, shimmerThickness};
  private static int[] yPoints = {shimmerStretch, 0, 0, shimmerStretch};
  private static Polygon shimmerPoly = new Polygon(xPoints, yPoints, xPoints.length); // Shimmer shape to draw.
  private static double shimmerBasePoint = -(shimmerStretch+shimmerSpacing); // Where to draw the first shimmer.
  private static long lastDrawTime = System.currentTimeMillis(); // Used to control shimmer drift.
  private static double shimmerPxPerMs = 0.03; // Controls the speed of shimmer movement.

  public static void setDimensions(Dimension d)
  {
    dimensions = d;
  }

  public static void draw(Graphics g, MapInfo mapInfo, COSetupController control)
  {
    ////////////////// Background /////////////////////////
    g.setColor(BGCOLOR);
    g.fillRect(0,0,dimensions.width, dimensions.height);

    // Draw fancy background effects.
    drawBGShimmer(g);

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

    // Figure out where to draw each player's CO portrait.
    int drawYCenter = ((dimensions.height - vspace) / 2) + vspace;
    int coHighlightedPortraitXCenter = dimensions.width / 2; // Whichever player has focus should be centered.

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

  /**
   * This function works by tracking a single point, where the first shimmer begins, and then drawing
   * each band of color at a regular spacing from there. The basis point floats over time, and once
   * it moves far enough off the screen, it is brought back in range (to the next draw point).
   */
  private static void drawBGShimmer(Graphics g)
  {
    // Make a copy of the base polygon and translate it to the first draw point.
    Polygon drawPoly = new Polygon(shimmerPoly.xpoints, shimmerPoly.ypoints, shimmerPoly.npoints);
    int currentDrawPoint = (int)shimmerBasePoint; // Get the basis point to the nearest pixel.
    drawPoly.translate(currentDrawPoint, 0);

    // Draw all the shimmers.
    g.setColor(BGSHIMMER);
    for(; currentDrawPoint < dimensions.width; currentDrawPoint += shimmerSpacing)
    {
      // Draw the current shimmering band, then translate the polygon for the next.
      g.fillPolygon(drawPoly);
      drawPoly.translate(shimmerSpacing, 0);
    }

    // Time management.
    long thisTime = System.currentTimeMillis();
    long timeDiff = thisTime - lastDrawTime;
    lastDrawTime = thisTime;

    // Figure out how far to move the basis point.
    // lastDrawTime only updates when this is called, so discard if too large.
    double dist = timeDiff * shimmerPxPerMs;
    dist = (dist > dimensions.width)? 0 : dist;

    // Translate the basis point, bringing it back on-screen if needed.
    shimmerBasePoint -= dist;
    if(shimmerBasePoint < -(shimmerStretch+shimmerSpacing))
    {
      shimmerBasePoint += shimmerSpacing;
    }
  }
}
