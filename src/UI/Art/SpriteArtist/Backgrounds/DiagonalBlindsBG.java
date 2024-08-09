package UI.Art.SpriteArtist.Backgrounds;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

import UI.Art.SpriteArtist.SpriteOptions;

public class DiagonalBlindsBG
{
  private static final Color COLOR_BG = new Color(234, 243, 255); // Light blue background.
  private static final Color COLOR_BLINDS= new Color(248, 248, 255); // Lighter blue blinds color effect.
  private static int shimmerScale = SpriteOptions.getDrawScale();
  private static int shimmerThickness = 25*shimmerScale;
  private static int shimmerSpacing = 100*shimmerScale; // Distance between adjacent bands.
  private static double shimmerBasePoint = -(SpriteOptions.getScreenDimensions().height+shimmerSpacing); // Where to draw the first band.
  private static long lastDrawTime = System.currentTimeMillis(); // Used to control drift.
  private static double shimmerPxPerMs = 0.03; // Controls the movement speed.

  /**
   * This function works by tracking a single point, where the first band begins, and then drawing
   * each band of color at a regular spacing from there. The basis point floats over time, and once
   * it moves far enough off the screen, it is brought back in range (to the next draw point).
   */
  public static void draw(Graphics g)
  {
    // Figure out the screen dimensions so we can draw the background correctly.
    Dimension dimensions = SpriteOptions.getScreenDimensions();

    // Lay down the base coat of paint.
    g.setColor(COLOR_BG);
    g.fillRect(0, 0, dimensions.width, dimensions.height);

    // If the scale has changed since we last checked, reinitialize.
    if(shimmerScale != SpriteOptions.getDrawScale())
    {
      shimmerScale = SpriteOptions.getDrawScale();
      shimmerThickness = 25*shimmerScale;
      shimmerSpacing = 100*shimmerScale; // Distance between adjacent bands.
      shimmerBasePoint = -(dimensions.height+shimmerSpacing); // Where to draw the first shimmer.
      lastDrawTime = System.currentTimeMillis(); // Used to control shimmer drift.
      shimmerPxPerMs = 0.03; // Controls the speed of shimmer movement.
    }

    // Build the polygon we will use to draw the lighter bands in the background.
    int[] xPoints = {0, dimensions.height, dimensions.height+shimmerThickness, shimmerThickness};
    int[] yPoints = {dimensions.height, 0, 0, dimensions.height};
    Polygon drawPoly = new Polygon(xPoints, yPoints, xPoints.length); // Shimmer shape to draw.

    int currentDrawPoint = (int)shimmerBasePoint; // Get the basis point to the nearest pixel.
    drawPoly.translate(currentDrawPoint, 0);

    // Draw all the shimmers.
    g.setColor(COLOR_BLINDS);
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
    if(shimmerBasePoint < -(dimensions.height+shimmerSpacing))
    {
      shimmerBasePoint += shimmerSpacing;
    }
  }
}
