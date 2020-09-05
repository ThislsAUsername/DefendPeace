package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import UI.InputOptionsController;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;

public class ControlExtraOptionsSetupArtist
{
  private static HorizontalSelectorTemplate template;

  public static void draw(Graphics g)
  {
    // Draw a fancy background.
    DiagonalBlindsBG.draw(g);

    if( null == template )
    {
      template = new HorizontalSelectorTemplate();
      template.initialize(InputOptionsController.allOptions);
    }

    // Set up some initial parameters.
    int spacing = template.optionNamePanel.getHeight()*2;
    int yDraw = spacing/2;

    // Find the selected command/key.
    int selectedAction = InputOptionsController.actionCommandSelector.getSelectionNormalized();

    // Create an un-scaled image to draw everything at real size before scaling it to the screen.
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage controlsImage = SpriteLibrary.createTransparentSprite(dimensions.width/drawScale, dimensions.height/drawScale);
    Graphics cig = controlsImage.getGraphics();

    // Loop through and draw all the bindings, building or updating the necessary images.
    yDraw = spacing/2; // Reset y-spacing to draw keys.
    for( int ip = 0; ip < InputOptionsController.actionCommandSelector.size(); ++ip )
    {
      template.drawGameOption(cig, spacing / 2, yDraw, InputOptionsController.allOptions[ip]);
      if( ip == selectedAction ) // Draw the cursor over the selected item.
      {
        int xDraw = template.graphicsOptionWidth - template.optionSettingPanel.getWidth()/2 - 8;
        cig.drawImage(template.optionArrows,
                      xDraw, yDraw + 3,
                      template.optionArrows.getWidth(), template.optionArrows.getHeight(),
                      null);
      }
      yDraw += spacing;
    }

    // Redraw to the screen at scale.
    g.drawImage(controlsImage, 0, 0, controlsImage.getWidth()*drawScale, controlsImage.getHeight()*drawScale, null);
  }
}
