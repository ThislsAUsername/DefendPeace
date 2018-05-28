package UI.Art.Animation;

import java.awt.Color;
import java.awt.Graphics;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteOptions;

public class ResupplyAnimation implements GameAnimation
{
  private boolean firstCall = true;
  private boolean isCancelled = false;
  private long startTime = 0;
  private String SUPPLYTEXT = "SUPPLY";
  private XYCoord mapLocation = null;
  private int signWidth = 0;
  private int signHeight = 0;

  private final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private final Color MENUBGCOLOR = new Color(234, 204, 154);
  private final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  public ResupplyAnimation(XYCoord mapLocation)
  {
    this(mapLocation.xCoord, mapLocation.yCoord);
  }

  public ResupplyAnimation(int mapX, int mapY)
  {
    mapLocation = new XYCoord(mapX, mapY);
    int drawScale = SpriteOptions.getDrawScale();
    int menuTextWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
    int menuTextHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();

    /**
     * We are gonna draw this over the unit:
     *   ------------
     *   | RESUPPLY |
     *   ------------
     * But with cool pop up/pop down effects.
     */
    signWidth = ((menuTextWidth * SUPPLYTEXT.length()) + 4) * drawScale;
    signHeight = (menuTextHeight + 4) * drawScale;
  }

  private void drawSign(Graphics g, int x, int y, int w, int h)
  {
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(x, y, w, h);
    g.setColor(MENUHIGHLIGHTCOLOR);
    g.fillRect(x + 1, y + 1, w - 1, h - 1);
    g.setColor(MENUBGCOLOR);
    g.fillRect(x + 1, y + 1, w - 2, h - 2);
  }

  @Override
  public boolean animate(Graphics g)
  {
    if( isCancelled )
    {
      return true;
    }

    // The first time we are called, record the time so we know when to show each frame.
    if( firstCall )
    {
      startTime = System.currentTimeMillis();
      firstCall = false;
    }

    long animTime = System.currentTimeMillis() - startTime;

    // The sign begins to appear at 0, is visible from signUpBegin to signUpEnd, and should be gone by signGone.
    final long signUpBegin = 100;
    final long signUpEnd = 550;
    final long signGone = 600;

    int drawScale = SpriteOptions.getDrawScale();
    int tileSize = SpriteLibrary.baseSpriteSize * drawScale;
    int tileCenterX = (mapLocation.xCoord * tileSize) + (tileSize / 2);
    int tileCenterY = (mapLocation.yCoord * tileSize) + (tileSize / 2);

    // Show the menu expanding from nothing, then disappearing
    if( animTime < signUpBegin )
    {
      // The sign is popping into existence.
      double percent = (double) animTime / signUpBegin;
      int width = (int) (signWidth * percent);
      int height = (int) (signHeight * percent);
      XYCoord signTopLeft = new XYCoord(tileCenterX - width / 2, tileCenterY - height / 2);

      drawSign(g, signTopLeft.xCoord, signTopLeft.yCoord, width, height);

    }
    else if( animTime < signUpEnd )
    {
      // The sign is legible.
      XYCoord signTopLeft = new XYCoord(tileCenterX - signWidth / 2, tileCenterY - signHeight / 2);

      drawSign(g, signTopLeft.xCoord, signTopLeft.yCoord, signWidth, signHeight);
      SpriteLibrary.drawTextSmallCaps(g, SUPPLYTEXT, signTopLeft.xCoord + 2 * drawScale, signTopLeft.yCoord + 2 * drawScale,
          drawScale);
    }
    else if( animTime < signGone )
    {
      // The sign is dropping back out of existence.
      double percent = 1 - ((double) (animTime - signUpEnd) / (signGone - signUpEnd));
      int width = (int) (signWidth * percent);
      int height = (int) (signHeight * percent);
      XYCoord signTopLeft = new XYCoord(tileCenterX - width / 2, tileCenterY - height / 2);

      drawSign(g, signTopLeft.xCoord, signTopLeft.yCoord, width, height);
    }

    return animTime > signGone;
  }

  @Override
  public void cancel()
  {
    isCancelled = true;
  }
}
