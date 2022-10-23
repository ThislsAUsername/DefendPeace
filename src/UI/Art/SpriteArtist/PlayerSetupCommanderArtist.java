package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.CommanderInfo;
import Engine.IController;
import UI.PlayerSetupCommanderController;
import UI.SlidingValue;
import UI.UIUtils;
import UI.UIUtils.COSpriteSpec;

public class PlayerSetupCommanderArtist
{
  private static HashMap<Integer, CommanderPanel> coPanels = new HashMap<Integer, CommanderPanel>();

  private static PlayerSetupCommanderController myControl;
  private static SlidingValue panelOffsetY = new SlidingValue(0);
  private static int panelDrawW = CommanderPanel.eyesWidth + 2;
  private static SlidingValue panelDrawX = new SlidingValue(0);

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

    /////////////// Commander Panels //////////////////////
    drawCmdrPickerPanels(myG, myHeight, myWidth, infos, playerColor, snapCursor);

    /////////////// Tag Picker Panels //////////////////////
    if( myControl.shouldSelectMultiCO || myControl.tagCmdrList.size() > 1 )
    {
      drawTagPickerPanels(myG, myWidth, infos, playerColor, snapCursor);
    }

    /////////////// Tooltip ////////////////////////////
    BufferedImage tooltip = SpriteUIUtils.makeTextFrame("Press Q for more info", 3, 2);
    myG.drawImage(tooltip, myWidth - tooltip.getWidth(), 3, null);

    // Draw the composed image to the window at scale.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
  }

  public static void drawCmdrPickerPanels(
                       Graphics myG, int myHeight, int myWidth,
                       ArrayList<CommanderInfo> infos, Color playerColor,
                       boolean snapCursor)
  {
    // Selected horizontal bin on the screen
    int binIndex        = myControl.cmdrBinSelector.getSelectionNormalized();
    // Index into that bin that's selected
    int coIndex         = myControl.cmdrInBinSelector.getSelectionNormalized();
    // Value of that selection; index into the list of CO infos
    int highlightedCmdr = myControl.cmdrBins.get(binIndex).get(coIndex);
    // Calculate the vertical space each bin panel will consume.
    int panelBuffer = 3;
    int panelHeight = CommanderPanel.PANEL_HEIGHT+panelBuffer + 1;
    int panelShift  = textToastHeight + panelHeight + panelBuffer;

    // We're drawing the panels to align with the vertically-fixed cursor,
    // so figure out where the zeroth bin panel should be drawn.
    panelOffsetY.set(binIndex*panelShift, snapCursor);
    int drawY = myHeight / 2 - panelOffsetY.geti() - panelHeight + panelBuffer + 1;

    // Selected CO's name for drawing later
    String coNameText = "";

    int binToDraw = 0;
    // X offset to start drawing CO faces from
    int baseDrawX = SpriteLibrary.getCursorSprites().getFrame(0).getWidth(); // Make sure we have room to draw the cursor around the frame.

    for(; drawY - CommanderPanel.PANEL_HEIGHT/2 < myHeight
        && binToDraw < myControl.cmdrBins.size();
        ++binToDraw )
    {
      int indexInBin = 0;
      int drawX = baseDrawX;

      // Draw the bin panel to go behind the COs
      final COSpriteSpec spriteSpec = myControl.binColorSpec.get(binToDraw);
      Color[] palette = UIUtils.defaultMapColors;
      String canonName = "MISC";
      if( Color.LIGHT_GRAY != spriteSpec.color )
      {
        palette = UIUtils.getMapUnitColors(spriteSpec.color).paletteColors;
        canonName = UIUtils.getCanonicalFactionName(spriteSpec);
      }
      int currentPanelBottomY = drawCmdrBin(myG, canonName, palette[5], palette[3], myWidth, drawY, panelHeight);

      // Actually draw the CO mugs
      ArrayList<Integer> currentBin = myControl.cmdrBins.get(binToDraw);
      while (drawX < myWidth && indexInBin < currentBin.size())
      {
        int coToDraw = currentBin.get(indexInBin);
        CommanderInfo coInfo = infos.get(coToDraw);
        Integer key = coToDraw;

        // Get the relevant PlayerPanel.
        if( !coPanels.containsKey(key) )
          coPanels.put(key, new CommanderPanel(coInfo, spriteSpec.color));
        CommanderPanel panel = coPanels.get(key);

        // Update the PlayerPanel and render it to an image.
        BufferedImage playerImage = panel.update(coInfo, spriteSpec.color);

        int drawCmdrY = drawY + textToastHeight + txtBuf;
        myG.drawImage(playerImage, drawX, drawCmdrY, null);

        // Set the cursor width.
        if( highlightedCmdr == coToDraw )
        {
          panelDrawX.set(drawX, snapCursor);
          coNameText = coInfo.name;
        }

        ++indexInBin;
        drawX += playerImage.getWidth() + panelBuffer;
      }

      drawY = currentPanelBottomY + panelBuffer;
    }

    final int cursorY = myHeight / 2 - CommanderPanel.PANEL_HEIGHT / 2;

    BufferedImage coNameFrame = SpriteUIUtils.makeTextFrame(coNameText, 2, 2);
    int drawNameX = panelDrawX.geti() + panelDrawW/2;
    int drawNameY = cursorY + CommanderPanel.PANEL_HEIGHT + coNameFrame.getHeight()/2 + 2;
    SpriteUIUtils.drawImageCenteredOnPoint(myG, coNameFrame, drawNameX, drawNameY);

    // Draw the cursor over the selected option.
    SpriteCursor.draw(myG, panelDrawX.geti(), cursorY, panelDrawW, CommanderPanel.PANEL_HEIGHT, playerColor);
  }

  static final int textWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
  static final int textHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();
  static final int txtBuf = 2;
  static final int textToastHeight = textHeight + txtBuf; // upper buffer only
  public static int drawCmdrBin(Graphics g, String label, Color bg,  Color frame, int screenWidth, int y, int bodyHeight)
  {
    int textToastWidth  = textWidth*label.length();

    // Smooths between the label backing to the CO face holder
    Polygon triangle = new Polygon();
    triangle.addPoint(txtBuf+textToastWidth                , y);                 // top left
    triangle.addPoint(txtBuf+textToastWidth                , y+textToastHeight); // bottom left
    triangle.addPoint(txtBuf+textToastWidth+textToastHeight, y+textToastHeight); // right

    g.setColor(frame);
    g.fillPolygon(triangle);
    g.fillRect(0, y                 , txtBuf+textToastWidth , bodyHeight); // behind text
    g.fillRect(0, y+textToastHeight , screenWidth           , bodyHeight); // main body

    g.setColor(bg);
    for( int i = 0; i < 3; ++i )
      triangle.ypoints[i] += 1; // Shift one pixel down to expose the frame
    g.fillPolygon(triangle);
    g.fillRect(0, y+1                , txtBuf+textToastWidth, bodyHeight-2);
    g.fillRect(0, y+1+textToastHeight, screenWidth          , bodyHeight-2);

    SpriteUIUtils.drawTextSmallCaps(g, label, txtBuf, y + txtBuf);

    return y + textToastHeight + bodyHeight;
  }

  public static void drawTagPickerPanels(
                       Graphics myG, int myWidth,
                       ArrayList<CommanderInfo> infos, Color playerColor,
                       boolean snapCursor)
  {
    // Calculate the vertical space each player panel will consume.
    final int panelThickness = 1;
    final int panelBuffer = 2*panelThickness;
    final int panelWidth = CommanderPanel.eyesWidth+panelBuffer;
    final int panelHeight = CommanderPanel.eyesHeight+panelBuffer;

    // Take over the top of the screen
    SpriteUIUtils.drawMenuFrame(myG, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR,
        0, -1, myWidth, panelHeight*3/2, 2);

    final int panelSpacing = 1;
    final int panelXShift = panelWidth + panelSpacing;

    // Draw the list of COs in your tag from left to right
    final int drawY = 4;
    final ArrayList<Integer> taggedCOs = myControl.tagCmdrList;
    for( int tagToDraw = 0; tagToDraw < taggedCOs.size(); ++tagToDraw )
    {
      CommanderInfo coInfo = infos.get(taggedCOs.get(tagToDraw));

      BufferedImage playerImage = SpriteLibrary.getCommanderSprites( coInfo.name ).eyes;

      int drawX = 4 + (tagToDraw*panelXShift);
      myG.setColor(Color.BLACK);
      myG.fillRect(drawX, drawY, panelWidth, panelHeight);
      int dx = drawX+panelThickness, dy = drawY+panelThickness;
      myG.setColor(playerColor);
      myG.fillRect(dx, dy, panelWidth-panelThickness-1, panelHeight-panelThickness-1);

      myG.drawImage(playerImage, dx, dy, null);
    }
  }

  /**
   * Renders itself into an image like this, with no scaling applied.
   * +----------------+
   * |                |
   * |   Cmdr Eyes    |
   * |                |
   * +----------------+
   */
  private static class CommanderPanel
  {
    // A couple of helper quantities.
    public static int eyesWidth = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getWidth();
    public static int eyesHeight = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getHeight();

    // Total vertical panel space, sans scaling.
    public static final int PANEL_HEIGHT = eyesHeight + 2; // Eyes plus 1 above and below.

    // The composed TeamPanel image.
    private BufferedImage myImage;

    // Each frame that makes up the larger panel.
    private SpriteUIUtils.ImageFrame commanderFace;

    // Stored values.
    Color myColor;

    public CommanderPanel(CommanderInfo info, Color color)
    {
      update(info, color);
    }

    public BufferedImage update(CommanderInfo coInfo, Color color)
    {
      if( !color.equals(myColor))
      {
        myColor = color;
        commanderFace = new SpriteUIUtils.ImageFrame(1, 1, eyesWidth, eyesHeight, color,
            color, true, SpriteLibrary.getCommanderSprites( coInfo.name ).eyes);

        // Re-render the panel.
        myImage = SpriteLibrary.createTransparentSprite( commanderFace.width + 3, PANEL_HEIGHT );
        Graphics g = myImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, commanderFace.width + 3, myImage.getHeight());
        commanderFace.render(g);
      }

      return myImage;
    }
  }
}
