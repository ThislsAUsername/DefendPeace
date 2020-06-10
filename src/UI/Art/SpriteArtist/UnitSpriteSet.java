package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Function;

import CommandingOfficers.Commander;
import UI.UIUtils;
import Units.Unit;

public class UnitSpriteSet
{
  Sprite sprites[] = new Sprite[AnimState.values().length];

  public final int ANIM_FRAMES_PER_MARK = 3;

  public static enum AnimState
  {
    IDLE
    {
      public String toString()
      {
        return ""; // To match the existing map image format
      }
    },
    TIRED, MOVENORTH, MOVEEAST, MOVESOUTH, MOVEWEST, DIE
  }

  /**
   * Fetch a sprite sheet for each animation state and colorize it
   * @param fileFinder Mapping from action type to file name
   * @param coColors What colors the end sprites should use
   */
  public UnitSpriteSet(Function<AnimState, String> fileFinder, ColorPalette coColors)
  {
    try
    {
      for( int action = 0; action < AnimState.values().length; ++action )
      {
        String filestr = fileFinder.apply(AnimState.values()[action]);
        BufferedImage spriteSheet = SpriteLibrary.loadSpriteSheetFile(filestr);
        if( null != spriteSheet )
        {
          // Assume sub-sprites are squares that fill out the height of the source image
          Sprite spr = new Sprite(spriteSheet, spriteSheet.getHeight(), spriteSheet.getHeight());
          sprites[action] = spr;
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("WARNING: Exception hit in UnitSpriteSet constructor:" + e);
      System.out.println("WARNING:   Attempting to continue.");
    }

    // Handle the case of having one sideways move direction and not the other defined
    if( null == sprites[AnimState.MOVEEAST.ordinal()] && null != sprites[AnimState.MOVEWEST.ordinal()] )
      sprites[AnimState.MOVEEAST.ordinal()] = new Sprite(sprites[AnimState.MOVEWEST.ordinal()], true);

    if( null == sprites[AnimState.MOVEWEST.ordinal()] && null != sprites[AnimState.MOVEEAST.ordinal()] )
      sprites[AnimState.MOVEWEST.ordinal()] = new Sprite(sprites[AnimState.MOVEEAST.ordinal()], true);

    // Fill out any missing images
    Sprite defaultSprite = sprites[AnimState.IDLE.ordinal()];
    // Use the IDLE action if it exists, otherwise we are going with a black rectangle.
    if( null == defaultSprite )
      defaultSprite = new Sprite(null, SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize);
    for( int action = 0; action < AnimState.values().length; ++action )
    {
      if( null == sprites[action] )
        sprites[action] = defaultSprite;
    }

    colorize(UIUtils.defaultMapColors, coColors.paletteColors);

    Sprite turnDone = new Sprite(sprites[AnimState.TIRED.ordinal()]);
    sprites[AnimState.TIRED.ordinal()] = turnDone;

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

  private void colorize(Color[] oldColors, Color[] newColors)
  {
    for( Sprite s : sprites )
    {
      s.colorize(oldColors, newColors);
    }
  }

  private BufferedImage getUnitImage(AnimState state, int imageIndex)
  {
    return sprites[state.ordinal()].getFrame(imageIndex);
  }

  public void drawUnit(Graphics g, Unit u, AnimState state, int imageIndex, int drawX, int drawY)
  {
    boolean flipImage = SpriteMapView.shouldFlip(u);

    BufferedImage frame = getUnitImage(state, imageIndex);
    int shiftX =(SpriteLibrary.baseSpriteSize - frame.getWidth())/2; // center X
    int shiftY = SpriteLibrary.baseSpriteSize - frame.getHeight(); // bottom-justify Y

    // Draw the unit, facing the appropriate direction.
    if( flipImage && isStateFlippable(state) )
    {
      g.drawImage(frame, drawX - shiftX + (frame.getWidth()), drawY + shiftY, -frame.getWidth(), frame.getHeight(), null);
    }
    else
    {
      g.drawImage(frame, drawX + shiftX, drawY + shiftY, frame.getWidth(), frame.getHeight(), null);
    }
  }

  public void drawUnitIcons(Graphics g, Commander[] COs, Unit u, int animIndex, int drawX, int drawY)
  {
    int unitHeight = sprites[0].getFrame(0).getHeight();

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

  public static boolean isStateFlippable(AnimState state)
  {
    switch(state)
    {
      case MOVENORTH:
      case MOVEEAST:
      case MOVESOUTH:
      case MOVEWEST:
        return false;
      default:
        return true;
    }
  }
}
