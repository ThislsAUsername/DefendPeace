package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;



public class SpriteUIUtils
{
  public static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  public static final Color MENUBGCOLOR = new Color(234, 204, 154);
  public static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);


  public static BufferedImage makeTextFrame(String item, int hBuffer, int vBuffer)
  {
    return makeTextFrame(MENUBGCOLOR, MENUFRAMECOLOR, item, hBuffer, vBuffer);
  }
  public static BufferedImage makeTextFrame(Color bg, Color frame, int hBuffer, int vBuffer)
  {
    return makeTextMenu(bg, frame, bg, new ArrayList<String>(), 0, hBuffer, vBuffer);
  }
  public static BufferedImage makeTextFrame(Color bg, Color frame, String item, int hBuffer,
      int vBuffer)
  {
    ArrayList<String> items = new ArrayList<String>();
    items.add(item);
    return makeTextMenu(bg, frame, bg, items, 0, hBuffer, vBuffer);
  }
  public static BufferedImage makeTextMenu(ArrayList<String> items, int selection, int hBuffer, int vBuffer)
  {
    return makeTextMenu(MENUBGCOLOR, MENUFRAMECOLOR, MENUHIGHLIGHTCOLOR, items, selection, hBuffer, vBuffer);
  }
  public static BufferedImage makeTextMenu(Color bg, Color frame, Color focus, ArrayList<String> items, int selection,
      int hBuffer, int vBuffer)
  {
    // Find the dimensions of the menu we are drawing.
    int menuTextWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
    int menuTextHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();
    int menuWidth = (( items.isEmpty() ) ? 0 : getMenuTextWidthPx(items, menuTextWidth)) + hBuffer * 2;
    int menuHeight = (( items.isEmpty() ) ? 0 : getMenuTextHeightPx(items, menuTextHeight)) + vBuffer * 2;

    // Build our image.
    BufferedImage menuImage = null;
    if( menuWidth == 0 || menuHeight == 0 )
      return SpriteLibrary.createDefaultBlankSprite(1, 1); // zero-dimensioned images aren't kosher
    else
      menuImage = SpriteLibrary.createDefaultBlankSprite(menuWidth, menuHeight);
    Graphics g = menuImage.getGraphics();

    // Draw the nice box for our text.
    drawMenuFrame(g, bg, frame, 0, 0, menuWidth, menuHeight, vBuffer);

    // Draw the highlight for the currently-selected option.
    // selY = upper menu-frame buffer, plus (letter height, plus 1px-buffer, times number of options).
    int selY = vBuffer + (menuTextHeight + 1) * selection;
    g.setColor(focus);
    g.fillRect(0, selY, menuWidth, menuTextHeight);

    // Draw the actual menu text.
    for( int txtY = vBuffer, i = 0; i < items.size(); ++i, txtY += menuTextHeight + 1 )
    {
      SpriteUIUtils.drawTextSmallCaps(g, items.get(i), hBuffer, txtY);
    }

    return menuImage;
  }

  /**
   * Returns an image with the input prose printed, in normal text.
   * Acts as an overload to {@link #drawProseToWidth(String, int)}
   */
  public static BufferedImage drawProse(String prose)
  {
    // Figure out how big our text is.
    PixelFont font = SpriteLibrary.getFontStandard();

    ArrayList<String> lines = new ArrayList<String>();
    // Unload our prose into the lines it already has
    lines.addAll(Arrays.asList(prose.split("\\R")));

    int maxWidth = 1;
    for( String line : lines )
      maxWidth = Math.max(maxWidth, font.getWidth(line));

    return drawProseToWidth(prose, maxWidth);
  }

  /**
   * Returns an image with the input string printed within the specified width, in normal text.
   * @param reqWidth: Actual UI size in pixels that you want to fit the text into.
   */
  public static BufferedImage drawProseToWidth(String prose, int reqWidth)
  {
    // Figure out how big our text is.
    PixelFont font = SpriteLibrary.getFontStandard();
    int characterHeight = font.getHeight();

    ArrayList<String> lines = new ArrayList<String>();
    // Unload our prose into the lines it already has
    lines.addAll(Arrays.asList(prose.split("\\R")));

    if( reqWidth < font.emSizePx || lines.isEmpty() )
      return SpriteLibrary.createDefaultBlankSprite(1, 1); // zero-dimensioned images aren't kosher

    for( int i = 0; i < lines.size(); ++i ) // basic for, since we care about indices
    {
      String line = lines.get(i);
      if( font.getWidth(line) <= reqWidth ) // if the line's short enough already, don't split it further
        continue;

      lines.remove(i);

      String subline = line;
      int splitIndex = 0;
      boolean fits = false;
      do
      {
        // Start by cutting the line at each space to try and make it fit.
        splitIndex = subline.lastIndexOf(' ');
        if( splitIndex < 1 ) // no spaces we can split on...
          splitIndex = subline.length() - 1; // Just shave off a letter instead

        subline = subline.substring(0, splitIndex);
        fits = font.getWidth(subline) <= reqWidth;
      } while(!fits);

      lines.add(i, line.substring(splitIndex)); // put in the second half
      lines.add(i, line.substring(0, splitIndex)); // and then the first half behind it
    }

    int totalTextHeight = ((lines.isEmpty()) ? 0 : getMenuTextHeightPx(lines, characterHeight));
    // Build our image.
    BufferedImage menuImage = null;
    menuImage = SpriteLibrary.createTransparentSprite(reqWidth, totalTextHeight);
    Graphics g = menuImage.getGraphics();

    // Draw the actual text.
    for( int txtY = 0, i = 0; i < lines.size(); ++i, txtY += characterHeight + 1 )
    {
      font.write(g, lines.get(i), 0, txtY);
    }

    return menuImage;
  }

  public static void drawMenuFrame(Graphics g, Color bg, Color frame, int x, int y, int w, int h, int vBuffer)
  {
    int menuFrameHeight = vBuffer / 2; // Upper and lower bit can look framed.

    g.setColor(bg);
    g.fillRect(x, y, w, h); // Main menu body;
    g.setColor(frame);
    g.fillRect(x, y, w, menuFrameHeight); // Upper frame;
    g.fillRect(x, y + h - menuFrameHeight, w, menuFrameHeight); // Lower frame;
  }

  /**
   * Draws itself as a two-tone box with an image on top.
   */
  public static class ImageFrame
  {
    public final int xPos;
    public final int yPos;
    public final int width;
    public final int height;
    private Color mainColor;
    private Color rimColor;
    private boolean rimIsUp;
    private BufferedImage content;
    public ImageFrame(int x, int y, int w, int h, Color main, Color rim, boolean rimUp, BufferedImage display)
    {
      xPos = x;
      yPos = y;
      width = w;
      height = h;
      mainColor = main;
      rimColor = rim;
      rimIsUp = rimUp;
      content = SpriteLibrary.createDefaultBlankSprite(width, height);
      Graphics graphics = content.getGraphics();
      graphics.setColor(rimColor);
      graphics.fillRect(0, 0, width, height);
      int dx = 0, dy = 0;
      if( rimIsUp ) dy++; else dx++;
      graphics.setColor(mainColor);
      graphics.fillRect(dx, dy, width-1, height-1);
      SpriteUIUtils.drawImageCenteredOnPoint(graphics, display, width/2, height/2);
    }

    public void render(Graphics g)
    {
      g.drawImage(content, xPos, yPos, width, height, null);
    }
  }

  public static int getMenuTextWidthPx(ArrayList<String> menuOptions, int charWidthPx)
  {
    int maxWidth = 0;
    for( int i = 0; i < menuOptions.size(); ++i )
    {
      int optw = menuOptions.get(i).length() * charWidthPx;
      maxWidth = (optw > maxWidth) ? optw : maxWidth;
    }

    return maxWidth;
  }

  public static int getMenuTextHeightPx(ArrayList<String> menuOptions, int charHeightPx)
  {
    // Height of the letters plus 1 (for buffer between menu options), times the number of entries,
    // minus 1 because there is no buffer after the last entry.
    return (charHeightPx + 1) * menuOptions.size() - 1;
  }

  public static int getTileSize()
  {
    return SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale();
  }

  /**
   * Draws the provided image, centered around x, y.
   */
  public static void drawImageCenteredOnPoint(Graphics g, BufferedImage image, int x, int y)
  {
    SpriteUIUtils.drawImageCenteredOnPoint(g, image, x, y, 1);
  }

  /**
   * Draws the provided image, centered around x, y, scaled by the provided factor.
   */
  public static void drawImageCenteredOnPoint(Graphics g, BufferedImage image, int x, int y, int drawScale)
  {
    // Calculate the size to draw.
    int drawWidth = image.getWidth() * drawScale;
    int drawHeight = image.getHeight() * drawScale;
  
    // Center over the target location.
    int drawX = x - drawWidth / 2;
    int drawY = y - drawHeight / 2;
  
    g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
  }

  /**
   * Draws the provided text at the provided location, using the standard alphanumeric sprite set.
   * @param g Graphics object to draw the text.
   * @param text Text to be drawn as sprited letters.
   * @param x X-coordinate of the top-left corner of the first letter to be drawn.
   * @param y Y-coordinate of the top-left corner of the first letter to be drawn.
   */
  public static void drawText(Graphics g, String text, int x, int y)
  {
    SpriteLibrary.getFontStandard().write(g, text, x, y);
  }

  /**
   * Draws the provided text at the provided location, using the small-caps alphanumeric sprite set.
   * @param g Graphics object to draw the text.
   * @param text Text to be drawn as sprited letters.
   * @param x X-coordinate of the top-left corner of the first letter to be drawn.
   * @param y Y-coordinate of the top-left corner of the first letter to be drawn.
   */
  public static void drawTextSmallCaps(Graphics g, String text, int x, int y)
  {
    drawText(g, text, x, y, SpriteLibrary.getLettersSmallCaps(), SpriteLibrary.getLettersSmallCaps(),
        SpriteLibrary.getNumbersSmallCaps(), SpriteLibrary.getSymbolsSmallCaps());
  }

  /**
   * Draws the provided text at the provided location, with the Bold
   * alphanumeric sprite set used for unit HP.
   * @param g Graphics object to draw the text.
   * @param text Text to be drawn.
   * @param x X-coordinate of the top-left corner of the first letter to be drawn.
   * @param y Y-coordinate of the top-left corner of the first letter to be drawn.
   */
  public static void drawBoldText(Graphics g, String text, int x, int y)
  {
    drawText(g, text, x, y, SpriteLibrary.getMapUnitLetterSprites(), SpriteLibrary.getMapUnitLetterSprites(),
        SpriteLibrary.getMapUnitNumberSprites(), SpriteLibrary.getMapUnitSymbolSprites());
  }

  /**
   * Draws the provided text at the provided location, using the provided sprite set.
   * @param g Graphics object to draw the text.
   * @param text Text to be drawn as sprited letters.
   * @param x X-coordinate of the top-left corner of the first letter to be drawn.
   * @param y Y-coordinate of the top-left corner of the first letter to be drawn.
   * @param uppercase Sprite containing the uppercase letter sprites.
   * @param lowercase Lowercase letter sprites. Can just be a duplicate of `upperase` if they match.
   * @param numbers Numeric character sprites.
   * @param symbols Symbolic character sprites.
   */
  public static void drawText(Graphics g, String text, int x, int y,
      Sprite uppercase, Sprite lowercase, Sprite numbers, Sprite symbols)
  {
    int menuTextWidth = uppercase.getFrame(0).getWidth();
    int menuTextHeight = uppercase.getFrame(0).getHeight();
  
    for( int i = 0; i < text.length(); ++i, x += menuTextWidth )
    {
      char thisChar = text.charAt(i);
      if( Character.isAlphabetic(thisChar) )
      {
        if( Character.isUpperCase(thisChar) )
        {
          int letterIndex = thisChar - 'A';
          g.drawImage(uppercase.getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
        }
        else
        {
          int letterIndex = thisChar - 'a';
          g.drawImage(lowercase.getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
        }
      }
      else if( Character.isDigit(thisChar) )
      {
        int letterIndex = thisChar - '0';
        g.drawImage(numbers.getFrame(letterIndex), x, y, menuTextWidth, menuTextHeight, null);
      }
      else // Assume symbolic
      {
        int symbolIndex = SpriteLibrary.charKey.indexOf(text.charAt(i));
        if( symbolIndex >= 0 )
        {
          g.drawImage(symbols.getFrame(symbolIndex), x, y, menuTextWidth, menuTextHeight, null);
        }
      }
    }
  }

  /**
   * Returns a BufferedImage containing the contents of `text` rendered on one
   * line in the standard font, on a transparent background, with no scaling applied.
   */
  public static BufferedImage getTextAsImage(String text)
  {
    return SpriteUIUtils.getTextAsImage(text, false);
  }

  /**
   * Returns a BufferedImage containing the contents of `text` rendered on one line (in small
   * caps or standard font as specified), on a transparent background, with no scaling applied.
   */
  public static BufferedImage getTextAsImage(String text, boolean smallCaps)
  {
    BufferedImage textImage;
    if(smallCaps)
    {
      Sprite letters = SpriteLibrary.getLettersSmallCaps();
      int width = letters.getFrame(0).getWidth() * text.length();
      int height = letters.getFrame(0).getHeight();
      textImage = SpriteLibrary.createTransparentSprite(width, height);
      drawTextSmallCaps(textImage.getGraphics(), text, 0, 0);
    }
    else
    {
      PixelFont pf = SpriteLibrary.getFontStandard();
      int width = pf.getWidth(text);
      int height = pf.getHeight();
      textImage = SpriteLibrary.createTransparentSprite(width, height);
      pf.write(textImage.getGraphics(), text, 0, 0);
    }
    return textImage;
  }

  /**
   * Returns a BufferedImage containing the contents of `text` rendered on one line,
   *  on a transparent background, with no scaling applied.
   */
  public static BufferedImage getBoldTextAsImage(String text)
  {
    Sprite letters = SpriteLibrary.getMapUnitLetterSprites();
    int width = letters.getFrame(0).getWidth() * text.length();
    int height = letters.getFrame(0).getHeight();
    BufferedImage textImage = SpriteLibrary.createTransparentSprite(width, height);
    drawBoldText(textImage.getGraphics(), text, 0, 0);
    return textImage;
  }

  /**
   * Returns a BufferedImage containing the input number rendered on one line (in map HP letters),
   *  on a transparent background, with no scaling applied.
   */
  public static BufferedImage getNumberAsImage(int num)
  {
    Sprite nums = SpriteLibrary.getMapUnitNumberSprites();
    int numWidth = nums.getFrame(0).getWidth();
    int numHeight = nums.getFrame(0).getHeight();

    int digits = 1, divisor = 10; // Start the count at one, since "0" is still one digit
    while (num / divisor > 0)
    {
      divisor *= 10;
      ++digits;
    }

    int width = nums.getFrame(0).getWidth() * digits;
    int height = nums.getFrame(0).getHeight();
    BufferedImage numImage = SpriteLibrary.createTransparentSprite(width, height);
    Graphics g = numImage.getGraphics();

    int x = width;
    do // We divide by 10 and truncate each time; expect three loops max.
    {
      int frame = num % 10;
      x -= numWidth; // Move the x-draw location to the left.
      g.drawImage(nums.getFrame(frame), x, 0, numWidth, numHeight, null);
      num /= 10; // Shift to the next higher digit in the number.
    } while (num > 0);
    return numImage;
  }

  /**
   * Code kinda-not-really stolen from https://stackoverflow.com/questions/20826216/copy-two-bufferedimages-into-one-image-side-by-side
   * Joins BufferedImages, creating a single contiguous image
   */
  public static BufferedImage joinBufferedImages(BufferedImage[] frames, int imageSpacing)
  {
    // aggregate sizing
    int width = 0;
    int height = frames[0].getHeight();
    for(BufferedImage frame : frames)
    {
      width += (frame.getWidth() + imageSpacing);
      if(height != frame.getHeight())
      {
        height = Math.max(height, frame.getHeight());
        System.out.println("WARNING: Joining images with unequal heights");
      }
    }

    //create a new buffer and draw two image into the new image
    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics g = newImage.getGraphics();

    int currentOffset = 0;
    for( int i = 0; i < frames.length; i++ )
    {
      g.drawImage(frames[i], currentOffset, 0, null);
      currentOffset += (frames[i].getWidth() + imageSpacing);
    }
    return newImage;
  }
}
