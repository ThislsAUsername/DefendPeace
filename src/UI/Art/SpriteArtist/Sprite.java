package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

/**
 * Holds a collection of related images, e.g. an animation sequence.
 */
public class Sprite
{
  private ArrayList<BufferedImage> spriteImages;

  public Sprite(BufferedImage baseSprite)
  {
    if( null == baseSprite )
    {
      System.out.println("WARNING! Sprite() given null image. Continuing with placeholder.");
      // If we are given an invalid image, just use a default that should stick out like a sore thumb.
      baseSprite = SpriteLibrary.createDefaultBlankSprite(SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize);
    }
    spriteImages = new ArrayList<BufferedImage>();
    spriteImages.add(baseSprite);
  }

  /**
   * Parses the provided sprite sheet into individual, like-sized frames, with the assumption
   * that the sprite images are arranged horizontally.
   * If the sprite sheet is taller than the provided height, the extra image space is ignored.
   * If the sprite sheet width does not divide evenly, then the final (too-short) section
   * of the image will NOT be included as a separate frame.
   * @param spriteSheet The image to parse into frames.
   * @param spriteWidthPx The width of each frame in pixels.
   * @param spriteHeightPx The height of each frame in pixels.
   */
  public Sprite(BufferedImage spriteSheet, int spriteWidthPx, int spriteHeightPx)
  {
    spriteImages = new ArrayList<BufferedImage>();

    if( null == spriteSheet || spriteHeightPx > spriteSheet.getHeight() || spriteWidthPx > spriteSheet.getWidth() )
    {
      System.out.println("WARNING! Sprite() given invalid sprite sheet. Creating placeholder image.");
      // Just make a single blank frame of the specified size.
      spriteImages.add(SpriteLibrary.createDefaultBlankSprite(spriteWidthPx, spriteHeightPx));
    }
    else
    {
      // Start at the beginning of the sprite sheet.
      int xOffset = 0;
      int yOffset = 0;
      int spriteNum = 0;

      // Cut the sprite-sheet into individual frames.
      while ((spriteNum + 1) * spriteWidthPx <= spriteSheet.getWidth())
      {
        spriteImages.add(spriteSheet.getSubimage(xOffset, yOffset, spriteWidthPx, spriteHeightPx));
        xOffset += spriteWidthPx;
        spriteNum++;
      }
    }
  }

  /**
   * Sprite copy-constructor. Perform a deep-copy on each of the other sprite's frames.
   */
  public Sprite(Sprite other)
  {
    this(other, false);
  }
  public Sprite(Sprite other, boolean hFlip)
  {
    spriteImages = new ArrayList<BufferedImage>();

    if( null == other )
    {
      System.out.println("WARNING! Sprite() given null Sprite. Creating placeholder image.");
      // Just make a single blank frame of the specified size.
      spriteImages.add(SpriteLibrary.createDefaultBlankSprite(SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize));
    }
    else
    {
      int width = other.getFrame(0).getWidth();
      int height = other.getFrame(0).getHeight();
      for( int i = 0; i < other.numFrames(); ++i )
      {
        BufferedImage aFrame = other.getFrame(i);
        BufferedImage myFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if( hFlip )
          myFrame.getGraphics().drawImage(aFrame, width, 0, -width, height, null);
        else
          myFrame.getGraphics().drawImage(aFrame, 0, 0, null);
        spriteImages.add(myFrame);
      }
    }
  }

  public int numFrames()
  {
    return spriteImages.size();
  }

  /**
   * Adds another frame to this sprite.
   * @param sprite
   */
  public void addFrame(BufferedImage sprite)
  {
    spriteImages.add(sprite);
  }

  public BufferedImage getFrame(int index)
  {
    return spriteImages.get(Math.abs(index) % spriteImages.size());
  }

  /**
   * For every image contained in this sprite, change each pixel with a value in oldColors to the corresponding value in newColors.
   */
  public void colorize(Color[] oldColors, Color[] newColors)
  {
    for( BufferedImage bi : spriteImages )
    {
      for( int x = 0; x < bi.getWidth(); ++x )
      {
        for( int y = 0; y < bi.getHeight(); ++y )
        {
          int colorValue = bi.getRGB(x, y);
          for( int c = 0; c < oldColors.length; ++c )
          {
            if( oldColors[c].getRGB() == colorValue )
            {
              bi.setRGB(x, y, newColors[c].getRGB());
            }
          }
        }
      }
    }
  }

  /**
   * For every image contained in this sprite, change each oldColor pixel to newColor.
   */
  public void colorize(Color oldColor, Color newColor)
  {
    for( BufferedImage bi : spriteImages )
    {
      for( int x = 0; x < bi.getWidth(); ++x )
      {
        for( int y = 0; y < bi.getHeight(); ++y )
        {
          int colorValue = bi.getRGB(x, y);
          if( oldColor.getRGB() == colorValue )
          {
            bi.setRGB(x, y, newColor.getRGB());
          }
        }
      }
    }
  }

  /**
   * Convert any non-transparent pixels in this Sprite to the given maskColor.
   */
  public void convertToMask(Color maskColor)
  {
    for( BufferedImage bi : spriteImages )
    {
      WritableRaster raster = bi.getRaster();
      double mask[] = {maskColor.getRed(), maskColor.getGreen(), maskColor.getBlue(), maskColor.getAlpha() };
      for( int x = 0; x < bi.getWidth(); ++x )
      {
        for( int y = 0; y < bi.getHeight(); ++y )
        {
          double[] pixel = new double[4];
          raster.getPixel(x, y, pixel);
          if( pixel[3] != 0 ) raster.setPixel(x, y, mask);
        }
      }
    }
  }
}
