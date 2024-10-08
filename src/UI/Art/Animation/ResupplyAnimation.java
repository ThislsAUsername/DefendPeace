package UI.Art.Animation;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteUIUtils;
import UI.Art.SpriteArtist.UnitSpriteSet;
import Units.Unit;

public class ResupplyAnimation extends BaseUnitActionAnimation
{
  private boolean firstCall = true;
  private boolean isCancelled = false;
  private long startTime = 0;
  private String SUPPLYTEXT = "SUPPLY";
  private int signWidth = 0;
  private int signHeight = 0;

  public ResupplyAnimation(int tileSize, Unit supplier, int mapX, int mapY)
  {
    this(tileSize, supplier, new XYCoord(mapX, mapY));
  }

  public ResupplyAnimation(int tileSize, Unit supplier, XYCoord actorCoord)
  {
    super(tileSize, supplier, actorCoord);
    int menuTextWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
    int menuTextHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();

    /**
     * We are gonna draw this over the unit:
     *   ------------
     *   | RESUPPLY |
     *   ------------
     * But with cool pop up/pop down effects.
     */
    signWidth = ((menuTextWidth * SUPPLYTEXT.length()));
    signHeight = (menuTextHeight);
  }

  @Override
  public boolean animate(Graphics g)
  {
    if( isCancelled || actorCoord.x < 0 || actorCoord.y < 0 )
    {
      return true;
    }

    // The first time we are called, record the time so we know when to show each frame.
    if( firstCall )
    {
      startTime = System.currentTimeMillis();
      firstCall = false;
    }

    if( null != actor )
      drawUnit(g, actor, UnitSpriteSet.AnimState.IDLE, actor.x, actor.y );

    long animTime = System.currentTimeMillis() - startTime;

    // The sign begins to appear at 0, is visible from signUpBegin to signUpEnd, and should be gone by signGone.
    final long signUpBegin = 100;
    final long signUpEnd = 550;
    final long signGone = 600;

    int tileSize = SpriteLibrary.baseSpriteSize;
    int tileCenterX = (actorCoord.x * tileSize) + (tileSize / 2);
    int tileCenterY = (actorCoord.y * tileSize) + (tileSize / 2);

    // Show the menu expanding from nothing, then disappearing
    BufferedImage menu = SpriteLibrary.createTransparentSprite(tileSize, tileSize);
    if( animTime < signUpBegin )
    {
      // The sign is popping into existence.
      double percent = (double) animTime / signUpBegin;
      int width = (int) (signWidth * percent);
      int height = (int) (signHeight * percent);

      menu = SpriteUIUtils.makeTextFrame(SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR, width / 2, height / 2);
    }
    else if( animTime < signUpEnd )
    {
      // The sign is legible.
      menu = SpriteUIUtils.makeTextFrame(SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR,
          SUPPLYTEXT, 2, 2);
    }
    else if( animTime < signGone )
    {
      // The sign is dropping back out of existence.
      double percent = 1 - ((double) (animTime - signUpEnd) / (signGone - signUpEnd));
      int width = (int) (signWidth * percent);
      int height = (int) (signHeight * percent);

      menu = SpriteUIUtils.makeTextFrame(SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR, width / 2, height / 2);
    }
    SpriteUIUtils.drawImageCenteredOnPoint(g, menu, tileCenterX, tileCenterY);

    return animTime > signGone;
  }

  @Override
  public void cancel()
  {
    isCancelled = true;
  }
}
