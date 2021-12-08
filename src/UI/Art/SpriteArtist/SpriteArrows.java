package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import UI.SlidingValue;

public class SpriteArrows
{
  public static final int ARROW_SIZE = 7;
  public boolean isVertical = false;
  private SlidingValue xPos;
  private SlidingValue yPos;
  private SlidingValue arrowSpacing;
  private Color color;

  public SpriteArrows()
  {
    this(0, 0, 0, SpriteUIUtils.MENUFRAMECOLOR);
  }

  public SpriteArrows(int x, int y, int l, Color c)
  {
    xPos = new SlidingValue(x);
    yPos = new SlidingValue(y);
    arrowSpacing = new SlidingValue(l);
    color = c;
  }

  public void set(Color c)
  {
    color = c;
  }

  public void set(int x, int y)
  {
    xPos.set(x);
    yPos.set(y);
  }

  public void snap(int x, int y)
  {
    xPos.snap(x);
    yPos.snap(y);
  }

  public void set(int x, int y, boolean snap)
  {
    if(snap) snap(x, y);
    else set(x, y);
  }

  public void set(int x, int y, int l)
  {
    xPos.set(x);
    yPos.set(y);
    arrowSpacing.set(l);
  }

  public void snap(int x, int y, int l)
  {
    xPos.snap(x);
    yPos.snap(y);
    arrowSpacing.snap(l);
  }

  public void set(int x, int y, int l, boolean snap)
  {
    if(snap) snap(x, y, l);
    else set(x, y, l);
  }

  public void draw(Graphics g)
  {
    draw(g, xPos.geti(), yPos.geti(), arrowSpacing.geti(), isVertical, color);
  }

  // Draw the arrows centered on a square image, then draw the image into place
  public static void draw(Graphics g, int x, int y, int itemWidth, boolean isVertical, Color color)
  {
    int imageDimension = itemWidth + ARROW_SIZE;
    // Make sure the image size is odd, so rotating is consistent
    if( (imageDimension & 1) == 0 )
      imageDimension += 1;
    // Make points to define a selection arrow.
    int[] xPoints = { 1, 5, 5, 1 };
    int[] yPoints = { imageDimension / 2 + 0, imageDimension / 2 + -4, imageDimension / 2 + 4, imageDimension / 2 + 0 };

    BufferedImage arrows = new BufferedImage(imageDimension, imageDimension, BufferedImage.TYPE_INT_ARGB);
    Graphics2D ag = arrows.createGraphics();
    if( isVertical ) // Spin 90 degrees around the image center
      ag.rotate(Math.PI / 2, imageDimension / 2, imageDimension / 2);
    // Paint the center and outline of the triangle, for a consistent shape
    ag.setColor(color);
    ag.fillPolygon(xPoints, yPoints, xPoints.length);
    ag.drawPolygon(xPoints, yPoints, xPoints.length);
    // Flip the image 180 degrees and paint the same points again
    ag.rotate(Math.PI, imageDimension / 2, imageDimension / 2);
    ag.fillPolygon(xPoints, yPoints, xPoints.length);
    ag.drawPolygon(xPoints, yPoints, xPoints.length);
    SpriteUIUtils.drawImageCenteredOnPoint(g, arrows, x, y);
  }

  public int getX()
  {
    return xPos.geti();
  }
  public int getY()
  {
    return yPos.geti();
  }
  public int getW()
  {
    return arrowSpacing.geti();
  }
}
