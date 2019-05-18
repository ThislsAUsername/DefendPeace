package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Commander;
import Engine.Driver;
import Engine.GameInstance;
import Engine.Utils;
import Engine.GameEvents.GameEventQueue;
import UI.COStateInfo;
import UI.InfoController;
import UI.MapView;

/**
 * InfoView paints the CO's full portrait and a box with info in it.
 * The contents of the info are defined by inputs; current game status or static CO info are both used
 */
public class SpriteInfoView extends MapView // Extend MapView for getDrawableMap(). We don't actually draw it, but we need fog info.
{
  private InfoController myControl;
  
  /** Width of the visible space in pixels. */
  private int viewWidth;
  /** Height of the visible space in pixels. */
  private int viewHeight;

  public SpriteInfoView(InfoController control)
  {
    Dimension dims = Driver.getInstance().gameGraphics.getScreenDimensions();
    viewWidth = dims.width;
    viewHeight = dims.height;
    myControl = control;
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return new Dimension(viewWidth, viewHeight);
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    viewWidth = width;
    viewHeight = height;
    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(viewWidth, viewHeight);
  }

  @Override
  public void render(Graphics g)
  {
    DiagonalBlindsBG.draw(g);

    // Get the CO info menu.
    int drawScale = SpriteOptions.getDrawScale();
    
    int paneOuterBuffer = 4*drawScale; // sets both outer border and frame border size
    int paneHSize = (int) (viewWidth*0.7) - paneOuterBuffer*4; // width of the BG color area
    int paneVSize = (int) (viewHeight   ) - paneOuterBuffer*4; // height ""

    // Get the current menu selections.
    Commander thisCO = myControl.getSelectedCO();
    InfoPage page = myControl.getSelectedPage();

    // Draw the commander art.
    BufferedImage COPic = SpriteLibrary.getCommanderSprites(myControl.getSelectedCOInfo().name).body;
    // justify bottom/right
    g.drawImage(COPic, viewWidth - COPic.getWidth()*drawScale, viewHeight - COPic.getHeight()*drawScale,
        COPic.getWidth()*drawScale, COPic.getHeight()*drawScale, null);

    if (null != thisCO)
      CommanderOverlayArtist.drawCommanderOverlay(g, thisCO, false);
    
    // add the actual info
    g.setColor(SpriteUIUtils.MENUFRAMECOLOR); // outer buffer
    g.fillRect(  paneOuterBuffer,   paneOuterBuffer, paneHSize + 2*paneOuterBuffer, paneVSize + 2*paneOuterBuffer);
    g.setColor(SpriteUIUtils.MENUBGCOLOR);    // inside of the pane
    g.fillRect(2*paneOuterBuffer, 2*paneOuterBuffer, paneHSize                    , paneVSize                    );
    
    // TODO: consider drawing all pages as one big image, so the user can scroll smoothly through it regardless of screen size
    int drawingWidth = paneHSize - paneOuterBuffer;
    switch (page.pageType)
    {
      case CO_HEADERS:
        int overlayHeight = 42*drawScale;
        int heightOffset = 0;
        for (Commander CO : myControl.getGame().commanders)
        {
          // Build our overlay
          BufferedImage overlayPic = SpriteLibrary.createTransparentSprite(drawingWidth, overlayHeight);
          CommanderOverlayArtist.drawCommanderOverlay(overlayPic.getGraphics(), CO, true);

          // Add brief status text per CO
          String status = new COStateInfo(getDrawableMap(myControl.getGame()), CO).getAbbrevStatus();
          BufferedImage statusText = SpriteUIUtils.drawTextToWidth(status, drawingWidth);
          overlayPic.getGraphics().drawImage(statusText, 0, drawScale * (5+SpriteLibrary.getCoOverlay(CO, true).getHeight()), null);

          // Drop the overlay where it's supposed to go
          g.drawImage(overlayPic,2*paneOuterBuffer, 2*paneOuterBuffer + heightOffset, null);
          heightOffset += overlayHeight;
        }
        break;
      case GAME_STATUS:
        if( null != thisCO )
        {
          String status = new COStateInfo(getDrawableMap(myControl.getGame()), thisCO).getFullStatus();
          BufferedImage statusText = SpriteUIUtils.drawTextToWidth(status, drawingWidth);
          g.drawImage(statusText, 3 * paneOuterBuffer, 3 * paneOuterBuffer, null);
        }
        break;
      case BASIC:
        BufferedImage infoText = SpriteUIUtils.drawTextToWidth(page.info, drawingWidth);
        g.drawImage(infoText,3*paneOuterBuffer, 3*paneOuterBuffer, null);
        break;
    }
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
