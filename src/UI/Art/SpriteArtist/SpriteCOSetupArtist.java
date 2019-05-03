package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.CommanderStrong;
import Engine.XYCoord;
import Terrain.MapInfo;
import UI.COSetupController;
import UI.COSetupInfo;
import UI.COSetupInfo.OptionList;
import UI.UIUtils;

public class SpriteCOSetupArtist
{
  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);
  
  // how long we think color, faction, and AI name can reasonably get
  private static final int EXPECTED_TEXT_LENGTH = 8;
  private static final int CO_ELEM_BUFFER = 6;

  private static double animHighlightedPlayer = 0;

//  private static long lastDrawTime = 0;

  private static COSetupController myControl = null;

  /**
   * Draw a thing like this, for each player:
   * +------------------+ +---------+ +---------+
   * |                  | |  Color  | | Faction |
   * |                  | +---------+ +---------+
   * |        CO        | +---------+ +---------+
   * |                  | |  Team   | | AI Name |
   * |                  | +---------+ +---------+
   * +------------------+           CO
   */
  public static void draw(Graphics g, MapInfo mapInfo, COSetupController control)
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

    /////////////////// MiniMap ////////////////////////
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage miniMap = SpriteMiniMapArtist.getMapImage( mapInfo );

    // Figure out how large to draw the minimap. Make it as large as possible, allowing
    // space for the CO selection area below.
    int COSelectionAreaHeight = SpriteLibrary.getCommanderSprites(CommanderStrong.getInfo().name).head.getHeight()*2*drawScale;
    int minimapAreaHeight = dimensions.height - COSelectionAreaHeight;

    // Figure out how large to draw the minimap. We want to make it as large as possible, but still
    //   fit inside the available space (with a minimum scale factor of 1).
    int maxMiniMapWidth = dimensions.width - 4; // Subtract 4 so we have room to draw a frame.
    int maxMiniMapHeight = dimensions.height - COSelectionAreaHeight - 4;
    int mmWScale = maxMiniMapWidth / miniMap.getWidth();
    int mmHScale = maxMiniMapHeight / miniMap.getHeight();
    int mmScale = (mmWScale > mmHScale)? mmHScale : mmWScale;
    if( mmScale > 10*drawScale) mmScale = 10*drawScale;

    // Draw a frame for the minimap.
    int mapWidth = miniMap.getWidth() * mmScale;
    int mapHeight = miniMap.getHeight() * mmScale;
    int mapLeft = (dimensions.width / 2) - (mapWidth / 2);
    int mapTop = (minimapAreaHeight / 2) - (mapHeight / 2);
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(mapLeft-(2*drawScale), mapTop-(2*drawScale), mapWidth+(4*drawScale), mapHeight+(4*drawScale));
    g.setColor(MENUBGCOLOR);
    g.fillRect(mapLeft-drawScale, mapTop-drawScale, mapWidth+(2*drawScale), mapHeight+(2*drawScale));

    // Draw the mini map.
    SpriteLibrary.drawImageCenteredOnPoint(g, miniMap, dimensions.width / 2, minimapAreaHeight / 2, mmScale);

    // Define the space to draw the list of player CO portraits.
    int drawYCenter = ((dimensions.height - minimapAreaHeight) / 2) + minimapAreaHeight;
    int coHighlightedPortraitXCenter = dimensions.width / 2; // Whichever player has focus should be centered.

    /////////////////// CO Portraits ///////////////////////
    int numCOs = mapInfo.getNumCos();
    int highlightedPlayer = myControl.getHighlightedPlayer();

    // Allocate a static amount of horizontal space for each player in the map.
    // Possible TODO: Adjust the allocated space for each player based on how long their selected options actually are. 
    int xSpacing = (int)(
        SpriteLibrary.getCommanderSprites(CommanderStrong.getInfo().name).head.getWidth() + 
        SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth()*EXPECTED_TEXT_LENGTH*2 +
        CO_ELEM_BUFFER*3)*drawScale;

    // If we are moving from one option to another, calculate the intermediate draw location.
    if( animHighlightedPlayer != highlightedPlayer )
    {
      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedPlayer, highlightedPlayer);
      animHighlightedPlayer += slide;
    }

    // Find where the zeroth player CO should be drawn.
    // Shift from the center location by the spacing times the number of the highlighted option.
    int drawXCenter = (int)(coHighlightedPortraitXCenter - (animHighlightedPlayer * xSpacing)); 

    for(int i = 0; i < numCOs; ++i, drawXCenter += xSpacing)
    {
      // Draw all the info for the player in question, and the arrows if it's the currently highlighted player
      drawCoInfo(g, myControl.getPlayerInfo(i), drawXCenter, drawYCenter, i == highlightedPlayer);
    }
  }

  private static void drawSelectorArrows(Graphics g, int xCenter, int yCenter, int yBuffer)
  {
    int drawScale = SpriteOptions.getDrawScale();
    // Polygons for arrows to indicate the focused player slot. Add two pixels to the vertical buffer
    // for the frame border, plus two pixels between the portrait frame and the arrows.
    int[] upXPoints = {xCenter-(4*drawScale), xCenter, xCenter+drawScale, xCenter+(5*drawScale)};
    int[] upYPoints = {yCenter-(yBuffer*drawScale), yCenter-((yBuffer+4)*drawScale), yCenter-((yBuffer+4)*drawScale), yCenter-(yBuffer*drawScale)};
    int[] dnXPoints = {xCenter-(4*drawScale), xCenter, xCenter+drawScale, xCenter+(5*drawScale)};
    int[] dnYPoints = {yCenter+(yBuffer*drawScale), yCenter+((yBuffer+4)*drawScale), yCenter+((yBuffer+4)*drawScale), yCenter+(yBuffer*drawScale)};

    // Draw the arrows around the focused player slot.
    g.setColor(MENUFRAMECOLOR);
    g.fillPolygon(upXPoints, upYPoints, upXPoints.length);
    g.fillPolygon(dnXPoints, dnYPoints, dnXPoints.length);
  }

  private static void drawCoInfo(Graphics g, COSetupInfo info, int xCenter, int yCenter, boolean drawArrows)
  {
    // Fetch CO portrait
    BufferedImage portrait = SpriteLibrary.getCommanderSprites( info.getCurrentCO().name ).head;
    int drawScale = SpriteOptions.getDrawScale();

    int drawX = xCenter - ( (portrait.getWidth()*drawScale) / 2 );
    int drawY = yCenter - ( (portrait.getHeight()*drawScale) / 2);
    int drawW = portrait.getWidth() * drawScale;
    int drawH = portrait.getHeight() * drawScale;

    // Only bother to draw it if it is onscreen.
    if( (drawX+drawW > 0) && ( drawX < SpriteOptions.getScreenDimensions().getWidth() ) )
    {
      // Draw frame box
      g.setColor( MENUFRAMECOLOR );
      g.fillRect(drawX - (2*drawScale), drawY - (2*drawScale), drawW + (4*drawScale), drawH + (4*drawScale));

      // Draw team color box, nested inside the frame box.
      Color c = info.getCurrentColor();
      g.setColor( c );
      g.fillRect(drawX - drawScale, drawY - drawScale, drawW + (2*drawScale), drawH + (2*drawScale));

      // draw the CO's color selection
      BufferedImage colorFrame = SpriteUIUtils.makeTextFrame(c, c.darker(), UIUtils.getColorName(c), 2*drawScale, 2*drawScale);
      XYCoord colorOffset = getChoiceOffset(OptionList.COLOR, drawW/2, drawH/2, drawScale);
      SpriteLibrary.drawImageCenteredOnPoint(g, colorFrame, xCenter+colorOffset.xCoord, yCenter+colorOffset.yCoord, 1);

      // draw the CO's faction selection
      BufferedImage factionFrame = SpriteUIUtils.makeTextFrame(c, c.darker(), info.getCurrentFaction().name, 2*drawScale, 2*drawScale);
      XYCoord factionOffset = getChoiceOffset(OptionList.FACTION, drawW/2, drawH/2, drawScale);
      SpriteLibrary.drawImageCenteredOnPoint(g, factionFrame, xCenter+factionOffset.xCoord, yCenter+factionOffset.yCoord, 1);
      
      // draw the team selection
      String team = (info.getCurrentTeam() > -1)? Integer.toString(info.getCurrentTeam()+1) : "N/A"; // convert to human-readable teams 
      BufferedImage teamFrame = SpriteUIUtils.makeTextFrame(c, c.darker(), team, 3*drawScale, 2*drawScale);
      XYCoord teamOffset = getChoiceOffset(OptionList.TEAM, drawW/2, drawH/2, drawScale);
      SpriteLibrary.drawImageCenteredOnPoint(g, teamFrame, xCenter+teamOffset.xCoord, yCenter+teamOffset.yCoord, 1);
      
      // draw the AI selection
      String AI = info.getCurrentAI().getName(); 
      BufferedImage AIFrame = SpriteUIUtils.makeTextFrame(c, c.darker(), AI, 3*drawScale, 2*drawScale);
      XYCoord AIOffset = getChoiceOffset(OptionList.AI, drawW/2, drawH/2, drawScale);
      SpriteLibrary.drawImageCenteredOnPoint(g, AIFrame, xCenter+AIOffset.xCoord, yCenter+AIOffset.yCoord, 1);

      // Draw CO Portrait
      SpriteLibrary.drawImageCenteredOnPoint(g, portrait, xCenter, yCenter, drawScale);
      
      // draw the CO name
      BufferedImage nameFrame = SpriteUIUtils.makeTextFrame(c, c.darker(), info.getCurrentCO().name, 2*drawScale, 2*drawScale);
      SpriteLibrary.drawImageCenteredOnPoint(g, nameFrame,
          xCenter+drawW/2 + (CO_ELEM_BUFFER*3/2 + SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth()*EXPECTED_TEXT_LENGTH)*drawScale,
          yCenter+drawH/2 + 3*drawScale, 1);

      if (drawArrows)
      {
        XYCoord arrowOffset = getChoiceOffset(OptionList.values()[info.getSelectionNormalized()], drawW/2, drawH/2, drawScale);
        int yBuffer;
        if( OptionList.COMMANDER == OptionList.values()[info.getSelectionNormalized()] )
          yBuffer = SpriteLibrary.getCommanderSprites(CommanderStrong.getInfo().name).head.getHeight() / 2 + 4;
        else
          yBuffer = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight() / 2 + 3;

        drawSelectorArrows(g, xCenter + arrowOffset.xCoord, yCenter + arrowOffset.yCoord, yBuffer);
      }
    }
  }
  
  /**
   * @return the x,y offset of the center of the currently-selected option
   */
  private static XYCoord getChoiceOffset(OptionList option, int imageXShift, int imageYShift, int drawScale)
  {
    int x,y;
    int textWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
    int textHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();

    switch (option)
    {
      case COLOR:
        x = imageXShift + (CO_ELEM_BUFFER + textWidth*EXPECTED_TEXT_LENGTH/2)*drawScale;
        y = -imageYShift + textHeight*drawScale/2;
        break;
      case FACTION:
        x = imageXShift + (CO_ELEM_BUFFER*2 + textWidth*3*EXPECTED_TEXT_LENGTH/2)*drawScale;
        y = -imageYShift + textHeight*drawScale/2;
        break;
      case TEAM:
        x = imageXShift + (CO_ELEM_BUFFER + textWidth*EXPECTED_TEXT_LENGTH/2)*drawScale;
        y = textHeight*drawScale/2;
        break;
      case AI:
        x = imageXShift + (CO_ELEM_BUFFER*2 + textWidth*3*EXPECTED_TEXT_LENGTH/2)*drawScale;
        y = textHeight*drawScale/2;
        break;
      case COMMANDER: // fall-through
      default:
        x = 0;
        y = 0;
        break;
    }
    
    return new XYCoord(x,y);
  }
}
