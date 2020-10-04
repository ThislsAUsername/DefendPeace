package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class PixelFont
{
  final public Font font;
  final private FontMetrics fontMetrics;
  final public int emSizePx;

  public PixelFont(Font f)
  {
    this(f, 0, 0);
  }

  public PixelFont(Font f, int widthPx, int heightPx)
  {
    boolean adaptSize = widthPx != 0 && heightPx != 0;

    // Get a Graphics object so we can size the font.
    BufferedImage bitemp = SpriteLibrary.createDefaultBlankSprite(1, 1);
    Graphics gg = bitemp.getGraphics();
    int fgoalwidth = widthPx+1;
    int fgoalheight = heightPx+1;
    int fsize = 0;
    int sanityCheck = 0;
    
    // Figure out the correct size to draw this font so it draws at the correct size.
    Font currentFont = adaptSize ? null : f;
    FontMetrics currentFm = adaptSize ? null : gg.getFontMetrics(f);
    while(adaptSize)
    {
      sanityCheck++;
      f = f.deriveFont(Font.PLAIN, fsize);
      gg.setFont(f);
      FontMetrics fm = gg.getFontMetrics();
      int w = fm.charWidth('M');
      int h = fm.getHeight();
      
      if( sanityCheck >= 100 )
      {
        font = null;
        fontMetrics = null;
        throw new RuntimeException("Failed to find an acceptable font size!");
      }
      else if( w > fgoalwidth || h > fgoalheight )
      {
        // This is too big; use the last one we found.
        break;
      }
      else if( w == fgoalwidth && h == fgoalheight )
      {
        // There may be multiple font sizes that resolve to the same pixel size.
        // We want the largest "correct" size to increase odds of correct rendering.
        currentFont = f;
        currentFm = fm;
      }
      fsize++;
    }
    if( null == currentFont || null == currentFm )
      throw new RuntimeException("Error initializing PixelFont!");

    font = currentFont;
    fontMetrics = currentFm;
    this.emSizePx = fontMetrics.charWidth('M');
    System.out.println("Setting font size to " + font.getSize());
    System.out.println(" height: " + fontMetrics.getHeight());
    System.out.println(" ascent: " + fontMetrics.getAscent());
    System.out.println(" emsize: " + this.emSizePx);
    System.out.println(" dscent: " + fontMetrics.getDescent());
  }

  public void write(Graphics g, String text, int x, int y)
  {
    g.setFont(font);
    g.setColor(Color.BLACK);
    g.drawString(text, x, y+fontMetrics.getAscent());
  }

  public int getWidth(char c)
  {
    return fontMetrics.charWidth(c);
  }

  public int getWidth(String text)
  {
    return fontMetrics.stringWidth(text);
  }

  public int getHeight()
  {
    return fontMetrics.getHeight();
  }

  public int getAscent()
  {
    return fontMetrics.getAscent();
  }
}
