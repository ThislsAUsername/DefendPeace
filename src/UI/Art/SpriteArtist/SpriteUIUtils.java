package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import UI.UIUtils;

public class SpriteUIUtils
{
  public static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  public static final Color MENUBGCOLOR = new Color(234, 204, 154);
  public static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private static HashMap<Color, Sprite> coloredCursors;
  public static void drawCursor(Graphics g, int x, int y, int w, int h, Color color)
  {
    if( null == coloredCursors )
    {
      coloredCursors = new HashMap<Color, Sprite>();
    }
    if( !coloredCursors.containsKey(color) )
    {
      Sprite newCursor = new Sprite(SpriteLibrary.getCursorSprites());
      newCursor.colorize(UIUtils.defaultMapColors[4], color);
      coloredCursors.put(color, newCursor);
    }

    // Draw the arrows around the focused player attribute.
    Sprite cursor = coloredCursors.get(color);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(0), x, y, 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(1), x+w, y, 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(2), x+w, y+h, 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(3), x, y+h, 1);
  }

  /**
   * Loads the image at the given file location and returns it as a BufferedImage.
   * @param filename The full file-path to an image on disk.
   * @return The file as a BufferedImage, or null if the file cannot be read.
   */
  public static BufferedImage loadSpriteSheetFile(String filename)
  {
    BufferedImage bi = null;
    try
    {
      File imgFile = new File(filename);
      if( imgFile.exists() && !imgFile.isDirectory() )
        bi = ImageIO.read(imgFile);
      else System.out.println("WARNING! Resource file " + filename + " does not exist.");
    }
    catch (IOException ioex)
    {
      System.out.println("WARNING! Exception loading resource file " + filename);
      bi = null;
    }
    return bi;
  }

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
      SpriteLibrary.drawTextSmallCaps(g, items.get(i), hBuffer, txtY, 1);
    }

    return menuImage;
  }

  /**
   * Returns an image with the input string printed within the specified width, in normal text.
   * @param reqWidth: Actual UI size in pixels that you want to fit the text into.
   */
  public static BufferedImage drawTextToWidth(String prose, int reqWidth)
  {
    // Figure out how big our text is.
    int drawScale = SpriteOptions.getDrawScale();
    int characterWidth = SpriteLibrary.getLettersUppercase().getFrame(0).getWidth() * drawScale;
    int characterHeight = SpriteLibrary.getLettersUppercase().getFrame(0).getHeight() * drawScale;

    ArrayList<String> lines = new ArrayList<String>();
    // Unload our prose into the lines it already has
    lines.addAll(Arrays.asList(prose.split("\\r\\n|\\n|\\r"))); // Should match all common newline formats. If we ever go to Java 8, use \R

    if( reqWidth < characterWidth || lines.isEmpty() )
      return SpriteLibrary.createDefaultBlankSprite(1, 1); // zero-dimensioned images aren't kosher

    for( int i = 0; i < lines.size(); ++i ) // basic for, since we care about indices
    {
      String line = lines.get(i);
      if( line.length() * characterWidth <= reqWidth ) // if the line's short enough already, don't split it further
        continue;

      lines.remove(i);

      // See if we can split the line on a space
      int splitIndex = line.substring(0, reqWidth / characterWidth).lastIndexOf(' ');
      if( splitIndex < 1 ) // no spaces we can split on
      {
        // Can't be helped. Split in the middle of the word
        splitIndex = reqWidth / characterWidth;
      }

      lines.add(i, line.substring(splitIndex)); // put in the second half
      lines.add(i, line.substring(0, splitIndex)); // and then the first half behind it
    }

    int totalTextHeight = ((lines.isEmpty()) ? 0 : getMenuTextHeightPx(lines, characterHeight));
    // Build our image.
    BufferedImage menuImage = null;
    menuImage = SpriteLibrary.createTransparentSprite(reqWidth, totalTextHeight);
    Graphics g = menuImage.getGraphics();

    // Draw the actual text.
    for( int txtY = 0, i = 0; i < lines.size(); ++i, txtY += characterHeight + drawScale )
    {
      SpriteLibrary.drawText(g, lines.get(i), 0, txtY, drawScale);
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
      SpriteLibrary.drawImageCenteredOnPoint(graphics, display, width/2, height/2, 1);
    }

    public void render(Graphics g)
    {
      g.drawImage(content, xPos, yPos, width, height, null);
    }
  }

  public static int getMenuTextWidthPx(ArrayList<String> menuOptions, int scaledCharWidthPx)
  {
    int maxWidth = 0;
    for( int i = 0; i < menuOptions.size(); ++i )
    {
      int optw = menuOptions.get(i).length() * scaledCharWidthPx;
      maxWidth = (optw > maxWidth) ? optw : maxWidth;
    }

    return maxWidth;
  }

  public static int getMenuTextHeightPx(ArrayList<String> menuOptions, int scaledCharHeightPx)
  {
    // Height of the letters plus 1 (for buffer between menu options), times the number of entries,
    // minus 1 because there is no buffer after the last entry.
    return (scaledCharHeightPx + SpriteOptions.getDrawScale()) * menuOptions.size() - SpriteOptions.getDrawScale();
  }

  public static int getTileSize()
  {
    return SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale();
  }
}
