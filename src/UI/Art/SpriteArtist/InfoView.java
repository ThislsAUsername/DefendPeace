package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.CommanderInfo.InfoPage.PageType;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapPerspective;
import UI.COStateInfo;
import UI.InfoController;
import UI.MapView;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;

/**
 * InfoView paints the CO's full portrait and a box with info in it.
 * The contents of the info are defined by inputs; current game status or static CO info are both used
 */
public class InfoView extends MapView // Extend MapView for getDrawableMap(). We don't actually draw it, but we need fog info.
{
  private static final int PANE_OUTER_BUFFER = 3; // sets both outer border and frame border size

  private InfoController myControl;

  private ArrayList<InfoPage> prevPages = null;
  private BufferedImage pageImage = null;

  public InfoView(InfoController control)
  {
    myControl = control;
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return SpriteOptions.getScreenDimensions();
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    prevPages = null; // Have to redraw if we've been resized
    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(width, height);
  }

  @Override
  public void render(Graphics g)
  {
    DiagonalBlindsBG.draw(g);

    // Get the draw space. We'll draw it all in real size and then scale it when we draw to the window.
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int imageWidth = dimensions.width / drawScale;
    int imageHeight = dimensions.height / drawScale;
    if (1 > imageHeight || 1 > imageWidth)
      return;

    BufferedImage image = SpriteLibrary.createTransparentSprite(imageWidth, imageHeight);
    Graphics myG = image.getGraphics();

    int paneHSize = (int) (imageWidth*0.7) - PANE_OUTER_BUFFER*4; // width of the BG color area
    int paneVSize = (int) (imageHeight   ) - PANE_OUTER_BUFFER*4; // height ""

    // Get the current menu selections.
    Commander thisCO = myControl.getSelectedCO();
    ArrayList<InfoPage> pages = myControl.getSelectedPages();

    // Draw the commander art.
    BufferedImage COPic = SpriteLibrary.getCommanderSprites(myControl.getSelectedCOInfo().name).body;
    // justify bottom/right
    myG.drawImage(COPic, imageWidth - COPic.getWidth(), imageHeight - COPic.getHeight(), null);

    if (PANE_OUTER_BUFFER > paneHSize || PANE_OUTER_BUFFER > paneVSize)
      return; // Continue no further if we don't have room to actually draw

    if (null != thisCO)
      CommanderOverlayArtist.drawCommanderOverlay(myG, thisCO, false);
    
    // add the actual info
    myG.setColor(SpriteUIUtils.MENUFRAMECOLOR); // outer buffer
    myG.fillRect(  PANE_OUTER_BUFFER,   PANE_OUTER_BUFFER, paneHSize + 2*PANE_OUTER_BUFFER, paneVSize + 2*PANE_OUTER_BUFFER);
    myG.setColor(SpriteUIUtils.MENUBGCOLOR);    // inside of the pane
    myG.fillRect(2*PANE_OUTER_BUFFER, 2*PANE_OUTER_BUFFER, paneHSize                      , paneVSize                      );

    final int drawingWidth = paneHSize - PANE_OUTER_BUFFER*2;
    if( pages != prevPages )
      pageImage = renderPage(pages, drawingWidth);
    prevPages = pages;

    final int shiftDown = myControl.getShiftDown() * 2;
    final int possibleShift = Math.max(0, pageImage.getHeight() - paneVSize);
    int pixelShift = 0;
    if( possibleShift > 0 )
      pixelShift = (possibleShift + shiftDown % possibleShift) % possibleShift;

    final int contentWidth  = Math.min(Math.abs(        drawingWidth           ), pageImage.getWidth());
    final int contentHeight = Math.min(Math.abs(paneVSize - PANE_OUTER_BUFFER*2), pageImage.getHeight());
    // Draw the cropped page into our pane
    myG.drawImage(pageImage.getSubimage(0, pixelShift, contentWidth, contentHeight), 3 * PANE_OUTER_BUFFER, 3 * PANE_OUTER_BUFFER, null);

    // Finally, draw our rendered image onto the window.
    g.drawImage(image, 0, 0, imageWidth*drawScale, imageHeight*drawScale, null);
  }

  private BufferedImage renderPage(ArrayList<InfoPage> pages, int drawingWidth)
  {
    ArrayList<BufferedImage> pageImages = new ArrayList<BufferedImage>();
    for( InfoPage p : pages )
    {
      switch (p.pageType)
      {
        case CO_HEADERS:
        case GAME_STATUS: // Fall-through
          Commander[] coList = myControl.getGame().commanders;
          MapPerspective drawableMap = getDrawableMap(myControl.getGame());

          for( Commander thisCO : coList )
          {
            BufferedImage statusHeader =
                SpriteLibrary.createTransparentSprite(drawingWidth, CommanderOverlayArtist.OVERLAY_HEIGHT + 5);
            Graphics headerG = statusHeader.getGraphics();
            CommanderOverlayArtist.drawCommanderOverlay(headerG, thisCO, 0, true);

            // Add brief status text per CO
            BufferedImage statusText;
            if( p.pageType == PageType.GAME_STATUS )
            {
              COStateInfo coStatus = new COStateInfo(drawableMap, thisCO);
              BufferedImage[] statusHalves = new BufferedImage[2];
              // Draw assuming we don't have to wrap the text
              statusHalves[0] = SpriteUIUtils.drawProse(coStatus.getFullStatusLabels());
              statusHalves[1] = SpriteUIUtils.drawProse(coStatus.getFullStatusValues());
              statusText = SpriteUIUtils.joinBufferedImages(statusHalves, 3);
            }
            else
            {
              String status = new COStateInfo(drawableMap, thisCO).getAbbrevStatus();
              statusText = SpriteUIUtils.drawProseToWidth(status, drawingWidth);
            }

            pageImages.add(SpriteUIUtils.stackBufferedImages(new BufferedImage[] { statusHeader, statusText }, 5));
          }
          break;
        case BASIC:
          pageImages.add(SpriteUIUtils.drawProseToWidth(p.info, drawingWidth));
          break;
      }
    }

    return SpriteUIUtils.stackBufferedImages(pageImages.toArray(new BufferedImage[0]), 15);
  }

  // Vestigial method stubs from MapView
  @Override
  public void animate(GameEventQueue newEvents)
  {
  }
  @Override
  public void cleanup()
  {
  }
}
