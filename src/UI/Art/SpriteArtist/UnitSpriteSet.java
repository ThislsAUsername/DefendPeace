package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import UI.UIUtils;
import Units.Unit;

public class UnitSpriteSet
{

  Sprite sprites[] = new Sprite[6];

  public final int ACTION_IDLE = 0;
  public final int ACTION_MOVENORTH = 1;
  public final int ACTION_MOVEEAST = 2;
  public final int ACTION_MOVESOUTH = 3;
  public final int ACTION_MOVEWEST = 4;
  public final int ACTION_DIE = 5;
  
  public final int ANIM_FRAMES_PER_MARK = 3; 

  Sprite turnDone;

  /**
   * UnitSpritSet constructor - parse the provided sprite sheet. Sprites are expected to 
   * be arranged in rows by animation type, in the following order:
   * idle
   * Move North
   * Move East
   * Move South
   * Move West
   * Death
   * 
   * @param spriteSheet The image containing the frames to be used in this sprite set.
   * @param width The width of each frame in the sprite.
   * @param height The height of each frame in the sprite.
   */
  public UnitSpriteSet(BufferedImage spriteSheet, int width, int height, ColorPalette coColors)
  {
    int h = 0;
    int action = ACTION_IDLE; // This variable relies on the ordering of the ACTION variables defined above.

    try
    {
      if( null != spriteSheet && spriteSheet.getWidth() >= width && spriteSheet.getHeight() >= height )
      {
        while (height <= spriteSheet.getHeight() && action <= ACTION_DIE)
        {
          Sprite spr = new Sprite(spriteSheet.getSubimage(0, h, spriteSheet.getWidth(), height), width, height);
          sprites[action] = spr;
          ++action;
        }
        if( action < ACTION_DIE )
        { // We didn't load all animations we wanted - our sprite set is incomplete. Default the rest to IDLE.
          sprites[action] = sprites[ACTION_IDLE];
          ++action;
        }
      }
      else
      { // No sprite sheet provided? Just make stuff up.
        System.out.println("WARNING! Continuing with placeholder images.");
        sprites[ACTION_IDLE] = new Sprite(null, width, height);
        for( int i = ACTION_IDLE + 1; i <= ACTION_DIE; ++i )
        {
          sprites[i] = sprites[ACTION_IDLE];
        }
        turnDone = sprites[ACTION_IDLE];
      }
    }
    catch (RasterFormatException rfe)
    {
      // Something went wrong. Just make something up and hope nobody notices.
      // Use the IDLE action if it exists, otherwise we are going with a black rectangle.
      Sprite defaultSprite = (action > ACTION_IDLE) ? sprites[ACTION_IDLE] : new Sprite(null, width, height);

      System.out.println("WARNING: RasterFormatException in UnitSpriteSet constructor.");
      System.out.println("WARNING:   Attempting to continue.");
      for( ; action <= ACTION_DIE; ++action )
      {
        sprites[action] = defaultSprite;
      }
    }

    colorize(UIUtils.defaultMapColors, coColors.paletteColors);
    if( action > 0 ) // We at least got the IDLE sprites. Use those as the basis for the "already moved" sprites.
    {
      turnDone = new Sprite(sprites[0]); // Duplicate the IDLE sprite to make the TurnDone sprite.

      // Get my color presets.
      Color tiredColor = new Color(128, 128, 128, 160);

      // Shade each frame so the unit can be grayed-out after moving.
      for( int f = 0; f < turnDone.numFrames(); ++f )
      {
        BufferedImage frame = turnDone.getFrame(f);
        Graphics g = frame.getGraphics();
        g.setColor(tiredColor);

        // Loop through each pixel and shade the non-transparent ones.
        for( int y = 0; y < frame.getHeight(); ++y )
        {
          for( int x = 0; x < frame.getWidth(); ++x )
          {
            // Only shade pixels that are are not transparent.
            if( frame.getRGB(x, y) != 0 )
            {
              // Yes, one pixel at a time.
              g.fillRect(x, y, 1, 1);
            }
          }
        }
      }
    }
  }

  private void colorize(Color[] oldColors, Color[] newColors)
  {
    for( Sprite s : sprites )
    {
      s.colorize(oldColors, newColors);
    }
  }

  private BufferedImage getUnitImage(Commander activeCO, Unit u, int imageIndex)
  {
    BufferedImage frame = null;

    // Retrieve the correct subimage.
    if( u.isStunned || (u.isTurnOver && u.CO == activeCO) )
    {
      frame = turnDone.getFrame(imageIndex);
    }
    else
    {
      frame = sprites[ACTION_IDLE/*action*/].getFrame(imageIndex);
    }

    return frame;
  }

  public void drawUnit(Graphics g, Commander activeCO, Unit u, /* int action,*/int imageIndex, int drawX, int drawY, boolean flipImage)
  {
    BufferedImage frame = getUnitImage(activeCO, u, imageIndex);

    // Draw the unit, facing the appropriate direction.
    if( flipImage )
    {
      g.drawImage(frame, drawX + (frame.getWidth()), drawY, -frame.getWidth(), frame.getHeight(), null);
    }
    else
    {
      g.drawImage(frame, drawX, drawY, frame.getWidth(), frame.getHeight(), null);
    }
  }

  public void drawUnitIcons(Graphics g, Commander[] COs, Unit u, int animIndex, int drawX, int drawY)
  {
    int unitHeight = turnDone.getFrame(0).getHeight();

    // Draw the unit's HP if it is below full health.
    if( u.getHP() < 10 )
    {
      BufferedImage num = SpriteLibrary.getMapUnitHPSprites().getFrame(u.getHP());
      g.drawImage(num, drawX, drawY + ((unitHeight) / 2), num.getWidth(), num.getHeight(),
          null);
    }
    
    // Collect all the Commanders who desire to mark this unit
    ArrayList<Commander> markers = new ArrayList<Commander>();
    for( Commander co : COs )
    {
      char symbol = co.getUnitMarking(u);
      if( '\0' != symbol ) // null char is our sentry value
      {
        markers.add(co);
      }
    }

    // Draw one of them, based on our animation index
    if( !markers.isEmpty() )
    {
      Commander co = markers.get((animIndex%(markers.size()*ANIM_FRAMES_PER_MARK))/ANIM_FRAMES_PER_MARK);
      BufferedImage symbol = SpriteLibrary.getColoredMapTextSprites(co.myColor).get(co.getUnitMarking(u));
      // draw in the upper right corner
      g.drawImage(symbol, drawX + ((unitHeight) / 2), drawY, symbol.getWidth(), symbol.getHeight(), null);
    }

    // Draw the transport icon if the unit is holding another unit.
    if( u.heldUnits != null && !u.heldUnits.isEmpty() )
    {
      // Get the icon and characterize the draw space.
      BufferedImage cargoIcon = SpriteLibrary.getCargoIcon();
      int iconX = drawX + ((unitHeight) / 2);
      int iconY = drawY + ((unitHeight) / 2);
      int iconW = cargoIcon.getWidth();
      int iconH = cargoIcon.getHeight();

      // Draw team-color background for the icon.
      g.setColor( u.CO.myColor );
      g.fillRect( iconX, iconY, iconW, iconH);

      // Draw transport icon.
      g.drawImage( cargoIcon, iconX, iconY, iconW, iconH, null );
    }

    if( u.isStunned )
    {
      // Get the icon and characterize the draw space.
      BufferedImage stunIcon = SpriteLibrary.getStunIcon();
      int iconW = stunIcon.getWidth();
      int iconH = stunIcon.getHeight();

      // Draw team-color background for the icon.
      g.setColor( u.CO.myColor );
      g.fillRect( drawX+1, drawY+1, iconW-(2), iconH-(2));

      // Draw stun icon.
      g.drawImage( stunIcon, drawX, drawY, iconW, iconH, null );
    }

    // Draw the capture icon if the unit is capturing a base.
    if( u.getCaptureProgress() > 0 )
    {
      // Get the icon and characterize the draw space.
      BufferedImage captureIcon = SpriteLibrary.getCaptureIcon();
      int iconX = drawX + ((unitHeight) / 2);
      int iconY = drawY + ((unitHeight) / 2);
      int iconW = captureIcon.getWidth();
      int iconH = captureIcon.getHeight();

      // Draw team-color background for the icon.
      g.setColor( u.CO.myColor );
      g.fillRect( iconX, iconY, iconW, iconH);

      // Draw transport icon.
      g.drawImage( captureIcon, iconX, iconY, iconW, iconH, null );
    }
  }
}
