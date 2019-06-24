package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;

public class CommanderOverlayArtist
{
  private static String overlayFundsString = "FUNDS     0";
  private static int previousOverlayFunds = 0;
  private static int animIndex = 0;
  private static long animIndexUpdateTime = 0;
  private static final int animIndexUpdateInterval = 8;
  
  private static Map<String, Sprite> activeAbilityTextSprites = new HashMap<String, Sprite>();

  public static void drawCommanderOverlay(Graphics g, Commander commander, boolean overlayIsLeft)
  {
    updateAnimIndex();

    int coEyesWidth = 25;
    int xTextOffset = (4+coEyesWidth); // Distance from the side of the view to the CO overlay text.
    int yTextOffset = 3; // Distance from the top of the view to the CO overlay text.
    BufferedImage spriteA = SpriteLibrary.getLettersSmallCaps().getFrame(0); // Convenient reference so we can check dimensions.
    int textHeight = spriteA.getHeight();

    // Rebuild the funds string to draw if it has changed.
    if( previousOverlayFunds != commander.money )
    {
      previousOverlayFunds = commander.money;
      overlayFundsString = String.format("FUNDS %5d", commander.money);
    }

    String coString = commander.coInfo.name;

    // Choose left or right overlay image to draw.
    BufferedImage overlayImage = SpriteLibrary.getCoOverlay(commander, overlayIsLeft);
    BufferedImage powerBarImage = null;
    boolean trueIfBarFalseIfText = commander.getActiveAbilityName().isEmpty();
    if( trueIfBarFalseIfText )
    {
      powerBarImage = buildCoPowerBar(commander, commander.getAbilityCosts(), commander.getAbilityPower(), overlayIsLeft);
    }
    else
    {
      powerBarImage = getActiveAbilityName(commander.getActiveAbilityName());
    }
    final int POWERBAR_BUFFER = 3; // The distance from the top of the powerbar image frame to the top of the actual power bar (since the ability points are taller).

    if( overlayIsLeft )
    { // Draw the overlay on the left side.
      g.drawImage(overlayImage, 0, 0, overlayImage.getWidth(), overlayImage.getHeight(), null);
      SpriteUIUtils.drawTextSmallCaps(g, coString, xTextOffset, yTextOffset); // CO name
      SpriteUIUtils.drawTextSmallCaps(g, overlayFundsString, xTextOffset, textHeight + 1 + yTextOffset); // Funds
      g.drawImage( powerBarImage, 0, (overlayImage.getHeight()) - (POWERBAR_BUFFER), powerBarImage.getWidth(), powerBarImage.getHeight(), null );
    }
    else
    { // Draw the overlay on the right side.
      int mapViewWidth = SpriteOptions.getScreenDimensions().width / SpriteOptions.getDrawScale();
      int xPos = mapViewWidth - overlayImage.getWidth();
      int coNameXPos = mapViewWidth - spriteA.getWidth() * coString.length() - xTextOffset;
      int fundsXPos = mapViewWidth - spriteA.getWidth() * overlayFundsString.length() - xTextOffset;
      g.drawImage(overlayImage, xPos, 0, overlayImage.getWidth(), overlayImage.getHeight(), null);
      SpriteUIUtils.drawTextSmallCaps(g, coString, coNameXPos, yTextOffset); // CO name
      SpriteUIUtils.drawTextSmallCaps(g, overlayFundsString, fundsXPos, textHeight + 1 + yTextOffset); // Funds
      int pbXPos = mapViewWidth - powerBarImage.getWidth();
      if( trueIfBarFalseIfText )
      {
        // We are drawing the power bar, and want to flip it horizontally.
        g.drawImage( powerBarImage, pbXPos + (powerBarImage.getWidth()), (overlayImage.getHeight()) - (POWERBAR_BUFFER), -powerBarImage.getWidth(), powerBarImage.getHeight(), null );
      }
      else
      {
        // We are drawing an ability name, and we don't want to flip it.
        g.drawImage( powerBarImage, pbXPos, (overlayImage.getHeight()) - (POWERBAR_BUFFER), powerBarImage.getWidth(), powerBarImage.getHeight(), null );
      }
    }
  }

  private static void updateAnimIndex()
  {
    // Calculate the sprite index to use.
    long thisTime = System.currentTimeMillis();
    long animTimeDiff = thisTime - animIndexUpdateTime;

    // If it's time to update the sprite index... update the sprite index.
    if( animTimeDiff > animIndexUpdateInterval )
    {
      animIndex++;
      animIndexUpdateTime = thisTime;
    } 
  }

  /**
   * Generate an ability-power bar for the given Commander at 1x size. The requester is responsible for applying any scale factors.
   */
  public static BufferedImage buildCoPowerBar(Commander co, double[] abilityPoints, double currentPower, boolean leftSide)
  {
    final double pixelsPerPowerUnit = 3.0;
    int slowAnimIndex = (animIndex/32) % 2;

    // Find the most expensive ability so we know how long to draw the bar.
    double maxAP = 0;
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
    if( atLeastOne )
    {
      int glintPos = animIndex % 600; // Say, every 10 seconds or so.
      if( glintPos < bar.getWidth()-5)
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

    return bar;
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
    return activeAbilityTextSprites.get(abilityName).getFrame((animIndex/4));
  }
}
