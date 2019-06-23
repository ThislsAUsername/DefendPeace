package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameEvents.GameEventQueue;
import UI.COStateInfo;
import UI.InfoController;
import UI.MapView;

/**
 * InfoView paints the CO's full portrait and a box with info in it.
 * The contents of the info are defined by inputs; current game status or static CO info are both used
 */
public class InfoView extends MapView // Extend MapView for getDrawableMap(). We don't actually draw it, but we need fog info.
{
  private InfoController myControl;

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
    BufferedImage image = SpriteLibrary.createTransparentSprite(imageWidth, imageHeight);
    Graphics myG = image.getGraphics();
    
    int paneOuterBuffer = 4; // sets both outer border and frame border size
    int paneHSize = (int) (imageWidth*0.7) - paneOuterBuffer*4; // width of the BG color area
    int paneVSize = (int) (imageHeight   ) - paneOuterBuffer*4; // height ""

    // Get the current menu selections.
    Commander thisCO = myControl.getSelectedCO();
    InfoPage page = myControl.getSelectedPage();

    // Draw the commander art.
    BufferedImage COPic = SpriteLibrary.getCommanderSprites(myControl.getSelectedCOInfo().name).body;
    // justify bottom/right
    myG.drawImage(COPic, imageWidth - COPic.getWidth(), imageHeight - COPic.getHeight(), null);

    if (null != thisCO)
      CommanderOverlayArtist.drawCommanderOverlay(myG, thisCO, false);
    
    // add the actual info
    myG.setColor(SpriteUIUtils.MENUFRAMECOLOR); // outer buffer
    myG.fillRect(  paneOuterBuffer,   paneOuterBuffer, paneHSize + 2*paneOuterBuffer, paneVSize + 2*paneOuterBuffer);
    myG.setColor(SpriteUIUtils.MENUBGCOLOR);    // inside of the pane
    myG.fillRect(2*paneOuterBuffer, 2*paneOuterBuffer, paneHSize                    , paneVSize                    );
    
    // TODO: consider drawing all pages as one big image, so the user can scroll smoothly through it regardless of screen size
    int drawingWidth = paneHSize - paneOuterBuffer;
    switch (page.pageType)
    {
      case CO_HEADERS:
        int overlayHeight = 42;
        int heightOffset = 0;
        for (Commander CO : myControl.getGame().commanders)
        {
          // Build our overlay
          BufferedImage overlayPic = SpriteLibrary.createTransparentSprite(drawingWidth, overlayHeight);
          CommanderOverlayArtist.drawCommanderOverlay(overlayPic.getGraphics(), CO, true);

          // Add brief status text per CO
          String status = new COStateInfo(getDrawableMap(myControl.getGame()), CO).getAbbrevStatus();
          BufferedImage statusText = SpriteUIUtils.drawTextToWidth(status, drawingWidth);
          overlayPic.getGraphics().drawImage(statusText, 0, (5+SpriteLibrary.getCoOverlay(CO, true).getHeight()), null);

          // Drop the overlay where it's supposed to go
          myG.drawImage(overlayPic,2*paneOuterBuffer, 2*paneOuterBuffer + heightOffset, null);
          heightOffset += overlayHeight;
        }
        break;
      case GAME_STATUS:
        if( null != thisCO )
        {
          String status = new COStateInfo(getDrawableMap(myControl.getGame()), thisCO).getFullStatus();
          BufferedImage statusText = SpriteUIUtils.drawTextToWidth(status, drawingWidth);
          myG.drawImage(statusText, 3 * paneOuterBuffer, 3 * paneOuterBuffer, null);
        }
        break;
      case BASIC:
        BufferedImage infoText = SpriteUIUtils.drawTextToWidth(page.info, drawingWidth);
        myG.drawImage(infoText,3*paneOuterBuffer, 3*paneOuterBuffer, null);
        break;
    }

    // Finally, draw our rendered image onto the window.
    g.drawImage(image, 0, 0, imageWidth*drawScale, imageHeight*drawScale, null);
  }

  
  // Vestigial method stubs from MapView
  @Override
  public int getTileSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void animate(GameEventQueue newEvents)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void cleanup()
  {
    // TODO Auto-generated method stub
    
  }
}
