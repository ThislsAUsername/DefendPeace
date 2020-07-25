package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Engine.IController;
import Engine.XYCoord;
import Terrain.MapInfo;
import UI.PlayerSetupInfo;
import UI.PlayerSetupTeamController;
import UI.SlidingValue;
import UI.UIUtils;

public class PlayerSetupTeamArtist
{
  private static HashMap<Integer, TeamPanel> teamPanels = new HashMap<Integer, TeamPanel>();
  private static SlidingValue player0YCenter = new SlidingValue(0);
  private static IController myControl;

  public static void draw(Graphics g, MapInfo mapInfo, IController controller)
  {
    PlayerSetupTeamController control = (PlayerSetupTeamController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupColorFactionArtist was given the wrong controller!");
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

    // Allocate space for player info.
    Sprite arrowheads = new Sprite(SpriteLibrary.getArrowheadSprites()); // U R D L. We only use L and R here.
    arrowheads.colorize(UIUtils.defaultMapColors[4], control.getPlayerInfo(control.getHighlightedPlayer()).getCurrentColor());
    int arrowheadPx = arrowheads.getFrame(0).getWidth(); // Arrowheads are square.
    int playerZoneWidth = arrowheadPx*4 + TeamPanel.PANEL_WIDTH;
    int playerZoneYCenter = myHeight / 2;

    /////////////// Team Panels //////////////////////
    // Left and right arrowheads around the center panel.
    myG.drawImage(arrowheads.getFrame(3), arrowheadPx, playerZoneYCenter - arrowheadPx/2, null);
    myG.drawImage(arrowheads.getFrame(1), arrowheadPx*2 + TeamPanel.PANEL_WIDTH, playerZoneYCenter - arrowheadPx/2, null);

    // Draw the team panels.
    int numCOs = mapInfo.getNumCos();
    int highlightedPlayer = control.getHighlightedPlayer();

    // Calculate the vertical space each player panel will consume.
    int panelBuffer = 3;
    int panelHeight = TeamPanel.PANEL_HEIGHT+panelBuffer;

    // Find where the zeroth player CO should be drawn.
    // Shift from the center location by the spacing times the number of the highlighted option.
    int target = (int)(playerZoneYCenter - (highlightedPlayer * panelHeight));
    player0YCenter.set(target, snapCursor); // If we just entered this screen then snap into position instead of scrolling.
    int drawYCenter = (int)player0YCenter.get();

    // Draw all of the visible team panels.
    for(int i = 0; i < numCOs; ++i, drawYCenter += (panelHeight))
    {
      // Only bother to draw it if it is onscreen.
      if( (drawYCenter > -panelHeight/2) && ( drawYCenter < SpriteOptions.getScreenDimensions().getHeight()+(panelHeight/2) ) )
      {
        PlayerSetupInfo playerInfo = control.getPlayerInfo(i);
        Integer key = i;

        // Get the relevant PlayerPanel.
        if( !teamPanels.containsKey(key) ) teamPanels.put(key, new TeamPanel(playerInfo));
        TeamPanel panel = teamPanels.get(key);

        // Update the PlayerPanel and render it to an image.
        BufferedImage playerImage = panel.update(playerInfo);

        int drawX = arrowheadPx * 2;
        int drawY = drawYCenter - playerImage.getHeight()/2;
        myG.drawImage(playerImage, drawX, drawY, playerImage.getWidth(), playerImage.getHeight(), null);
      }
    }

    /////////////////// MiniMap ////////////////////////
    // Figure out how large to draw the minimap. We want to make it as large as possible, but still
    //   fit inside the available space (with a minimum scale factor of 1).
    int maxMiniMapWidth = myWidth - playerZoneWidth - 4; // Subtract 4 so we have room to draw a frame.
    int maxMiniMapHeight = myHeight - 4;

    Color[] teamColors = new Color[mapInfo.getNumCos()];
    for( int i = 0; i < mapInfo.getNumCos(); ++i )
    {
      teamColors[i] = control.getPlayerInfo(i).getCurrentColor();
    }
    BufferedImage miniMap = MiniMapArtist.getMapImage( mapInfo, teamColors, drawScale*maxMiniMapWidth, drawScale*maxMiniMapHeight );

    int mmWScale = drawScale*maxMiniMapWidth / miniMap.getWidth();
    int mmHScale = drawScale*maxMiniMapHeight / miniMap.getHeight();
    int mmScale = (mmWScale > mmHScale)? mmHScale : mmWScale;

    // Draw a frame for the minimap.
    int mapWidth = miniMap.getWidth() * mmScale;
    int mapHeight = miniMap.getHeight() * mmScale;
    int mapLeft = drawScale*playerZoneWidth + (drawScale*maxMiniMapWidth - mapWidth)/2;
    int mapTop = (drawScale*myHeight / 2) - (mapHeight / 2);
    g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    g.fillRect(mapLeft-(drawScale*2), mapTop-(drawScale*2), mapWidth+(drawScale*4), mapHeight+(drawScale*4));
    g.setColor(SpriteUIUtils.MENUBGCOLOR);
    g.fillRect(mapLeft-drawScale*1, mapTop-drawScale*1, mapWidth+drawScale*2, mapHeight+drawScale*2);

    // Draw the mini map.
    g.drawImage(miniMap, mapLeft, mapTop, mapWidth, mapHeight, null);
    drawPlayerPropertyHighlights(g, mapLeft, mapTop, mapInfo, mmScale * miniMap.getWidth()/mapInfo.getWidth(), highlightedPlayer, (int) (Math.abs(target - player0YCenter.get())/4));

    // Render the final composed image to the window.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
  }

  /**
   * Renders itself into an image like this, with no scaling applied.
   * +----------------+---------+
   * |                |  Team   |
   * |                |---------+
   * |      CO        |         |
   * |                |    #    |
   * |                |         |
   * +----------------+---------+
   */
  private static class TeamPanel
  {
    // A couple of helper quantities.
    private static int textBuffer = 2;
    private static int portraitPx = SpriteLibrary.getCommanderSprites( "STRONG" ).head.getHeight(); // Faces are square.

    // Total horizontal panel space, sans scaling.
    public static final int PANEL_WIDTH = 2 + portraitPx + 2 + "TEAM".length()*SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth() + textBuffer*2 + 1 + 4;

    // Total vertical panel space, sans scaling.
    public static final int PANEL_HEIGHT = portraitPx + 4; // Face plus 2 on top and bottom.

    // The composed TeamPanel image.
    private BufferedImage myImage;

    // Each frame that makes up the larger panel.
    private SpriteUIUtils.ImageFrame commanderFrame;
    private SpriteUIUtils.ImageFrame teamLabel;
    private SpriteUIUtils.ImageFrame teamFrame;

    // Last known values.
    private int teamNumber = -99;
    private String colorName;

    public TeamPanel(PlayerSetupInfo info)
    {
      myImage = SpriteLibrary.createDefaultBlankSprite( PANEL_WIDTH, PANEL_HEIGHT );
      Graphics g = myImage.getGraphics();
      g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
      g.fillRect(0, 0, myImage.getWidth(), myImage.getHeight());

      commanderFrame = new SpriteUIUtils.ImageFrame(1, 1, portraitPx + 2, portraitPx + 2, info.getCurrentColor(),
          info.getCurrentColor(), true, SpriteLibrary.getCommanderSprites( info.getCurrentCO().name ).head);
      teamLabel = new SpriteUIUtils.ImageFrame(commanderFrame.width+2, 1, 28, 10, SpriteUIUtils.MENUHIGHLIGHTCOLOR, SpriteUIUtils.MENUBGCOLOR, false, SpriteUIUtils.getTextAsImage("TEAM", true));
      commanderFrame.render(g);
      teamLabel.render(g);

      update(info);
    }

    public BufferedImage update(PlayerSetupInfo info)
    {
      Graphics g = myImage.getGraphics();
      if( teamNumber != info.getCurrentTeam() )
      {
        teamFrame = new SpriteUIUtils.ImageFrame(commanderFrame.width+2, teamLabel.height+2, 28, 23, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUHIGHLIGHTCOLOR, true,
            SpriteUIUtils.getNumberAsImage(info.getCurrentTeam()));
        teamFrame.render(g);
      }
      if( !UIUtils.getPaletteName(info.getCurrentColor()).equals(colorName) )
      {
        colorName = UIUtils.getPaletteName(info.getCurrentColor());
        commanderFrame = new SpriteUIUtils.ImageFrame(1, 1, portraitPx + 2, portraitPx + 2, info.getCurrentColor(),
            info.getCurrentColor(), true, SpriteLibrary.getCommanderSprites( info.getCurrentCO().name ).head);
        commanderFrame.render(g);
      }
      return myImage;
    }
  }

  /**
   * Draws little white boxes around all the currently-highlighted player's properties.
   * @param distance - defines how far the boxes should be from the property edge (serves to highlight the new properties when switching between players)
   */
  private static void drawPlayerPropertyHighlights(Graphics g, int baseX, int baseY, MapInfo mapInfo, int scale, int faction, int distance)
  {
    g.setColor(Color.WHITE);
    XYCoord[] ownedCoords = mapInfo.COProperties[faction];
    for( XYCoord coord : ownedCoords )
    {
      int x = baseX + coord.xCoord * scale - distance, y = baseY + coord.yCoord * scale - distance;
      int s = scale + distance * 2;
      g.drawRect(x, y, s, s);
    }
  }
}
