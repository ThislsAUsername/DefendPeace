package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import UI.UIUtils;
import UI.UIUtils.Faction;
import Units.Unit;
import Units.UnitModel;

public class UnitSpriteSet
{
  Sprite sprites[] = new Sprite[AnimState.values().length];

  public final int ANIM_FRAMES_PER_MARK = 3; 

  Sprite turnDone;
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
  public UnitSpriteSet(String unitType, Faction faction, ColorPalette coColors)
  {
    try
    {
      // Create a filename string template to fetch all relevant animations.
      String filenameTemplate = getMapUnitSpriteFilenameTemplate(unitType, faction);
      for( int action = 0; action < AnimState.values().length; ++action )
      {
        // Get the filename for this animation state.
        String fileStr = String.format(filenameTemplate, UnitModel.standardizeID(AnimState.values()[action].toString()));

        BufferedImage spriteSheet = SpriteLibrary.loadSpriteSheetFile(fileStr);
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

  /**
   * Find the IDLE map-sprite file for the given unit type, as owned by the specified faction.
   * If the specified faction has no sprite for that unit, it will try to load it from that faction's
   * basis instead. If the basis also has no sprite, then it will default to the "Thorn" version.
   * The resulting template-string ensures that all sprites loaded are from the same faction set.
   * @return A string with the given unit/faction names populated, and a template token for the unit state.
   */
  private String getMapUnitSpriteFilenameTemplate(String unitType, Faction faction)
  {
    final String format = "res/unit/faction/%s/%s_map%s.png";

    // Try the faction's proper name, the one it's based off of, then default to "Thorn" if all else fails.
    String[] namesToTry = {faction.name, faction.basis};
    String template = String.format( format, SpriteLibrary.DEFAULT_FACTION, UnitModel.standardizeID(unitType), "" ); // Replace if we can.
    for( String name : namesToTry )
    {
      String idleName = String.format( format, name, UnitModel.standardizeID(unitType), "" );
      if (new File(idleName).canRead())
      {
        template = String.format( format, name, UnitModel.standardizeID(unitType), "%s" );
        break;
      }
    }
    return template;
  }

  private void colorize(Color[] oldColors, Color[] newColors)
  {
    for( Sprite s : sprites )
    {
      s.colorize(oldColors, newColors);
    }
  }

  /**
   * Return the first IDLE sprite image.
   */
  public BufferedImage getUnitImage()
  {
    return sprites[AnimState.IDLE.ordinal()].getFrame(0);
  }

  /**
   * Return the subimage of the requested sprite, greying if it cannot move, unless a different CO is active.
   */
  private BufferedImage getUnitImage(AnimState state, int imageIndex)
  {
    return sprites[state.ordinal()].getFrame(imageIndex);
  }

  /**
   * Return the frame `imageIndex` of the `action` Sprite in this UnitSpriteSet.
   */
  public BufferedImage getUnitImage(int action, int imageIndex)
  {
    BufferedImage frame = null;
    frame = sprites[action].getFrame(imageIndex);
    return frame;
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

  /**
   * Draw icons detailing any relevant unit information.
   * Icons are arranged as follows:
   * Upper Left: Status icons, e.g. when stunned or low on fuel or ammo.
   * Upper Right: Special markings, usually applied by Commander abilities.
   * Lower Right: Activity icons, e.g. transporting units or capturing property.
   * Lower Left: HP when not at full health (this can extend to the lower right if the unit has >10 HP).
   */
  public void drawUnitIcons(Graphics g, Commander[] COs, Unit u, int animIndex, int drawX, int drawY)
  {
    int unitHeight = sprites[0].getFrame(0).getHeight();

    ArrayList<BufferedImage> unitIcons = new ArrayList<BufferedImage>();

    // Draw the unit's HP if it is not at full health.
    if( u.getHP() != u.model.maxHP )
    {
      BufferedImage num;
      if( u.getHP() > u.model.maxHP )
      {
        num = SpriteLibrary.getMapUnitNumberSprites().getFrame(1); // Tens place.

        // Ones place shares space with the activity icons below if HP > 10.
        unitIcons.add( SpriteLibrary.getMapUnitNumberSprites().getFrame((u.getHP()-u.model.maxHP)) );
      }
      else
        num = SpriteLibrary.getMapUnitNumberSprites().getFrame(u.getHP());
      g.drawImage(num, drawX, drawY + ((unitHeight) / 2), num.getWidth(), num.getHeight(), null);
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

    // Evaluate/draw unit status effects.
    ArrayList<BufferedImage> statusIcons = new ArrayList<BufferedImage>();
    if( u.isStunned )
      statusIcons.add(SpriteLibrary.getStunIcon());

    double lowIndicatorFraction = 3.0;
    if( u.fuel < u.model.maxFuel / lowIndicatorFraction )
      statusIcons.add(SpriteLibrary.getFuelIcon());

    if( u.ammo >= 0 && !u.model.weapons.isEmpty() && u.ammo < u.model.maxAmmo / lowIndicatorFraction )
      statusIcons.add(SpriteLibrary.getAmmoIcon());

    if( !statusIcons.isEmpty() )
    {
      int iconIndex = (animIndex%(statusIcons.size()*ANIM_FRAMES_PER_MARK))/ANIM_FRAMES_PER_MARK;
      BufferedImage statusIcon = statusIcons.get(iconIndex);
      int iconW = statusIcon.getWidth();
      int iconH = statusIcon.getHeight();

      g.drawImage( statusIcon, drawX, drawY, iconW, iconH, null );
    }

    // Transport icon.
    if( u.heldUnits != null && !u.heldUnits.isEmpty() )
      unitIcons.add(SpriteLibrary.getCargoIcon(u.CO.myColor));

    // Capture icon.
    if( u.getCaptureProgress() > 0 )
      unitIcons.add(SpriteLibrary.getCaptureIcon(u.CO.myColor));

    // Hide icon.
    if( u.model.hidden )
      unitIcons.add(SpriteLibrary.getHideIcon(u.CO.myColor));

    // Draw one of the current activity icons in the lower-right.
    if( !unitIcons.isEmpty() )
    {
      int iconIndex = (animIndex%(unitIcons.size()*ANIM_FRAMES_PER_MARK))/ANIM_FRAMES_PER_MARK;
      BufferedImage icon = unitIcons.get(iconIndex);

      int iconX = drawX + ((unitHeight) / 2);
      int iconY = drawY + ((unitHeight) / 2);
      int iconW = icon.getWidth();
      int iconH = icon.getHeight();

      // Draw the icon
      g.drawImage( icon, iconX, iconY, iconW, iconH, null );
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
