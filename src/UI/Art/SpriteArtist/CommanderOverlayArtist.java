package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.Commander;

public class CommanderOverlayArtist
{
  private static String overlayFundsString = "FUNDS     0";
  private static int previousOverlayFunds = 0;
  private static int animIndex = 0;
  private static long animIndexUpdateTime = 0;
  private static final int animIndexUpdateInterval = 8;
  
  public static void drawCommanderOverlay(Graphics g, Commander commander, boolean overlayIsLeft)
  {
    updateAnimIndex();

    int drawScale = SpriteOptions.getDrawScale();
    int coEyesWidth = 25;
    int xTextOffset = (4+coEyesWidth) * drawScale; // Distance from the side of the view to the CO overlay text.
    int yTextOffset = 3 * drawScale; // Distance from the top of the view to the CO overlay text.
    BufferedImage spriteA = SpriteLibrary.getLettersSmallCaps().getFrame(0); // Convenient reference so we can check dimensions.
    int textHeight = spriteA.getHeight() * drawScale;

    // Rebuild the funds string to draw if it has changed.
    if( previousOverlayFunds != commander.money )
    {
      previousOverlayFunds = commander.money;
      overlayFundsString = buildFundsString(commander);
    }

    String coString = commander.coInfo.name;

    // Choose left or right overlay image to draw.
    BufferedImage overlayImage = SpriteLibrary.getCoOverlay(commander, overlayIsLeft);
    BufferedImage powerBarImage = buildCoPowerBar(commander, commander.getAbilityCosts(), commander.getAbilityPower(), overlayIsLeft);
    final int POWERBAR_BUFFER = 3; // The distance from the top of the powerbar image frame to the top of the actual power bar (since the ability points are taller).

    if( overlayIsLeft )
    { // Draw the overlay on the left side.
      g.drawImage(overlayImage, 0, 0, overlayImage.getWidth() * drawScale, overlayImage.getHeight() * drawScale, null);
      SpriteLibrary.drawTextSmallCaps(g, coString, xTextOffset, yTextOffset, drawScale); // CO name
      SpriteLibrary.drawTextSmallCaps(g, overlayFundsString, xTextOffset, textHeight + drawScale + yTextOffset, drawScale); // Funds
      g.drawImage( powerBarImage, 0, (overlayImage.getHeight() * drawScale) - (POWERBAR_BUFFER*drawScale), powerBarImage.getWidth() * drawScale, powerBarImage.getHeight() * drawScale, null );
    }
    else
    { // Draw the overlay on the right side.
      int screenWidth = SpriteOptions.getScreenDimensions().width;
      int xPos = screenWidth - overlayImage.getWidth() * drawScale;
      int coNameXPos = screenWidth - spriteA.getWidth() * drawScale * coString.length() - xTextOffset;
      int fundsXPos = screenWidth - spriteA.getWidth() * drawScale * overlayFundsString.length() - xTextOffset;
      g.drawImage(overlayImage, xPos, 0, overlayImage.getWidth() * drawScale, overlayImage.getHeight() * drawScale, null);
      SpriteLibrary.drawTextSmallCaps(g, coString, coNameXPos, yTextOffset, drawScale); // CO name
      SpriteLibrary.drawTextSmallCaps(g, overlayFundsString, fundsXPos, textHeight + drawScale + yTextOffset, drawScale); // Funds
      int pbXPos = screenWidth - powerBarImage.getWidth() * drawScale;
      g.drawImage( powerBarImage, pbXPos + (powerBarImage.getWidth() * drawScale), (overlayImage.getHeight() * drawScale) - (POWERBAR_BUFFER*drawScale), -powerBarImage.getWidth() * drawScale, powerBarImage.getHeight() * drawScale, null );
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
   * Constructs a fixed-width (padded as needed) 11-character string to be drawn in the commander overlay.
   * @param funds The number to convert to an HUD overlay funds string.
   * @return A string of the form "FUNDS XXXXX" where X is either a space or a digit.
   */
  private static String buildFundsString(Commander cmdr)
  {
    StringBuilder sb = new StringBuilder("FUNDS ");
    if( cmdr.money < 10000 ) // Fewer than 5 digits
    {
      sb.append(" ");
    }
    if( cmdr.money < 1000 ) // Fewer than 4 digits
    {
      sb.append(" ");
    }
    if( cmdr.money < 100 ) // Fewer than 3 digits
    {
      sb.append(" ");
    }
    if( cmdr.money < 10 ) // Fewer than 2 digits. You poor.
    {
      sb.append(" ");
    }
    sb.append(Integer.toString(cmdr.money));

    return sb.toString();
  }
  
  /**
   * Generate an ability-power bar for the given Commander at 1x size. The requester is responsible for applying any scale factors.
   */
  public static BufferedImage buildCoPowerBar(Commander co, int[] abilityPoints, double currentPower, boolean leftSide)
  {
    int slowAnimIndex = (animIndex/32) % 2;

    // Find the most expensive ability so we know how long to draw the bar.
    int maxAP = 0;
    for( int i = 0; i < abilityPoints.length; ++i )
    {
      maxAP = (maxAP < abilityPoints[i]) ? abilityPoints[i] : maxAP;
    }
    final int imageBufferW = 2;

    // Unfortunately, the power bar is a "some assembly required" kinda deal, so we have to put it together here.
    BufferedImage powerBar = SpriteLibrary.getCoOverlayPowerBar(co, maxAP, co.getAbilityPower());
    Sprite powerBarPieces = SpriteLibrary.getCoOverlayPowerBarAPs(co);

    // Make a new BufferedImage to hold the composited power bar, and set it all transparent to start.
    BufferedImage bar = SpriteLibrary.createDefaultBlankSprite(powerBar.getWidth()+imageBufferW, powerBarPieces.getFrame(0).getHeight());
    Sprite spr = new Sprite(bar);
    Color[] black = {new Color(0,0,0)};
    Color[] transparent = {new Color(0, 0, 0, 0)};
    spr.colorize(black, transparent);
    
    Graphics barGfx = bar.getGraphics();
    
    // Draw the actual bar.
    barGfx.drawImage(powerBar, 0, 2, null);

    // Draw the ability points
    boolean atLeastOne = false;
    for( int i = 0; i < abilityPoints.length; ++i )
    {
      int requiredPower = abilityPoints[i];
      double diff = requiredPower - currentPower;
      atLeastOne |= (diff < 0);
      BufferedImage segment = (diff > 1) ? powerBarPieces.getFrame(0)  // empty
          : ((diff > 0.5) ? powerBarPieces.getFrame(1)                 // 1/3 full
              : ((diff > 0) ? powerBarPieces.getFrame(2)               // 2/3 full
                  : ((slowAnimIndex == 0) ? powerBarPieces.getFrame(3) // filled
                      : powerBarPieces.getFrame(4))) );                // blinking
      int drawLoc = (requiredPower * 2) - imageBufferW - 3; // -3 to center the image around the power level.
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
}
