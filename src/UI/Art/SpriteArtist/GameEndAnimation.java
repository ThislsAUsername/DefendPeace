package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import UI.SlidingValue;
import UI.Art.Animation.GameAnimation;

/**
 * Draws the end-of-battle victory/defeat overlay anmiation.
 */
public class GameEndAnimation implements GameAnimation
{
  private ArrayList<GameResultPanel> panels;

  private int panelsInPlace = 0;

  public GameEndAnimation(Commander[] commanders)
  {
    // Figure out how far apart to draw each panel.
    int numCommanders = commanders.length;

    // If we draw n panels, we will have n+1 spaces around/between them.
    int vSpacing = (SpriteOptions.getScreenDimensions().height/SpriteOptions.getDrawScale()) / (numCommanders+1);

    // Set our starting position.
    int hLoc = vSpacing;
    
    // Create and populate our ArrayList.
    panels = new ArrayList<GameResultPanel>();
    for( int i = 0; i < numCommanders; ++i, hLoc += vSpacing)
    {
      int xDir = (i % 2 == 0)? -1:1;
      // Create a victory/defeat panel for each commander.
      panels.add( new GameResultPanel(commanders[i], xDir, hLoc));
    }
  }

  /**
   *  Draw each of the victory/defeat panels, animating them into place from off-screen.
   */
  @Override
  public boolean animate(Graphics g)
  {
    if( panelsInPlace < panels.size() )
    {
      // Get the panel we currently want to move.
      GameResultPanel panel = panels.get(panelsInPlace);
      panel.xPos.set(0);

      // Decide whether this panel is in place now.
      if( 0 == panel.xPos.get() )
      {
        panelsInPlace++;
      }
    }

    // Draw all of the panels.
    for( GameResultPanel p : panels )
    {
      BufferedImage img = p.panel;
      g.drawImage( p.panel, (int)p.xPos.get(), p.yPos-img.getHeight()/2, img.getWidth(), img.getHeight(), null);
    }

    // Never terminate the animation. The game is over, so just hang out until the game exits.
    return false;
  }

  @Override
  public void cancel()
  {
    // No action. Any cancel will just exit the battle anyway.
  }

  private static class GameResultPanel
  {
    BufferedImage panel;
    SlidingValue xPos;
    int yPos;

    public GameResultPanel(Commander cmdr, int xDir, int hPos)
    {
      // Establish some basic parameters.
      int screenWidth = SpriteOptions.getScreenDimensions().width/SpriteOptions.getDrawScale();
      
      // Figure out where the panel will start out before moving onto the screen.
      xPos = new SlidingValue(screenWidth * xDir);
      yPos = hPos;

      // Get the CO eyes image and the VICTORY/DEFEAT text.
      BufferedImage coMug = SpriteLibrary.getCommanderSprites(cmdr.coInfo.name).eyes;
      BufferedImage resultText = (cmdr.isDefeated)? SpriteLibrary.getGameOverDefeatText() : SpriteLibrary.getGameOverVictoryText();

      // Make a panel image large enough to fill the screen horizontally, and frame the CO portrait vertically.
      panel = SpriteLibrary.createDefaultBlankSprite(
          screenWidth,
          (coMug.getHeight() + 2));
      Graphics g = panel.getGraphics();

      // Make it all black to start, so we have a border/edge to frame the panel.
      g.setColor( Color.BLACK );
      g.fillRect( 0, 0, panel.getWidth(), panel.getHeight() );

      // Draw the background based on the CO color, inside our frame.
      g.setColor( cmdr.myColor );
      g.fillRect( 0, 1, panel.getWidth(), panel.getHeight() - 2 );

      // Draw the CO portrait in place
      g.drawImage(coMug, (screenWidth / 8), 1,
          coMug.getWidth(), coMug.getHeight(), null);

      // Draw the victory/defeat text, centered.
      int xPos = ( (screenWidth / 2) - ( (resultText.getWidth()) / 2) ); 
      g.drawImage(resultText, xPos, 3, resultText.getWidth(), resultText.getHeight(), null);
    }
  }
}
