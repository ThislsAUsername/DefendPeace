package UI.Art.SpriteArtist.Backgrounds;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteOptions;

public class HorizontalStreaksBG
{
  private static Color COLOR_BG = null;
  private static int drawScale = SpriteOptions.getDrawScale();
  private static long lastDrawTime = System.currentTimeMillis(); // Used to control drift.
  private static double nextX = 0;
  private static double movePxPerMs = 0.03; // Controls movement speed.
  private static int streakLengthMean = 48;
  private static int streakLengthStd = 10;
  private static int streakWidthMean = 16;
  private static int streakWidthStd = 5;
  private static int streakAlphaMean = 190;
  private static int streakAlphaStd = 20;
  private static double saturation = 0.2;
  private static Dimension lastDim;
  private static BufferedImage streaksImage;

  public static void draw(Graphics g)
  {
    draw(g, COLOR_BG);
  }

  public static void draw(Graphics g, Color newColor)
  {
    Dimension dimensions = SpriteOptions.getScreenDimensions();

    // Lay down the base coat of paint.
    COLOR_BG = newColor;
    g.setColor(COLOR_BG);
    g.fillRect(0, 0, dimensions.width, dimensions.height);

    // If the scale has changed since we last checked, reinitialize.
    if((!dimensions.equals(lastDim)) || (drawScale != SpriteOptions.getDrawScale()) || (null == streaksImage))
    {
      lastDim = new Dimension(dimensions);
      drawScale = SpriteOptions.getDrawScale();
      lastDrawTime = System.currentTimeMillis();
      
      // Generate an image with all of the streaks.
      streaksImage = SpriteLibrary.createTransparentSprite(dimensions.width, dimensions.height);
      Graphics sg = streaksImage.getGraphics();
      sg.setColor(Color.WHITE);
      sg.drawRect(0, 0, streaksImage.getWidth(), streaksImage.getHeight());
      int numToGenerate = (int)(((dimensions.width*dimensions.height) / (streakWidthMean*streakLengthMean)) * saturation);
      for( int nn = 0; nn < numToGenerate; nn++ )
      {
        double x = Math.random() * dimensions.width;
        double y = Math.random() * dimensions.height;
        int h = 32;
        int l = 48;
        Color c = new Color(255, 255, 255, (int)(Math.random() * 255));

        sg.setColor(c);
        sg.fillRoundRect((int)x, (int)y, l, h, l/4, h/4);

        // If it's too close to the edge, redraw it on the other edge.
        if( x + l > dimensions.width )
        {
          sg.fillRoundRect((int)(x-dimensions.width), (int)y, l, h, l/4, h/4);
        }
      }
    }

    // Draw the streaks overlay.
    {
      // Determine how much of the image is off the side, and
      // draw it in two chunks to fill the screen.
      int x = (int)nextX;
      int offscreenW = -x;
      int imgW = streaksImage.getWidth();
      int imgH = streaksImage.getHeight();

      // Draw the right side of the image (visible in the left side of the screen),
      // then the left side of the image (wrapped to the right side of the screen).
      // First four coords are the dest x,y,x2,y2. Next four are the source coords.
      g.drawImage(streaksImage, 0, 0, imgW-offscreenW, imgH,
                                offscreenW, 0, imgW, imgH, null);
      g.drawImage(streaksImage, imgW-offscreenW, 0, imgW, imgH,
                                0, 0, offscreenW, imgH, null);
    }

    // Time management.
    long thisTime = System.currentTimeMillis();
    long timeDiff = thisTime - lastDrawTime;
    lastDrawTime = thisTime;

    // Figure out how far to move everything.
    double dist = timeDiff * movePxPerMs;
    nextX -= dist;
    if( nextX < streaksImage.getWidth() * -1 )
      nextX = 0;
  }
}
