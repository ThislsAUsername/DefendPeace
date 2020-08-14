package UI.Art.SpriteArtist.Backgrounds;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteOptions;

public class HorizontalStreaksBG
{
  private static Color COLOR_BG = null;
  private static ArrayList<Streak> streaks;
  private static int drawScale = SpriteOptions.getDrawScale();
  private static long lastDrawTime = System.currentTimeMillis(); // Used to control drift.
  private static double movePxPerMs = 0.03; // Controls movement speed.
  private static int streakLengthMean = 48;
  private static int streakLengthStd = 10;
  private static int streakWidthMean = 16;
  private static int streakWidthStd = 5;
  private static int streakAlphaMean = 190;
  private static int streakAlphaStd = 20;
  private static double saturation = 0.2;
  private static Dimension lastDim;

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
    if((!dimensions.equals(lastDim)) || (drawScale != SpriteOptions.getDrawScale()) || (null == streaks))
    {
      lastDim = new Dimension(dimensions);
      drawScale = SpriteOptions.getDrawScale();
      lastDrawTime = System.currentTimeMillis();
      
      // Generate a bunch of streaks to draw.
      streaks = new ArrayList<Streak>();
      int numToGenerate = (int)(((dimensions.width*dimensions.height) / (streakWidthMean*streakLengthMean)) * saturation);
      for( int nn = 0; nn < numToGenerate; nn++ )
      {
        double xpos = Math.random() * dimensions.width;
        double ypos = Math.random() * dimensions.height;
        int w = 64;
        int l = 48;
        int a = (int)(Math.random() * 255);
        
        streaks.add(new Streak(xpos, ypos, w, l, a));
      }
    }

    // Draw everything.
    for( Streak ss : streaks )
    {
      g.setColor(ss.c);
      g.fillRoundRect((int)ss.x, (int)ss.y, ss.w, ss.h, ss.w/4, ss.h/4);
    }

    // Time management.
    long thisTime = System.currentTimeMillis();
    long timeDiff = thisTime - lastDrawTime;
    lastDrawTime = thisTime;

    // Figure out how far to move everything.
    // lastDrawTime only updates when this is called, so discard if too large.
    double dist = timeDiff * movePxPerMs;
    dist = (dist > dimensions.width)? 0 : dist;

    // Translate the basis point, bringing it back on-screen if needed.
    for( Streak ss : streaks )
    {
      ss.x -= dist;
      if( ss.x < -ss.w )
      {
        ss.x = dimensions.width;
      }
    }
  }

  private static class Streak
  {
    double x;
    double y;
    int w;
    int h;
    Color c;

    public Streak(double x, double y, int w, int h, int a)
    {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.c = new Color(255, 255, 255, a);
    }
  }
}
