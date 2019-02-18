package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class SpriteUIUtils
{
  /**
   * Calculate the distance to move an image make it look like it slides quickly into place instead of
   * just snapping at each button-press. Distance moved per frame is proportional to distance from goal location.
   * It is expected that currentNum and targetNum will not differ by more than 1.0.
   * NOTE: currentNum and targetNum correspond to (relative) positions, not to pixels.
   * NOTE: This is calibrated for 60fps, and changing the frame rate will change the closure speed.
   * 
   */
  public static double calculateSlideAmount(double currentNum, int targetNum)
  {
    double animMoveFraction = 0.3; // Movement cap to prevent over-speedy menu movement.
    double animSnapDistance = 0.05; // Minimum distance at which point we just snap into place.
    double slide = 0; // Return value; the distance we actually are going to move.
    double diff = Math.abs(targetNum - currentNum);
    int sign = (targetNum > currentNum) ? 1 : -1; // Since we took abs(), make sure we can correct the sign.
    if( diff < animSnapDistance )
    { // If we are close enough, just move the exact distance.
      slide = diff;
    }
    else
    { // Move a fixed fraction of the remaining distance.
      slide = diff * animMoveFraction;
    }

    return slide * sign;
  }

  // stolen wholesale from https://stackoverflow.com/questions/8933893/convert-each-animated-gif-frame-to-a-separate-bufferedimage
  // edited slightly to suit our needs
  public static ImageFrame[] readGIF(ImageReader reader, int width, int height) throws IOException
  {
    ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);

    // removed searching for width and height; they are known at the time of calling the function
    
    BufferedImage master = null;
    Graphics2D masterGraphics = null;

    for( int frameIndex = 0;; frameIndex++ )
    {
      BufferedImage image;
      try
      {
        image = reader.read(frameIndex);
      }
      catch (IndexOutOfBoundsException io)
      {
        break;
      }

      IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
      IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
      int delay = Integer.valueOf(gce.getAttribute("delayTime"));
      String disposal = gce.getAttribute("disposalMethod");

      int x = 0;
      int y = 0;

      if( master == null )
      {
        master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        masterGraphics = master.createGraphics();
        masterGraphics.setBackground(new Color(0, 0, 0, 0));
      }
      
      // this used to be an else for the above if statement, but that broke the T-Copter animation
      NodeList children = root.getChildNodes();
      for( int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++ )
      {
        Node nodeItem = children.item(nodeIndex);
        if( nodeItem.getNodeName().equals("ImageDescriptor") )
        {
          NamedNodeMap map = nodeItem.getAttributes();
          x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
          y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
        }
      }
      masterGraphics.drawImage(image, x, y, null);

      BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
      frames.add(new ImageFrame(copy, delay, disposal));

      if( disposal.equals("restoreToPrevious") )
      {
        BufferedImage from = null;
        for( int i = frameIndex - 1; i >= 0; i-- )
        {
          if( !frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0 )
          {
            from = frames.get(i).getImage();
            break;
          }
        }

        master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
        masterGraphics = master.createGraphics();
        masterGraphics.setBackground(new Color(0, 0, 0, 0));
      }
      else if( disposal.equals("restoreToBackgroundColor") )
      {
        masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
      }
    }
    reader.dispose();

    return frames.toArray(new ImageFrame[frames.size()]);
  }

  public static class ImageFrame
  {
    private final int delay;
    private final BufferedImage image;
    private final String disposal;

    public ImageFrame(BufferedImage image, int delay, String disposal)
    {
      this.image = image;
      this.delay = delay;
      this.disposal = disposal;
    }

    public BufferedImage getImage()
    {
      return image;
    }

    public int getDelay()
    {
      return delay;
    }

    public String getDisposal()
    {
      return disposal;
    }
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
    int drawScale = SpriteOptions.getDrawScale();
    int menuTextWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth() * drawScale;
    int menuTextHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight() * drawScale;
    int menuWidth = (( items.isEmpty() ) ? 0 : getMenuTextWidthPx(items, menuTextWidth)) + hBuffer * 2;
    int menuHeight = (( items.isEmpty() ) ? 0 : getMenuTextHeightPx(items, menuTextHeight)) + vBuffer * 2;

    // Build our image.
    BufferedImage menuImage = null;
    if( menuWidth == 0 || menuHeight == 0 )
      menuImage = SpriteLibrary.createDefaultBlankSprite(1, 1); // zero-dimensioned images aren't kosher
    else
      menuImage = SpriteLibrary.createDefaultBlankSprite(menuWidth, menuHeight);
    Graphics g = menuImage.getGraphics();

    // Draw the nice box for our text.
    drawMenuFrame(g, bg, frame, 0, 0, menuWidth, menuHeight, vBuffer);

    // Draw the highlight for the currently-selected option.
    // selY = upper menu-frame buffer, plus (letter height, plus 1px-buffer, times number of options).
    int selY = vBuffer + (menuTextHeight + drawScale) * selection;
    g.setColor(focus);
    g.fillRect(0, selY, menuWidth, menuTextHeight);

    // Draw the actual menu text.
    for( int txtY = vBuffer, i = 0; i < items.size(); ++i, txtY += menuTextHeight + drawScale )
    {
      SpriteLibrary.drawTextSmallCaps(g, items.get(i), hBuffer, txtY, drawScale);
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
