package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import CommandingOfficers.CommanderLibrary;
import Engine.IController;
import Terrain.MapInfo;
import UI.PlayerSetupController;
import UI.PlayerSetupInfo;
import UI.SlidingValue;
import UI.UIUtils;
import Units.UnitModel;

public class PlayerSetupArtist
{
  private static SlidingValue animHighlightedPlayer = new SlidingValue(0);
  private static SpriteCursor spriteCursor = new SpriteCursor();

  private static PlayerSetupController myControl = null;

  private static HashMap<Integer, PlayerPanel> playerPanels = new HashMap<Integer, PlayerPanel>();

  public static void draw(Graphics g, MapInfo mapInfo, PlayerSetupController control)
  {
    // If control has changed, we just entered a new CO setup screen. We don't want to
    //   animate a menu transition based on the last time we were choosing COs, since
    //   this class is static, but the CO select screen is not.
    boolean snapCursor = myControl != control;
    myControl = control;

    // Draw fancy background effects.
    DiagonalBlindsBG.draw(g);

    IController subMenu = myControl.getSubMenu();
    if( null == subMenu )
    {
      drawPlayerSetup(g, mapInfo, snapCursor);
    }
    else
    {
      if( myControl.getHighlightedCategory() == PlayerSetupController.SelectionCategories.COMMANDER.ordinal() )
      {
        PlayerSetupCommanderArtist.draw(g, subMenu, CommanderLibrary.getCommanderList(), control.getPlayerInfo(control.getHighlightedPlayer()).getCurrentColor());
      }
      if( myControl.getHighlightedCategory() == PlayerSetupController.SelectionCategories.COLOR_FACTION.ordinal() )
      {
        PlayerSetupColorFactionArtist.draw(g, subMenu);
      }
      if( myControl.getHighlightedCategory() == PlayerSetupController.SelectionCategories.TEAM.ordinal() )
      {
        PlayerSetupTeamArtist.draw(g, mapInfo, subMenu);
      }
      if( myControl.getHighlightedCategory() == PlayerSetupController.SelectionCategories.AI.ordinal() )
      {
        PlayerSetupAiArtist.draw(g, subMenu, control.getPlayerInfo(control.getHighlightedPlayer()).getCurrentColor());
      }
    }
    // Start preloading infantry sprites in the background so the ColorFaction screen doesn't freeze on first entry.
    PlayerSetupColorFactionArtist.preloadOneInfantrySprite();
  }

  private static void drawPlayerSetup(Graphics g, MapInfo mapInfo, boolean snapCursor)
  {
    // Get the draw space. We'll draw it all in real size and then scale it when we draw to the window.
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int imageWidth = dimensions.width / drawScale;
    int imageHeight = dimensions.height / drawScale;
    BufferedImage image = SpriteLibrary.createTransparentSprite(imageWidth, imageHeight);
    Graphics myG = image.getGraphics();

    /////////////////// Ready Button ////////////////////////
    BufferedImage readyButton = SpriteUIUtils.makeTextFrame("Ready!", 3, 2);
    int readyX = imageWidth-(int)(readyButton.getWidth()*1.25);
    int readyY = imageHeight/2-readyButton.getHeight()/2;
    myG.drawImage(readyButton, readyX, readyY, null);
    int readyAreaWidth = (int)(readyButton.getWidth() * 1.5);

    /////////////////// Player Panels ///////////////////////
    int numCOs = mapInfo.getNumCos();
    int highlightedPlayer = myControl.getHighlightedPlayer();

    // Calculate the vertical space each player panel will consume.
    int panelHeight = PlayerPanel.PANEL_HEIGHT+3;

    // Define the space to draw the list of player CO portraits.
    int playerXCenter = ((imageWidth - readyAreaWidth) / 2);
    int highlightedPlayerYCenter = imageHeight / 2; // Whichever player has focus should be centered.

    // If we are moving from one option to another, calculate the intermediate draw location.
    if(snapCursor)
      animHighlightedPlayer.snap(highlightedPlayer*panelHeight);
    else
      animHighlightedPlayer.set(highlightedPlayer*panelHeight);

    // Find where the zeroth player CO should be drawn.
    // Shift from the center location by the spacing times the number of the highlighted option.
    int playerYCenter = highlightedPlayerYCenter - animHighlightedPlayer.geti();

    // Draw all of the player info.
    for(int i = 0; i < numCOs; ++i, playerYCenter += (panelHeight))
    {
      // Only bother to draw it if it is on-screen.
      if( (playerYCenter > -panelHeight/2) && ( playerYCenter < imageHeight+(panelHeight/2) ) )
      {
        PlayerSetupInfo playerInfo = myControl.getPlayerInfo(i);
        Integer key = new Integer(i);

        // Get the relevant PlayerPanel.
        if( !playerPanels.containsKey(key) ) playerPanels.put(key, new PlayerPanel(playerInfo));
        PlayerPanel panel = playerPanels.get(key);

        // Update the PlayerPanel and render it to an image.
        BufferedImage playerImage = panel.update(playerInfo);

        int drawX = playerXCenter - (playerImage.getWidth())/2;
        int drawY = playerYCenter - (playerImage.getHeight())/2;
        myG.drawImage(playerImage, drawX, drawY, playerImage.getWidth(), playerImage.getHeight(), null);
      }
    }

    // Figure out where to draw the cursor.
    PlayerSetupInfo info = myControl.getPlayerInfo(highlightedPlayer);
    spriteCursor.set(info.getCurrentColor());
    if( myControl.getHighlightedCategory() == PlayerSetupController.SelectionCategories.START.ordinal() )
    {
      // Ready is currently selected.
      if( snapCursor )
        spriteCursor.snap(readyX, readyY, readyButton.getWidth(), readyButton.getHeight());
      else
        spriteCursor.set(readyX, readyY, readyButton.getWidth(), readyButton.getHeight());
      spriteCursor.draw(myG);
    }
    else // Draw the cursor over the appropriate player info panel, if it is visible.
    {
      // If the user pushes up from zero, the panel at the end may not have been rendered yet, and may not exist. If so, build it.
      PlayerPanel panel = playerPanels.get(highlightedPlayer);
      if( null == panel )
      {
        panel = new PlayerPanel(myControl.getPlayerInfo(highlightedPlayer));
        playerPanels.put(highlightedPlayer, panel);
      }

      // Figure out which frame in the player panel is under the cursor.
      SpriteUIUtils.ImageFrame pane;
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
      BufferedImage playerImage = panel.getImage();
      int panelX = playerXCenter - (playerImage.getWidth())/2;
      int panelY = imageHeight / 2 - playerImage.getHeight()/2;
      if( snapCursor )
        spriteCursor.snap(panelX+pane.xPos, panelY+pane.yPos, pane.width, pane.height);
      else
        spriteCursor.set(panelX+pane.xPos, panelY+pane.yPos, pane.width, pane.height);
      spriteCursor.draw(myG);
    }

    // Finally, draw our rendered image onto the window.
    g.drawImage(image, 0, 0, imageWidth*drawScale, imageHeight*drawScale, null);
  }

  /**
   * Holds all attributes of a player. Can render itself into
   * an image like this, with no scaling applied.
   * +-----------------------------------------------+
   * | CommanderName - ColorName FactionName         |
   * +--------------+----------+---------+-----------+
   * |              |          |  Team   |  Control  |
   * |              |          +---------+-----------+
   * |      CO      |  Sprite  |         |           |
   * |              |          |    #    |  AI Name  |
   * |              |          |         |           |
   * +--------------+----------+---------+-----------+
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
    private int teamNumber = -99;
    private String aiName;

    private BufferedImage myImage;
    private SpriteUIUtils.ImageFrame descriptionPane;
    private SpriteUIUtils.ImageFrame commanderPane;
    private SpriteUIUtils.ImageFrame unitPane;
    private SpriteUIUtils.ImageFrame teamLabel;
    private SpriteUIUtils.ImageFrame teamPane;
    private SpriteUIUtils.ImageFrame aiLabel;
    private SpriteUIUtils.ImageFrame aiPane;

    public PlayerPanel(PlayerSetupInfo info)
    {
      myImage = SpriteLibrary.createDefaultBlankSprite( PANEL_WIDTH, PANEL_HEIGHT );
      Graphics g = myImage.getGraphics();
      g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
      g.fillRect(0, 0, myImage.getWidth(), myImage.getHeight());

      // Create the two panes that don't ever change.
      teamLabel = new SpriteUIUtils.ImageFrame(65, 12, 28, 10, SpriteUIUtils.MENUHIGHLIGHTCOLOR, SpriteUIUtils.MENUBGCOLOR, false, SpriteUIUtils.getTextAsImage("TEAM", true));
      aiLabel = new SpriteUIUtils.ImageFrame(94, 12, 54, 10, SpriteUIUtils.MENUHIGHLIGHTCOLOR, SpriteUIUtils.MENUBGCOLOR, false, SpriteUIUtils.getTextAsImage("CONTROL", true));
      teamLabel.render(g);
      aiLabel.render(g);

      update(info);
    }

    public BufferedImage update(PlayerSetupInfo info)
    {
      // Keep track of which things need to be redrawn.
      boolean cmdrChanged = !info.getCurrentCO().name.equals(commanderName);
      boolean colorChanged = !UIUtils.getPaletteName(info.getCurrentColor()).equals(colorName);
      boolean factionChanged = !info.getCurrentFaction().name.equals(factionName);
      boolean teamChanged = teamNumber != info.getCurrentTeam();
      boolean aiChanged = !info.getCurrentAI().getName().equals(aiName);

      Graphics g = myImage.getGraphics();
      if( cmdrChanged || factionChanged || colorChanged )
      {
        // Update saved values.
        commanderName = info.getCurrentCO().name;
        colorName = UIUtils.getPaletteName(info.getCurrentColor());
        factionName = info.getCurrentFaction().name;

        // Build the description text: "Commander-Color Faction"
        StringBuffer coStrBuf = new StringBuffer(commanderName);
        coStrBuf.append("-").append(UIUtils.getCanonicalFactionName(colorName, factionName));

        descriptionPane = new SpriteUIUtils.ImageFrame(1, 1, PANEL_WIDTH - 2, 10, SpriteUIUtils.MENUHIGHLIGHTCOLOR, SpriteUIUtils.MENUBGCOLOR, false, SpriteUIUtils.getTextAsImage(coStrBuf.toString(), true));
        descriptionPane.render(g);
      }
      if( cmdrChanged || colorChanged )
      {
        commanderPane = new SpriteUIUtils.ImageFrame(1, 12, portraitPx + 2, portraitPx + 2, info.getCurrentColor(),
            info.getCurrentColor(), true, SpriteLibrary.getCommanderSprites( info.getCurrentCO().name ).head);
        commanderPane.render(g);
      }
      if( factionChanged || colorChanged )
      {
        UnitSpriteSet inf = SpriteLibrary.getMapUnitSpriteSet(UnitModel.UnitEnum.INFANTRY, info.getCurrentFaction(), info.getCurrentColor());
        BufferedImage infSprite = inf.sprites[inf.ACTION_IDLE].getFrame(0);
        unitPane = new SpriteUIUtils.ImageFrame(portraitPx + 4, 12, 28, portraitPx + 2, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR, true, infSprite);
        unitPane.render(g);
      }
      if( teamChanged )
      {
        teamNumber = info.getCurrentTeam();
        teamPane = new SpriteUIUtils.ImageFrame(65, 23, 28, 23, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR, true,
            SpriteLibrary.getMapUnitHPSprites().getFrame(info.getCurrentTeam()));
        teamPane.render(g);
      }
      if( aiChanged )
      {
        aiName = info.getCurrentAI().getName();
        aiPane = new SpriteUIUtils.ImageFrame(94, 23, 54, 23, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR, true, SpriteUIUtils.getTextAsImage(info.getCurrentAI().getName(), true));
        aiPane.render(g);
      }

      return myImage;
    }

    public BufferedImage getImage()
    {
      return myImage;
    }
  }
}
