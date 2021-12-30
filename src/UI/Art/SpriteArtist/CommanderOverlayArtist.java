package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.Army;
import Terrain.GameMap;
import UI.COStateInfo;

public class CommanderOverlayArtist
{
  public static final int OVERLAY_WIDTH = 97;
  public static final int OVERLAY_HEIGHT = 20;

  private static final int animIndexUpdateInterval = 13;
  
  private static Map<String, Sprite> activeAbilityTextSprites = new HashMap<String, Sprite>();

  /**
   * Draws an overlay for all COs passed in, left-justified, with status text and optionally highlights one CO
   */
  public static BufferedImage drawAllCommanderOverlays(Army[] coList, GameMap drawableMap, int drawingWidth, int drawingHeight, Army coToHighlight)
  {
    BufferedImage output = SpriteLibrary.createTransparentSprite(drawingWidth, drawingHeight);
    Graphics g = output.getGraphics();
    int verticalOffset = 0;

    for( Army CO : coList )
    {
      // Highlight the selected CO
      if( CO == coToHighlight )
      {
        g.setColor(SpriteUIUtils.MENUHIGHLIGHTCOLOR);
        g.fillRect(0, verticalOffset, drawingWidth, OVERLAY_HEIGHT);
      }

      CommanderOverlayArtist.drawCommanderOverlay(g, CO, verticalOffset, true);
      verticalOffset += OVERLAY_HEIGHT + 5;

      // Add brief status text per CO
      String status = new COStateInfo(drawableMap, CO).getAbbrevStatus();
      BufferedImage statusText = SpriteUIUtils.drawProseToWidth(status, drawingWidth);
      g.drawImage(statusText, 0, verticalOffset, null);
      verticalOffset += statusText.getHeight() + 5;

      if( verticalOffset + OVERLAY_HEIGHT > drawingHeight )
        break;
    }

    return output;
  }

  public static void drawCommanderOverlay(Graphics g, Army army, boolean overlayIsLeft)
  {
    drawCommanderOverlay(g, army, 0, overlayIsLeft);
  }
  public static void drawCommanderOverlay(Graphics g, Army army, int yInitialOffset, boolean overlayIsLeft)
  {
    final int coEyesWidth = 25;
    final int xTextOffset = (4+coEyesWidth); // Distance from the side of the view to the CO overlay text.
    final int yTextOffset = 3; // Distance from the top of the view to the CO overlay text.
    final int mapViewWidth = SpriteOptions.getScreenDimensions().width / SpriteOptions.getDrawScale();
    final BufferedImage spriteA = SpriteLibrary.getLettersSmallCaps().getFrame(0); // Convenient reference so we can check dimensions.

    int yCurrentOffset = yInitialOffset;

    for(Commander commander : army.cos)
    {
      // Choose left or right overlay image to draw.
      BufferedImage overlayImage = SpriteLibrary.getCoOverlay(commander, overlayIsLeft);
      BufferedImage powerBarImage = null;
      CommanderAbility ability = commander.getActiveAbility();
      final int xAbilityOffset;
      if( null == ability )
      {
        powerBarImage = buildCoPowerBar(commander, commander.getAbilityCosts(), commander.getAbilityPower(), overlayIsLeft);
        xAbilityOffset = coEyesWidth + 1;
      }
      else
      {
        powerBarImage = getActiveAbilityName(ability.toString());
        xAbilityOffset = xTextOffset;
      }

      int energy = COStateInfo.getEnergyUntilNextPower(commander);
      int energyLength = ("" + energy).length();

      final int yPowerBarOffset = SpriteLibrary.baseIconSize + yTextOffset - 1 + yCurrentOffset;
      if( overlayIsLeft )
      { // Draw the overlay on the left side.
        g.drawImage(overlayImage, 0, yCurrentOffset, overlayImage.getWidth(), overlayImage.getHeight(), null);
        drawIconAndValue(g, SpriteLibrary.MapIcons.ENERGY, energy, energyLength, xTextOffset, yTextOffset + yCurrentOffset);
        g.drawImage( powerBarImage, xAbilityOffset, yPowerBarOffset, powerBarImage.getWidth(), powerBarImage.getHeight(), null );
      }
      else
      { // Draw the overlay on the right side.
        int xPos = mapViewWidth - overlayImage.getWidth();
        int valueXPos = mapViewWidth - ICON_VALUE_SPACING - spriteA.getWidth()*energyLength - xTextOffset;
        g.drawImage(overlayImage, xPos, yCurrentOffset, overlayImage.getWidth(), overlayImage.getHeight(), null);
        drawIconAndValue(g, SpriteLibrary.MapIcons.ENERGY, energy, energyLength, valueXPos, yTextOffset + yCurrentOffset);
        int pbXPos = mapViewWidth - xAbilityOffset - powerBarImage.getWidth();
        g.drawImage( powerBarImage, pbXPos, yPowerBarOffset, powerBarImage.getWidth(), powerBarImage.getHeight(), null );
      }

      yCurrentOffset += overlayImage.getHeight() - 1;
    }

    final int moneyBufferPx = 2;
    final int fundsLength = ("" + army.money).length();
    final int fundsWidth = ICON_VALUE_SPACING + fundsLength * spriteA.getWidth() + 1;
    final int drawX = overlayIsLeft ? 0 : mapViewWidth - fundsWidth;
    final int drawY = yInitialOffset - moneyBufferPx + yCurrentOffset;
    SpriteUIUtils.drawMenuFrame(g, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR,
        drawX, drawY, fundsWidth, SpriteLibrary.baseIconSize+moneyBufferPx, moneyBufferPx);
    drawIconAndValue(g, SpriteLibrary.MapIcons.FUNDS, army.money,  fundsLength, drawX, drawY + moneyBufferPx);
  }

  private static final int ICON_VALUE_SPACING = SpriteLibrary.baseIconSize + 2;
  static void drawIconAndValue(Graphics g, SpriteLibrary.MapIcons icon, int value, int valueLength, int drawX, int drawY)
  {
    g.drawImage(icon.getIcon(), drawX, drawY, null);
    drawX += ICON_VALUE_SPACING;
    SpriteUIUtils.drawTextSmallCaps(g, String.format("%"+valueLength+"d", value), drawX, drawY);
  }

  private static int getAnimIndex()
  {
    // Fun fact: casting long->int can produce negative numbers, for some reason.
    int thisTime = Math.abs((int)System.currentTimeMillis());
    return  (thisTime / animIndexUpdateInterval);
  }

  /**
   * Generate an ability-power bar for the given Commander at 1x size. The requester is responsible for applying any scale factors.
   */
  public static BufferedImage buildCoPowerBar(Commander co, double[] abilityPoints, double currentPower, boolean leftSide)
  {
    final double pixelsPerPowerUnit = 3.0;
    final int animIndex = getAnimIndex();
    int slowAnimIndex = (animIndex/32) % 2;

    // Find the most expensive ability so we know how long to draw the bar.
    double maxAP = 1;
    for( int i = 0; i < abilityPoints.length; ++i )
    {
      maxAP = (maxAP < abilityPoints[i]) ? abilityPoints[i] : maxAP;
    }
    final int imageBufferW = 2;

    // Unfortunately, the power bar is a "some assembly required" kinda deal, so we have to put it together here.
    BufferedImage powerBar = SpriteLibrary.getCoOverlayPowerBar(co, maxAP, currentPower, pixelsPerPowerUnit);
    Sprite powerBarPieces = SpriteLibrary.getCoOverlayPowerBarAPs(co);

    // Make a new BufferedImage to hold the composited power bar, and set it all transparent to start.
    BufferedImage bar = SpriteLibrary.createTransparentSprite(powerBar.getWidth()+imageBufferW, powerBarPieces.getFrame(0).getHeight());

    Graphics barGfx = bar.getGraphics();
    
    // Draw the actual bar.
    barGfx.drawImage(powerBar, 0, 2, null);

    // Draw the ability points
    boolean atLeastOne = false;
    for( int i = 0; i < abilityPoints.length; ++i )
    {
      double requiredPower = abilityPoints[i];
      double diff = requiredPower - currentPower;
      atLeastOne |= (diff < 0);
      BufferedImage segment = (diff > 1) ? powerBarPieces.getFrame(0)  // empty
          : ((diff > 0.5) ? powerBarPieces.getFrame(1)                 // 1/3 full
              : ((diff > 0) ? powerBarPieces.getFrame(2)               // 2/3 full
                  : ((slowAnimIndex == 0) ? powerBarPieces.getFrame(3) // filled
                      : powerBarPieces.getFrame(4))) );                // blinking
      int drawLoc = ((int) (requiredPower*pixelsPerPowerUnit)) - imageBufferW - 3; // -3 to center the image around the power level.
      barGfx.drawImage(segment, drawLoc, 0, null);
    }
    
    // Draw a glint every now and then, if at least one ability is available.
    final int width = bar.getWidth();
    final int height = bar.getHeight();
    if( atLeastOne )
    {
      int glintPos = animIndex % 600; // Say, every 10 seconds or so.
      if( glintPos < width-5)
      {
        BufferedImage glint = SpriteLibrary.createDefaultBlankSprite(5, 5);
        int empty[] = { 255,255,255,0  , 255,255,255,0  , 255,255,255,80 , 255,255,255,80 , 255,255,255,80 ,
                        255,255,255,0  , 255,255,255,200, 255,255,255,200, 255,255,255,200, 255,255,255,200,
                        255,255,255,0  , 255,255,255,185, 255,255,255,185, 255,255,255,185, 255,255,255,0  ,
                        255,255,255,100, 255,255,255,100, 255,255,255,100, 255,255,255,100, 255,255,255,0  ,
                        255,255,255,50 , 255,255,255,50 , 255,255,255,50 , 255,255,255,0  , 255,255,255,0  };
        glint.getRaster().setPixels(0, 0, glint.getWidth(), glint.getHeight(), empty);
        barGfx.drawImage(glint, glintPos, 2, null);
      }
    }

    BufferedImage output = bar;
    if(!leftSide)
    { // If we're drawing on the right, flip the image for our caller
      output = SpriteLibrary.createTransparentSprite(width, height);
      output.getGraphics().drawImage(bar, width, 0, -width, height, null);
    }

    return output;
  }

  private static BufferedImage getActiveAbilityName(String abilityName)
  {
    // If we don't have this image yet, build it.
    if( !activeAbilityTextSprites.containsKey(abilityName) )
    {
      // Define a new image to hold the name with a little wiggle room.
      BufferedImage letterA = SpriteLibrary.getLettersSmallCaps().getFrame(0);
      BufferedImage background = SpriteLibrary.createTransparentSprite((letterA.getWidth()*abilityName.length()) + 2, letterA.getHeight() + 2);
      BufferedImage foreground = SpriteLibrary.createTransparentSprite((letterA.getWidth()*abilityName.length()) + 2, letterA.getHeight() + 2);
      Graphics bgGfx = background.getGraphics();
      Graphics fgGfx = foreground.getGraphics();

      // Generate a shaped black background to hold the ability name.
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 0, 0);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 0, 1);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 0, 2);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 1, 0);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 1, 1);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 1, 2);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 2, 0);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 2, 1);
      SpriteUIUtils.drawTextSmallCaps(bgGfx, abilityName, 2, 2);

      // Make an intermediate image of the center text, and recolor it to an intermediate value.
      SpriteUIUtils.drawTextSmallCaps(fgGfx, abilityName, 1, 1);
      Sprite fgSpr = new Sprite(foreground);
      Color intermediate = new Color(99, 100, 101);
      fgSpr.colorize(Color.BLACK, intermediate);

      // Active abilities are exciting! Therefore, they must be displayed with flashy, scintillating colors!
      // Let's build up a sprite sheet to spec, and then build a Sprite around it.
      Color frameColors[] = {new Color(255, 0, 0),new Color(255, 127, 0),new Color(255, 255, 0),new Color(0, 255, 0),new Color(0, 0, 255),new Color(255, 0, 255)};
      BufferedImage spriteSheet = SpriteLibrary.createTransparentSprite(background.getWidth()*frameColors.length, background.getHeight());
      Graphics ssGfx = spriteSheet.getGraphics();
      for( int i = 0; i < frameColors.length; ++i )
      {
        // This frame shall be this color.
        Color color = frameColors[i];

        // Draw the background image at the current offset.
        ssGfx.drawImage(background, i*background.getWidth(), 0, null);

        // Colorize the foreground image, and draw it onto the sprite sheet.
        fgSpr.colorize(intermediate, color);
        ssGfx.drawImage(fgSpr.getFrame(0), i*background.getWidth(), 0, null);

        // Un-colorize the foreground so it's ready for the next loop iteration.
        fgSpr.colorize(color, intermediate);
      }

      Sprite sprite = new Sprite(spriteSheet, background.getWidth(), background.getHeight());
      activeAbilityTextSprites.put(abilityName, sprite);
    }

    // Return the current sub-image.
    return activeAbilityTextSprites.get(abilityName).getFrame((getAnimIndex()/4));
  }
}
