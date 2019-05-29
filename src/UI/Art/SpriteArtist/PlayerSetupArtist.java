package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Terrain.MapInfo;
import UI.PlayerSetupController;
import UI.PlayerSetupInfo;
import UI.UIUtils;
import Units.UnitModel;

public class PlayerSetupArtist
{
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);
  private static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private static double animHighlightedPlayer = 0;

  private static PlayerSetupController myControl = null;

  private static HashMap<Integer, PlayerPanel> playerPanels = new HashMap<Integer, PlayerPanel>();

  public static void draw(Graphics g, MapInfo mapInfo, PlayerSetupController control)
  {
    // If control has changed, we just entered a new CO setup screen. We don't want to
    //   animate a menu transition based on the last time we were choosing COs, since
    //   this class is static, but the CO select screen is not.
    if(myControl != control)
    {
      animHighlightedPlayer = control.getHighlightedPlayer();
      myControl = control;
    }

    // Get the draw space
    Dimension dimensions = SpriteOptions.getScreenDimensions();

    // Draw fancy background effects.
    DiagonalBlindsBG.draw(g);

    /////////////////// Ready Button ////////////////////////
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage readyButton = SpriteUIUtils.makeTextFrame("Ready!", 3*drawScale, 2*drawScale);
    SpriteLibrary.drawImageCenteredOnPoint(g, readyButton, dimensions.width-(int)(readyButton.getWidth()*0.75), dimensions.height/2, 1);
    int readyAreaWidth = (int)(readyButton.getWidth() * 1.5);

    /////////////////// Player Panels ///////////////////////
    int numCOs = mapInfo.getNumCos();
    int highlightedPlayer = myControl.getHighlightedPlayer();

    // Calculate the vertical space each player panel will consume.
    int panelHeight = PlayerPanel.PANEL_HEIGHT*drawScale+drawScale*3;

    // If we are moving from one option to another, calculate the intermediate draw location.
    if( animHighlightedPlayer != highlightedPlayer )
    {
      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedPlayer, highlightedPlayer);
      animHighlightedPlayer += slide;
    }

    // Define the space to draw the list of player CO portraits.
    int playerXCenter = ((dimensions.width - readyAreaWidth) / 2);
    int highlightedPlayerYCenter = dimensions.height / 2; // Whichever player has focus should be centered.

    // Find where the zeroth player CO should be drawn.
    // Shift from the center location by the spacing times the number of the highlighted option.
    int playerYCenter = (int)(highlightedPlayerYCenter - (animHighlightedPlayer * panelHeight));

    // Draw all of the player info.
    for(int i = 0; i < numCOs; ++i, playerYCenter += (panelHeight))
    {
      // Only bother to draw it if it is onscreen.
      if( (playerYCenter > -panelHeight/2) && ( playerYCenter < SpriteOptions.getScreenDimensions().getHeight()+(panelHeight/2) ) )
      {
        PlayerSetupInfo playerInfo = myControl.getPlayerInfo(i);
        Integer key = new Integer(i);

        // Get the relevant PlayerPanel.
        if( !playerPanels.containsKey(key) ) playerPanels.put(key, new PlayerPanel(playerInfo));
        PlayerPanel panel = playerPanels.get(key);

        // Update the PlayerPanel and render it to an image.
        BufferedImage playerImage = panel.update(playerInfo);

        int drawX = playerXCenter - (playerImage.getWidth()*drawScale)/2;
        int drawY = playerYCenter - (playerImage.getHeight()*drawScale)/2;
        g.drawImage(playerImage, drawX, drawY, playerImage.getWidth()*drawScale, playerImage.getHeight()*drawScale, null);

        // Draw the cursor if this is the currently-highlighted player.
        if( (i == highlightedPlayer) && (myControl.getHighlightedCategory() != PlayerSetupController.SelectionCategories.START.ordinal()) )
        {
          PlayerPanel.Pane pane;
          switch(myControl.getHighlightedCategory())
          {
            default:
            case 0: // Commander
              pane = panel.commanderPane;
              break;
            case 1: // Faction/Color
              pane = panel.unitPane;
              break;
            case 2: // Team
              pane = panel.teamPane;
              break;
            case 3: // AI Controller
              pane = panel.aiPane;
              break;
          }
          drawSelector(g, drawX+pane.xPos*drawScale, drawY+pane.yPos*drawScale, pane.width*drawScale, pane.height*drawScale);
          drawSelector(g, drawX+pane.xPos*drawScale+1, drawY+pane.yPos*drawScale+1, pane.width*drawScale-2, pane.height*drawScale-2);
        }
      }
    }
  }

  private static void drawSelector(Graphics g, int x, int y, int w, int h)
  {
    // Draw the arrows around the focused player attribute.
    g.setColor(Color.GREEN);
    g.drawRect(x, y, w, h);
  }

  /**
   * Holds all attributes of a player. Can render itself into
   * an image like this, with no scaling applied.
   * +------------------------------------------------------+
   * | CommanderName - ColorName FactionName                |
   * +------------------+ +---------+ +---------+ +---------+
   * |                  | |         | |  Team   | | Control |
   * |                  | | Faction | +---------+ +---------+
   * |        CO        | |  Sprite | |         | |         |
   * |                  | |         | |    #    | | AI Name |
   * |                  | |         | |         | |         |
   * +------------------+ +---------+ +---------+ +---------+
   */
  private static class PlayerPanel
  {
    // A couple of helper quantities.
    private static int textVBuffer = 2;
    private static int portraitPx = SpriteLibrary.getCommanderSprites( "STRONG" ).head.getHeight(); // Faces are square.
    private static final int EXPECTED_TEXT_LENGTH = 9; // Reasonable expected length for Commander, color, faction, and AI names.

    // Total horizontal panel space, sans scaling.
    public static final int PANEL_WIDTH = (EXPECTED_TEXT_LENGTH*3 + 2)*SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth() + textVBuffer*2;

    // Total vertical panel space, sans scaling.
    public static final int PANEL_HEIGHT = /*Top border*/ 1 + portraitPx + /*name/face border*/ 1
        + /*portrait buffers*/ 2 + SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight() + textVBuffer*2 + /*bottom border*/1;

    private String commanderName;
    private String colorName;
    private String factionName;
    private int teamNumber;
    private String aiName;

    private BufferedImage myImage;
    private Pane descriptionPane;
    private Pane commanderPane;
    private Pane unitPane;
    private Pane teamLabel;
    private Pane teamPane;
    private Pane aiLabel;
    private Pane aiPane;

    public PlayerPanel(PlayerSetupInfo info)
    {
      myImage = SpriteLibrary.createDefaultBlankSprite( PANEL_WIDTH, PANEL_HEIGHT );
      Graphics g = myImage.getGraphics();
      g.setColor(MENUFRAMECOLOR);
      g.fillRect(0, 0, myImage.getWidth(), myImage.getHeight());

      // Create the two panes that don't ever change.
      teamLabel = new Pane(65, 12, 28, 10, MENUHIGHLIGHTCOLOR, MENUBGCOLOR, false, SpriteLibrary.getTextAsImage("TEAM"));
      aiLabel = new Pane(94, 12, 54, 10, MENUHIGHLIGHTCOLOR, MENUBGCOLOR, false, SpriteLibrary.getTextAsImage("CONTROL"));
      teamLabel.render(g);
      aiLabel.render(g);

      update(info);
    }

    public BufferedImage update(PlayerSetupInfo info)
    {
      // Pull out the info we need.
      // "Commander-Color Faction"
      StringBuffer coStrBuf = new StringBuffer(info.getCurrentCO().name);
      coStrBuf.append("-").append(UIUtils.getPaletteName(info.getCurrentColor())).append(" ").append(info.getCurrentFaction().name);

      boolean cmdrChanged = !info.getCurrentCO().name.equals(commanderName);
      boolean colorChanged = !UIUtils.getPaletteName(info.getCurrentColor()).equals(colorName);
      boolean factionChanged = !info.getCurrentFaction().name.equals(factionName);
      boolean teamChanged = teamNumber != info.getCurrentTeam();
      boolean aiChanged = !info.getCurrentAI().getName().equals(aiName);

      Graphics g = myImage.getGraphics();
      if( cmdrChanged || colorChanged || factionChanged )
      {
        descriptionPane = new Pane(1, 1, PANEL_WIDTH - 2, 10, MENUHIGHLIGHTCOLOR, MENUBGCOLOR, false, SpriteLibrary.getTextAsImage(coStrBuf.toString()));
        descriptionPane.render(g);
      }
      if( cmdrChanged )
      {
        commanderPane = new Pane(1, 12, portraitPx + 2, portraitPx + 2, info.getCurrentColor(),
            info.getCurrentColor(), true, SpriteLibrary.getCommanderSprites( info.getCurrentCO().name ).head);
        commanderPane.render(g);
      }
      if( factionChanged || colorChanged )
      {
        UnitSpriteSet inf = SpriteLibrary.getMapUnitSpriteSet(UnitModel.UnitEnum.INFANTRY, info.getCurrentFaction(), info.getCurrentColor());
        BufferedImage infSprite = inf.sprites[inf.ACTION_IDLE].getFrame(0);
        unitPane = new Pane(portraitPx + 4, 12, 28, portraitPx + 2, MENUBGCOLOR, MENUHIGHLIGHTCOLOR, true, infSprite);
        unitPane.render(g);
      }
      if( teamChanged )
      {
        teamPane = new Pane(65, 23, 28, 23, MENUBGCOLOR, MENUHIGHLIGHTCOLOR, true,
            SpriteLibrary.getMapUnitHPSprites().getFrame(info.getCurrentTeam()));
        teamPane.render(g);
      }
      if( aiChanged )
      {
        aiPane = new Pane(94, 23, 54, 23, MENUBGCOLOR, MENUHIGHLIGHTCOLOR, true, SpriteLibrary.getTextAsImage(info.getCurrentAI().getName()));
        aiPane.render(g);
      }

      return myImage;
    }

    /**
     * One of the info panes in the mosaic that is the player panel. Draws itself as a two-tone box with an image on top.
     */
    public static class Pane
    {
      public final int xPos;
      public final int yPos;
      public final int width;
      public final int height;
      private Color mainColor;
      private Color rimColor;
      private boolean rimIsUp;
      private BufferedImage content;
      public Pane(int x, int y, int w, int h, Color main, Color rim, boolean rimUp, BufferedImage display)
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
  }
}
