package UI.Art.Animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.Commander;
import UI.SlidingValue;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteOptions;
import UI.Art.SpriteArtist.SpriteUIUtils;
import UI.Art.SpriteArtist.Backgrounds.HorizontalStreaksBG;

/**
 * Draws an overlay with the CO at the start of each turn.
 */
public class TurnInitAnimation extends GameAnimation
{
  Commander commander;
  int turn;

  BufferedImage bgImage;
  BufferedImage fgImage;
  Dimension dims;

  int slideDir;
  SlidingValue bgOffset;

  long ingressEndTime = -1;
  long holdTimeMs = 1500;
  boolean ending;

  public TurnInitAnimation(Commander cmdr, int turnNum)
  {
    super(false);
    commander = cmdr;
    turn = turnNum;
    int width = SpriteOptions.getScreenDimensions().width;
    slideDir = (Math.random() > 0.5) ? -1 : 1;
    bgOffset = new SlidingValue(width*slideDir);
    bgOffset.set(0);
    ending = false;
  }

  private void regenImages()
  {
    dims = new Dimension(SpriteOptions.getScreenDimensions());
    bgImage = SpriteLibrary.createDefaultBlankSprite(dims.width, dims.height);


    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage dayImg = SpriteUIUtils.getBoldTextAsImage("Turn " + turn);
    int buf = 3*drawScale;
    fgImage = SpriteLibrary.createTransparentSprite(dims.width, dayImg.getHeight()*drawScale + (buf*2));
    int xCenter = dims.width/2;
    Graphics fgg = fgImage.getGraphics();
    fgg.setColor(Color.BLACK);
    fgg.fillRect(0, 0, fgImage.getWidth(), drawScale);
    fgg.fillRect(0, fgImage.getHeight()-drawScale, fgImage.getWidth(), drawScale);
    fgg.setColor(new Color(255, 255, 255, 180));
    fgg.fillRect(0, drawScale, fgImage.getWidth(), fgImage.getHeight()-(2*drawScale));
    fgImage.getGraphics().drawImage(dayImg, xCenter-dayImg.getWidth(), buf,
        dayImg.getWidth()*drawScale, dayImg.getHeight()*drawScale, null);
  }

  @Override
  public boolean animate(Graphics g)
  {
    if( !SpriteOptions.getScreenDimensions().equals(dims) )
      regenImages();

    int fgx = (int)bgOffset.get();
    int bgx = fgx*-1;

    if( fgx == 0 && !ending )
    {
      isMapVisible = false;
      if( ingressEndTime == -1 )
        ingressEndTime = System.currentTimeMillis();
      else if( System.currentTimeMillis() - ingressEndTime > holdTimeMs )
        cancel(); // Trigger the outro.
    }

    // Redraw the bg effects image.
    HorizontalStreaksBG.draw(bgImage.getGraphics(), commander.myColor);

    g.drawImage(bgImage, bgx, 0, null);
    g.drawImage(fgImage, fgx, bgImage.getHeight()/2-fgImage.getHeight()/2, null);

    // Return true once the outro completes.
    boolean done = ending && !bgOffset.moving();
    System.out.println("bgo=" + bgOffset.get());
    System.out.println("reporting done=" + done);
    return done;
  }

  @Override
  public void cancel()
  {
    bgOffset.set(-SpriteOptions.getScreenDimensions().width*slideDir);
    ending = true;
    isMapVisible = true;
  }
}
