package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.CommanderInfo;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;
import UI.PlayerSetupCommanderController;
import UI.UIUtils;

public class PlayerSetupCommanderArtist
{
  private static HashMap<Integer, CommanderPanel> coPanels = new HashMap<Integer, CommanderPanel>();

  public static void draw(Graphics g, IController controller, ArrayList<CommanderInfo> infos, Color playerColor)
  {
    PlayerSetupCommanderController control = (PlayerSetupCommanderController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupCommanderController was given the wrong controller!");
    }

    // Define the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    // Allocate space for the Commander faces.
    int cursorSizePx = SpriteLibrary.getCursorSprites().getFrame(0).getWidth(); // Cursor frames are square.
    int coZoneYCenter = myHeight / 2;

    /////////////// Tooltip ////////////////////////////
    BufferedImage tooltip = SpriteUIUtils.makeTextFrame("Press Q for more info", 3, 2);
    myG.drawImage(tooltip, myWidth - tooltip.getWidth(), 3, null);

    /////////////// Commander Portrait //////////////////////
    int highlightedCommander = control.getSelectedCommander();
    BufferedImage likeness = SpriteLibrary.getCommanderSprites( infos.get(highlightedCommander).name ).body;
    myG.drawImage(likeness, myWidth-likeness.getWidth(), myHeight-likeness.getHeight(), null);

    /////////////// Team Panels //////////////////////
    // Calculate the vertical space each player panel will consume.
    int panelBuffer = 3;
    int panelHeight = CommanderPanel.PANEL_HEIGHT+panelBuffer;

    // If we are moving from one option to another, calculate the intermediate draw location.
//    if( animHighlightedPlayer != highlightedPlayer )
//    {
//      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedPlayer, highlightedPlayer);
//      animHighlightedPlayer += slide;
//    }

    // Find where the zeroth Commander should be drawn. Start by assuming it's centered.
    int drawYCenter = coZoneYCenter;

    // We're gonna make this an endless scroll, so back up (in y-space and in the CO list) until
    // we find the Commander that would be off the top of the screen.
    OptionSelector coToDraw = new OptionSelector(infos.size());
    coToDraw.setSelectedOption(highlightedCommander);
    while( drawYCenter + CommanderPanel.PANEL_HEIGHT/2 > 0 )
    {
      coToDraw.handleInput(InputAction.UP);
      drawYCenter -= panelHeight;
    }
    // We don't actually want to draw something off the screen, so go forward one again.
    coToDraw.handleInput(InputAction.DOWN);
    drawYCenter += panelHeight;

    // Draw all of the visible commander panels.
    for(; drawYCenter - CommanderPanel.PANEL_HEIGHT/2 < myHeight ; coToDraw.handleInput(InputAction.DOWN), drawYCenter += (panelHeight))
    {
      // Only bother to draw it if it is onscreen.
      if( (drawYCenter > -panelHeight/2) && ( drawYCenter < myHeight+(panelHeight/2) ) )
      {
        CommanderInfo coInfo = infos.get(coToDraw.getSelectionNormalized());
        Integer key = new Integer(coToDraw.getSelectionNormalized());

        // Get the relevant PlayerPanel.
        if( !coPanels.containsKey(key) ) coPanels.put(key, new CommanderPanel(coInfo, playerColor));
        CommanderPanel panel = coPanels.get(key);

        // Update the PlayerPanel and render it to an image.
        BufferedImage playerImage = panel.update(coInfo, playerColor);

        int drawX = cursorSizePx * 2;
        int drawY = drawYCenter - playerImage.getHeight()/2;
        myG.drawImage(playerImage, drawX, drawY, null);

        // Draw the cursor if this panel is highlighted
        if( highlightedCommander == coToDraw.getSelectionNormalized() )
        {
          SpriteUIUtils.drawCursor(myG, drawX, drawY, playerImage.getWidth(), playerImage.getHeight(), playerColor);
        }
      }
    }

    /////////////// Cursor //////////////////////
//    CommanderPanel panel = coPanels.get(highlightedPlayer);
//    int highlightedPanelY = (int)(coZoneYCenter - CommanderPanel.PANEL_HEIGHT/2);

    // Draw the composed image to the window at scale.
    g.drawImage(image, 0, 0, dimensions.width, dimensions.height, null);
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
    private static int textBufferPx = 2;
    private static int eyesWidth = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getWidth();
    private static int eyesHeight = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getHeight();

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
            color, true, SpriteLibrary.getCommanderSprites( coInfo.name ).head);

        // If only the color changed, we don't need to redraw the nameplate, so check that the name actually changed.
        if( !coInfo.name.equals(myCoName) )
        {
          myCoName = coInfo.name;
          int newWidth = myCoName.length() * SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth() + textBufferPx*2;
          BufferedImage namePlate = SpriteLibrary.getTextAsImage(myCoName);
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
