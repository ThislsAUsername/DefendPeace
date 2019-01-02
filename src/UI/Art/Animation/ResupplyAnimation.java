package UI.Art.Animation;

import java.awt.Color;
import java.awt.Graphics;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteOptions;
import UI.Art.SpriteArtist.SpriteUIUtils;

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
    signWidth = ((menuTextWidth * SUPPLYTEXT.length())) * drawScale;
    signHeight = (menuTextHeight) * drawScale;
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

    // Show the menu expanding from nothing, then disappearing
    if( animTime < signUpBegin )
    {
      // The sign is popping into existence.
      double percent = (double) animTime / signUpBegin;
      int width = (int) (signWidth * percent);
      int height = (int) (signHeight * percent);

      SpriteUIUtils.drawTextFrame(g, MENUBGCOLOR, MENUFRAMECOLOR, mapLocation.xCoord, mapLocation.yCoord, width / 2,
        height / 2);
    }
    else if( animTime < signUpEnd )
    {
      // The sign is legible.
      SpriteUIUtils.drawTextFrame(g, MENUBGCOLOR, MENUFRAMECOLOR, SUPPLYTEXT, mapLocation.xCoord, mapLocation.yCoord,
          2 * drawScale, 2 * drawScale);
    }
    else if( animTime < signGone )
    {
      // The sign is dropping back out of existence.
      double percent = 1 - ((double) (animTime - signUpEnd) / (signGone - signUpEnd));
      int width = (int) (signWidth * percent);
      int height = (int) (signHeight * percent);

      SpriteUIUtils.drawTextFrame(g, MENUBGCOLOR, MENUFRAMECOLOR, mapLocation.xCoord, mapLocation.yCoord, width / 2,
          height / 2);
    }

    return animTime > signGone;
  }

  @Override
  public void cancel()
  {
    isCancelled = true;
  }
}
