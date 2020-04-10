package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import UI.InputHandler;
import UI.InputHandler.InputAction;

public class ControlOptionsSetupArtist
{
  private static InputHandler myControl = null;

  static ArrayList<KeyBindingPanel> kbps;
  static {
    kbps = new ArrayList<KeyBindingPanel>();
    for( InputAction ia : InputHandler.InputAction.values() )
    {
      kbps.add(new KeyBindingPanel(ia));
    }
  }

  private static SpriteCursor spriteCursor = new SpriteCursor();

  public static void draw(Graphics g, InputHandler control)
  {
    // Draw a fancy background.
    DiagonalBlindsBG.draw(g);

    myControl = control;

    // Set up some initial parameters.
    int spacing = kbps.get(0).getImage().getHeight()*2;
    int xDraw = spacing;
    int yDraw = spacing/2;

    // Find the selected command/key.
    int selectedAction = myControl.actionCommandSelector.getSelectionNormalized();
    int selectedKey = myControl.getKeySelector(InputHandler.InputAction.values()[selectedAction]).getSelectionNormalized();

    // Create an un-scaled image to draw everything at real size before scaling it to the screen.
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage controlsImage = SpriteLibrary.createTransparentSprite(dimensions.width/drawScale, dimensions.height/drawScale);
    Graphics cig = controlsImage.getGraphics();

    // Loop through and draw everything, building or updating the necessary images.
    for( int ip = 0; ip < kbps.size(); ++ip )
    {
      KeyBindingPanel kbp = kbps.get(ip);
      boolean drawAltAdd = (ip == selectedAction) && myControl.isAssigningKey();
      BufferedImage panel = kbp.getImage(drawAltAdd);
      cig.drawImage(panel, xDraw, yDraw, null);

      if( ip == selectedAction ) // Draw the cursor over the selected item.
      {
        kbp.setCursorLocationRelative(spriteCursor, selectedKey, xDraw, yDraw);
        spriteCursor.draw(cig);
      }

      yDraw += spacing;
    }

    // Redraw to the screen at scale.
    g.drawImage(controlsImage, 0, 0, controlsImage.getWidth()*drawScale, controlsImage.getHeight()*drawScale, null);
  }

  /**
   * Renders itself into an image like this, with no scaling applied.
   * +---------+  +------+  +------+  +-----+
   * | Command |  | Key1 |  | Key2 |  | Add |
   * +---------+  +------+  +------+  +-----+
   */
  private static class KeyBindingPanel
  {
    InputHandler.InputAction myAction;

    // A couple of helper quantities.
    private static final int textBufferPx = 4;
    private static final int keyBufferPx = 8;

    // The composed TeamPanel image.
    private BufferedImage myImage;

    // Each frame that makes up the larger panel.
    private SpriteUIUtils.ImageFrame commandNameFrame;
    private ArrayList<SpriteUIUtils.ImageFrame> boundKeyFrames;

    private int numFrames = -1;
    private boolean isAdding = false;

    public KeyBindingPanel(InputHandler.InputAction action)
    {
      myAction = action;
    }

    public BufferedImage getImage()
    {
      return getImage(false);
    }

    public BufferedImage getImage(boolean addingKey)
    {
      // If the number of bound keys hasn't changed, status quo remains (-1 for Add).
      if( (numFrames-1) == InputHandler.getBoundKeyCodes(myAction).size()
          && isAdding == addingKey )
      {
        return myImage;
      }

      boolean notSmallCaps = true;
      int xoffset = 0;
      { // Build the first image, representing the name of the command to which the keys are bound.
        BufferedImage commandNamePane = SpriteUIUtils.getTextAsImage(myAction.name(), notSmallCaps);
        int width = commandNamePane.getWidth() + textBufferPx;
        int height = commandNamePane.getHeight() + textBufferPx;
        commandNameFrame = new SpriteUIUtils.ImageFrame(xoffset, 0, width, height,
            SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR, true, commandNamePane);
        xoffset += commandNameFrame.width + keyBufferPx;
      }

      // Iterate over all bound keys and create images to display them as well.
      boundKeyFrames = new ArrayList<SpriteUIUtils.ImageFrame>();
      ArrayList<String> keysNames = InputHandler.getBoundKeyNames(myAction);
      for( String keyName : keysNames )
      {
        BufferedImage keyPane = SpriteUIUtils.getTextAsImage(keyName, notSmallCaps);
        int paneWidth = keyPane.getWidth() + textBufferPx;
        int paneHeight = keyPane.getHeight() + textBufferPx;

        SpriteUIUtils.ImageFrame keyFrame = new SpriteUIUtils.ImageFrame(xoffset, 0, paneWidth, paneHeight,
            SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR, true, keyPane);

        boundKeyFrames.add(keyFrame); // Add to collection, or just to image? use a map(key->image)?

        xoffset += keyFrame.width + keyBufferPx;
      }

      { // One more for the "Add" command.
        String addText = (addingKey) ? "..." : "Add";
        BufferedImage addPane = SpriteUIUtils.getTextAsImage(addText, notSmallCaps);
        int addWidth = addPane.getWidth() + textBufferPx;
        int addHeight = addPane.getHeight() + textBufferPx;
        SpriteUIUtils.ImageFrame addFrame = new SpriteUIUtils.ImageFrame(xoffset, 0, addWidth, addHeight,
            SpriteUIUtils.MENUHIGHLIGHTCOLOR, SpriteUIUtils.MENUBGCOLOR, false, addPane);

        boundKeyFrames.add(addFrame);
        xoffset += addFrame.width; // This is the last frame, so don't bother adding the final buffer.
      }

      int finalWidth = xoffset;

      // Re-render the panel.
      myImage = SpriteLibrary.createTransparentSprite( finalWidth, boundKeyFrames.get(0).height );
      Graphics g = myImage.getGraphics();
      commandNameFrame.render(g);
      for( SpriteUIUtils.ImageFrame frame : boundKeyFrames )
      {
        frame.render(g);
      }

      numFrames = boundKeyFrames.size();
      isAdding = addingKey;
      return myImage;
    }

    public void setCursorLocationRelative(SpriteCursor sc, int index, int xOff, int yOff)
    {
      SpriteUIUtils.ImageFrame imf = boundKeyFrames.get(index);
      sc.set(imf.xPos+xOff, imf.yPos+yOff, imf.width, imf.height);
      if( index == numFrames-1 ) sc.set(Color.GREEN); // Last slot is always "Add"
      else sc.set(Color.RED);
    }
  }
}
