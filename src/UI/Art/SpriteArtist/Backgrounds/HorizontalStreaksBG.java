package UI.Art.SpriteArtist.Backgrounds;

import java.util.Random;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteOptions;

public class HorizontalStreaksBG
{
  private static Color COLOR_BG = null;
  private static int drawScale = SpriteOptions.getDrawScale();
  private static long lastDrawTime = System.currentTimeMillis(); // Used to control drift.
  private static double nextX = 0;
  private static double movePxPerMs = 0.06; // Controls movement speed.
  private static int streakLengthMean = 48;
  private static int streakLengthStd = 10;
  private static int streakHeightMean = 12;
  private static int streakHeightStd = 3;
  private static double saturation = 0.3;
  private static Dimension lastDim;
  private static BufferedImage streaksImage;
  private static int regenTolerance = 4;

  private static Random random = new Random();

  public static void draw(Graphics g)
  {
    draw(g, COLOR_BG);
  }

  public static void draw(Graphics g, Color newColor)
  {
    Dimension dimensions = SpriteOptions.getScreenDimensions();

    // If the scale has changed since we last checked, reinitialize.
    if( null == lastDim ||
        COLOR_BG != newColor ||
        Math.abs(dimensions.height - lastDim.height) > regenTolerance ||
        Math.abs(dimensions.width - lastDim.width) > regenTolerance ||
        drawScale != SpriteOptions.getDrawScale() ||
        null == streaksImage)
    {
      COLOR_BG = newColor;
      lastDim = new Dimension(dimensions);
      drawScale = SpriteOptions.getDrawScale();
      lastDrawTime = System.currentTimeMillis();
      
      // Generate an image with all of the streaks.
      streaksImage = SpriteLibrary.createTransparentSprite(dimensions.width, dimensions.height);

      // Lay down the base coat of paint.
      Graphics sg = streaksImage.getGraphics();
      sg.setColor(COLOR_BG);
      sg.fillRect(0, 0, dimensions.width, dimensions.height);
      int numToGenerate = (int)(((dimensions.width*dimensions.height) / (streakHeightMean*streakLengthMean*drawScale*drawScale)) * saturation);
      for( int nn = 0; nn < numToGenerate; nn++ )
      {
        int x = random.nextInt(dimensions.width);
        int y = random.nextInt(dimensions.height);

        int h = (int)(random.nextGaussian() * streakHeightStd + streakHeightMean) * drawScale;
        int l = (int)(random.nextGaussian() * streakLengthStd + streakLengthMean) * drawScale;
        Color c = new Color(255, 255, 255, random.nextInt(120) + 80);

        sg.setColor(c);
        sg.fillRoundRect(x, y, l, h, l/4, h/4);

        // If it's too close to the edge, redraw it on the other edge.
        if( x + l > dimensions.width )
        {
          sg.fillRoundRect(x-dimensions.width, y, l, h, l/4, h/4);
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
