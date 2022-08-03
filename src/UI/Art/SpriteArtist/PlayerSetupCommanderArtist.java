package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderLibrary;
import Engine.IController;
import Engine.OptionSelector;
import UI.PlayerSetupCommanderController;
import UI.SlidingValue;
import UI.UIUtils;

public class PlayerSetupCommanderArtist
{
  private static HashMap<Integer, CommanderPanel> coPanels = new HashMap<Integer, CommanderPanel>();

  private static PlayerSetupCommanderController myControl;
  private static SlidingValue panelOffsetY = new SlidingValue(0);
  private static SlidingValue panelDrawW = new SlidingValue(0);
  private static SlidingValue tagsOffsetX = new SlidingValue(0);

  public static void draw(Graphics g, IController controller, ArrayList<CommanderInfo> infos, Color playerColor)
  {
    PlayerSetupCommanderController control = (PlayerSetupCommanderController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupCommanderController was given the wrong controller!");
    }
    boolean snapCursor = myControl != control;
    myControl = control;

    // Define the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    /////////////// Tooltip ////////////////////////////
    BufferedImage tooltip = SpriteUIUtils.makeTextFrame("Press Q for more info", 3, 2);
    myG.drawImage(tooltip, myWidth - tooltip.getWidth(), 3, null);

    /////////////// Tag Picker Panels //////////////////////
    if( myControl.shouldSelectMultiCO || myControl.tagCmdrList.size() > 1 )
    {
      int tagPickerOffset = tooltip.getHeight() + 2 + SpriteLibrary.getCursorSprites().getFrame(0).getHeight();
      drawTagPickerPanels(myG, tagPickerOffset, myWidth, infos, playerColor, snapCursor);
    }

    /////////////// Commander Portrait //////////////////////
    int highlightedCmdr = control.cmdrSelector.getSelectionNormalized();
    BufferedImage likeness = SpriteLibrary.getCommanderSprites( infos.get(highlightedCmdr).name ).body;
    final int likenessVOffset = Math.max(0, myHeight - likeness.getHeight());
    myG.drawImage(likeness, myWidth-likeness.getWidth(), likenessVOffset, null);

    /////////////// Commander Panels //////////////////////
    drawCmdrPickerPanels(myG, myHeight, infos, playerColor, snapCursor);

    // Draw the composed image to the window at scale.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
  }

  public static void drawCmdrPickerPanels(
                       Graphics myG, int myHeight,
                       ArrayList<CommanderInfo> infos, Color playerColor,
                       boolean snapCursor)
  {
    int highlightedCmdr = myControl.cmdrSelector.getSelectionNormalized();
    int highlightedCmdrOffset = myControl.cmdrSelector.getSelectionAbsolute();
    // Calculate the vertical space each player panel will consume.
    int panelBuffer = 3;
    int panelHeight = CommanderPanel.PANEL_HEIGHT+panelBuffer;

    // Find where the zeroth Commander should be drawn.
    panelOffsetY.set(highlightedCmdrOffset*panelHeight, snapCursor);
    int drawYCenter = myHeight / 2 - panelOffsetY.geti();

    // We're gonna make this an endless scroll, so back up (in y-space and in the CO list) until
    // we find the Commander that would be off the top of the screen.
    OptionSelector coToDraw = new OptionSelector(infos.size());
    while( drawYCenter + CommanderPanel.PANEL_HEIGHT/2 > 0 )
    {
      coToDraw.prev();
      drawYCenter -= panelHeight;
    }
    // We don't actually want to draw something off the screen, so go forward until we are on-screen again.
    while( drawYCenter + CommanderPanel.PANEL_HEIGHT/2 < 0 )
    {
      coToDraw.next();
      drawYCenter += panelHeight;
    }

    // Draw all of the army panels that are visible.
    int drawX = SpriteLibrary.getCursorSprites().getFrame(0).getWidth(); // Make sure we have room to draw the cursor around the frame.
    for(; drawYCenter - CommanderPanel.PANEL_HEIGHT/2 < myHeight ; coToDraw.next(), drawYCenter += (panelHeight))
    {
      CommanderInfo coInfo = infos.get(coToDraw.getSelectionNormalized());
      Integer key = coToDraw.getSelectionNormalized();

      // Get the relevant PlayerPanel.
      if( !coPanels.containsKey(key) ) coPanels.put(key, new CommanderPanel(coInfo, playerColor));
      CommanderPanel panel = coPanels.get(key);

      // Update the PlayerPanel and render it to an image.
      BufferedImage playerImage = panel.update(coInfo, playerColor);

      int drawY = drawYCenter - playerImage.getHeight()/2;
      myG.drawImage(playerImage, drawX, drawY, null);

      // Set the cursor width.
      if( highlightedCmdr == coToDraw.getSelectionNormalized() )
      {
        panelDrawW.set(playerImage.getWidth(), snapCursor);
      }
    }

    // Draw the cursor over the center option.
    SpriteCursor.draw(myG, drawX, myHeight/2 - CommanderPanel.PANEL_HEIGHT/2, panelDrawW.geti(), CommanderPanel.PANEL_HEIGHT, playerColor);
  }

  public static void drawTagPickerPanels(
                       Graphics myG, int tagPickerOffset, int myWidth,
                       ArrayList<CommanderInfo> infos, Color playerColor,
                       boolean snapCursor)
  {
    final int tagPicked = myControl.tagIndex.getSelectionNormalized();
    // Calculate the vertical space each player panel will consume.
    final int panelThickness = 1;
    final int panelBuffer = 2*panelThickness;
    final int panelWidth = CommanderPanel.eyesWidth+panelBuffer;
    final int panelHeight = CommanderPanel.eyesHeight+panelBuffer;

    final int panelSpacing = 1;
    final int panelXShift = panelWidth + panelSpacing;

    // Find where the zeroth Commander should be drawn.
    tagsOffsetX.set(tagPicked*panelXShift, snapCursor);
    int drawXCenter = myWidth / 2 - tagsOffsetX.geti();

    final int drawY = tagPickerOffset;
    final ArrayList<Integer> taggedCOs = myControl.tagCmdrList;
    for(int tagToDraw = 0; tagToDraw < taggedCOs.size(); ++tagToDraw)
    {
      CommanderInfo coInfo = infos.get(taggedCOs.get(tagToDraw));

      // Update the PlayerPanel and render it to an image.
      BufferedImage playerImage = SpriteLibrary.getCommanderSprites( coInfo.name ).eyes;

      int drawX = 1 + drawXCenter - panelXShift/2 + (tagToDraw*panelXShift);
      myG.setColor(Color.BLACK);
      myG.fillRect(drawX, drawY, panelWidth, panelHeight);
      int dx = drawX+panelThickness, dy = drawY+panelThickness;
      myG.setColor(playerColor);
      myG.fillRect(dx, dy, panelWidth-panelThickness-1, panelHeight-panelThickness-1);

      // This check should be fine since we can't save/load a game-creation in progress
      if( coInfo == CommanderLibrary.NotACO.getInfo() )
      {
        myG.setColor(Color.BLACK);
        // Draw a little plus sign
        myG.drawLine(drawX + 2*panelWidth/7, drawY +   panelHeight/2,
                     drawX + 5*panelWidth/7, drawY +   panelHeight/2);
        myG.drawLine(drawX +   panelWidth/2, drawY + 2*panelHeight/7,
                     drawX +   panelWidth/2, drawY + 5*panelHeight/7);
      }
      else
        myG.drawImage(playerImage, dx, dy, null);
    }

    // Draw the cursor over the center option.
    SpriteCursor.draw(myG, myWidth/2 - panelWidth/2, drawY, panelWidth, CommanderPanel.eyesHeight+panelBuffer, playerColor);
  }

  /**
   * Renders itself into an image like this, with no scaling applied.
   * +----------------+--------------------+
   * |                |                    |
   * |   Cmdr Eyes    |   CommanderName    |
   * |                |                    |
   * +----------------+--------------------+
   */
  private static class CommanderPanel
  {
    // A couple of helper quantities.
    public static int textBufferPx = 4;
    public static int eyesWidth = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getWidth();
    public static int eyesHeight = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getHeight();

    // Total vertical panel space, sans scaling.
    public static final int PANEL_HEIGHT = eyesHeight + 2; // Eyes plus 1 above and below.

    // The composed TeamPanel image.
    private BufferedImage myImage;

    // Each frame that makes up the larger panel.
    private SpriteUIUtils.ImageFrame commanderFace;
    private SpriteUIUtils.ImageFrame commanderName;

    // Stored values.
    String myCoName;
    String myColor;

    public CommanderPanel(CommanderInfo info, Color color)
    {
      update(info, color);
    }

    public BufferedImage update(CommanderInfo coInfo, Color color)
    {
      if( !coInfo.name.equals(myCoName) || !UIUtils.getPaletteName(color).equals(myColor))
      {
        myColor = UIUtils.getPaletteName(color);
        commanderFace = new SpriteUIUtils.ImageFrame(1, 1, eyesWidth, eyesHeight, color,
            color, true, SpriteLibrary.getCommanderSprites( coInfo.name ).eyes);

        // If only the color changed, we don't need to redraw the nameplate, so check that the name actually changed.
        if( !coInfo.name.equals(myCoName) )
        {
          myCoName = coInfo.name;
          PixelFont pf = SpriteLibrary.getFontStandard();
          int newWidth = pf.getWidth(myCoName) + textBufferPx*2;
          BufferedImage namePlate = SpriteUIUtils.getTextAsImage(myCoName);
          commanderName = new SpriteUIUtils.ImageFrame(commanderFace.width+2, 1, newWidth, commanderFace.height,
              SpriteUIUtils.MENUHIGHLIGHTCOLOR, SpriteUIUtils.MENUBGCOLOR, false, namePlate);
        }

        // Re-render the panel.
        myImage = SpriteLibrary.createTransparentSprite( commanderFace.width + commanderName.width + 3, PANEL_HEIGHT );
        Graphics g = myImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, commanderFace.width + commanderName.width + 3, myImage.getHeight());
        commanderFace.render(g);
        commanderName.render(g);
      }

      return myImage;
    }
  }
}
