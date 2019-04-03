package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Queue;

import CommandingOfficers.COMaker.InfoPage;
import CommandingOfficers.Commander;
import Engine.Driver;
import Engine.GameInstance;
import Engine.IView;
import Engine.Path;
import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import UI.CO_InfoMenu;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import UI.Art.Animation.NoAnimation;
import UI.Art.Animation.NobunagaBattleAnimation;
import UI.Art.Animation.ResupplyAnimation;
import Units.Unit;

public class SpriteInfoView extends MapView // extend MapView for getDrawableMap()
{
  private CO_InfoMenu myControl;
  
  /** Width of the visible space in pixels. */
  private int mapViewWidth;
  /** Height of the visible space in pixels. */
  private int mapViewHeight;

  boolean dimensionsChanged = false; // If the window is resized, don't bother sliding the view into place; just snap.

  public SpriteInfoView(CO_InfoMenu control)
  {
    Dimension dims = Driver.getInstance().gameGraphics.getScreenDimensions();
    mapViewWidth = dims.width;
    mapViewHeight = dims.height;
    myControl = control;
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return new Dimension(mapViewWidth, mapViewHeight);
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    // The user wants to use a specific amount of screen. Figure out how many tiles to draw for them.
    mapViewWidth = width;
    mapViewHeight = height;
    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(mapViewWidth, mapViewHeight);

    dimensionsChanged = true; // Let render() know that the window was resized.
  }

  @Override
  public void render(Graphics g)
  {
    DiagonalBlindsBG.draw(g);

    // Get the CO info menu.
    int drawScale = SpriteOptions.getDrawScale();
    
    int paneOuterBuffer = 4*drawScale; // sets both outer border and frame border size
    int paneHSize = (int) (mapViewWidth*0.7) - paneOuterBuffer*4; // width of the BG color area
    int paneVSize = (int) (mapViewHeight   ) - paneOuterBuffer*4; // height ""

    // Get the current menu selections.
    Commander co = myControl.getSelectedCO();
    InfoPage page = myControl.getPageSelection();

    // Draw the commander art. (the caller draws our background, so we don't have to)
    BufferedImage COPic = SpriteLibrary.getCommanderSprites(co.coInfo.name).body;
    // justify bottom/right
    g.drawImage(COPic, mapViewWidth - COPic.getWidth()*drawScale, mapViewHeight - COPic.getHeight()*drawScale,
        COPic.getWidth()*drawScale, COPic.getHeight()*drawScale, null);

    CommanderOverlayArtist.drawCommanderOverlay(g, co, false);
    
    // add the actual info
    g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    g.fillRect(  paneOuterBuffer,   paneOuterBuffer, paneHSize + 2*paneOuterBuffer, paneVSize + 2*paneOuterBuffer);
    g.setColor(SpriteUIUtils.MENUBGCOLOR);
    g.fillRect(2*paneOuterBuffer, 2*paneOuterBuffer, paneHSize                    , paneVSize                    );
    
    // TODO: consider drawing this all as one big image, so the user can scroll smoothly through it regardless of screen size
    switch (page.pageType)
    {
      case CO_HEADERS:
        int overlayHeight = 30*drawScale;
        int heightOffset = 0;
        for (Commander CO : myControl.getGame().commanders)
        {
          BufferedImage overlayPic = SpriteLibrary.createTransparentSprite(100*drawScale, overlayHeight);
          CommanderOverlayArtist.drawCommanderOverlay(overlayPic.getGraphics(), CO, true);
          g.drawImage(overlayPic,2*paneOuterBuffer, 2*paneOuterBuffer + heightOffset, null);
          heightOffset += overlayHeight;
        }
        break;
      case GAME_STATUS:
        String status = Utils.getGameStatusData(getDrawableMap(myControl.getGame()), co);
        BufferedImage statusText = SpriteUIUtils.paintTextNormalized(status, paneHSize-paneOuterBuffer);
        g.drawImage(statusText,3*paneOuterBuffer, 3*paneOuterBuffer, null);
        break;
      case BASIC:
        BufferedImage infoText = SpriteUIUtils.paintTextNormalized(page.info, paneHSize-paneOuterBuffer);
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
}
