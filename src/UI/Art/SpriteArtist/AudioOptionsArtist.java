package UI.Art.SpriteArtist;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import UI.AudioEngine;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;
import lombok.var;

public class AudioOptionsArtist
{
  private static HorizontalSelectorTemplate template = new HorizontalSelectorTemplate();

  static void initialize()
  {
    template.initialize(AudioEngine.allOptions);
  }

  public static void draw(Graphics g)
  {
    // Draw a fancy background.
    DiagonalBlindsBG.draw(g);

    // Create an un-scaled image to draw everything at real size before scaling it to the screen.
    var dims = SpriteOptions.getScreenDimensions();
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage optionsImage = SpriteLibrary.createTransparentSprite(dims.width/drawScale, dims.height/drawScale);
    Graphics optionsGraphics = optionsImage.getGraphics();

    // Set up some initial parameters.
    int xDraw = (optionsImage.getWidth() / 2) - (template.graphicsOptionWidth / 2);
    int yDraw = template.graphicsOptionHeight;
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = (template.graphicsOptionHeight + (template.optionNamePanel.getHeight() / 2));

    // Loop through and draw everything.
    for( int i = 0; i < AudioEngine.allOptions.length; ++i, yDraw += ySpacing )
    {
      template.drawGameOption(optionsGraphics, xDraw, yDraw, AudioEngine.allOptions[i]);
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    yDraw = firstOptionY + (int) (ySpacing * AudioEngine.animHighlightedOption.get()) + (3); // +3 to center.
    xDraw += (template.graphicsOptionWidth - template.optionSettingPanel.getWidth() - 8); // Subtract 5 to center the arrows around the option setting panel.

    optionsGraphics.drawImage(template.optionArrows, xDraw, yDraw, template.optionArrows.getWidth(), template.optionArrows.getHeight(), null);

    // Redraw to the screen at scale.
    g.drawImage(optionsImage, 0, 0, optionsImage.getWidth()*drawScale, optionsImage.getHeight()*drawScale, null);
  }

}
