package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Terrain.MapInfo;
import UI.GameOption;
import UI.GameOptionSetupController;
import UI.SlidingValue;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;

public class GameOptionSetupArtist
{
  private static GameOptionSetupController myControl = null;
  private static SlidingValue selectedGameOption = new SlidingValue(0);

  private static HorizontalSelectorTemplate template = new HorizontalSelectorTemplate();

  private static SlidingValue yDrawStart;

  private static void initialize()
  {
    GameOption<?>[] allOptions = myControl.gameOptions;

    int maxNameLen = 0;
    // Calculate the size of the longest option panel needed.
    for( int i = 0; i < allOptions.length; ++i )
    {
      if( allOptions[i].optionName.length() > maxNameLen )
      {
        maxNameLen = allOptions[i].optionName.length();
      }
    }
    int maxItemLen = 0;
    // Calculate the size of the longest item panel needed.
    for( int i = 0; i < allOptions.length; ++i )
    {
      ArrayList<?> allItems = allOptions[i].optionList;
      for( int j = 0; j < allItems.size(); ++j )
      {
        if( allItems.get(j).toString().length() > maxItemLen )
        {
          maxItemLen = allItems.get(j).toString().length();
        }
      }

      selectedGameOption.snap(myControl.optionSelector.getSelectionNormalized());
    }

    template.initialize(allOptions);
    yDrawStart = new SlidingValue(template.graphicsOptionHeight);
  }

  public static void draw(Graphics g, MapInfo selectedMapInfo, GameOptionSetupController control)
  {
    if(!control.inSubmenu())
    {
      // Control is local. We draw.
      drawGameOptions(g, control);
    }
    else
    {
      // Command resides in a sub-menu. Let it draw.
      PlayerSetupArtist.draw(g, selectedMapInfo, control.getSubController());
    }
  }

  public static void drawGameOptions(Graphics g, GameOptionSetupController control)
  {
    // Draw a fancy background.
    DiagonalBlindsBG.draw(g);

    // If control has changed, we just entered a new CO setup screen. We don't want to
    //   animate a menu transition based on the last time we were choosing COs, since
    //   this class is static, but the CO select screen is not.
    boolean reinit = myControl != control;
    myControl = control;

    // Create an un-scaled image to draw everything at real size before scaling it to the screen.
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage optionsImage = SpriteLibrary.createTransparentSprite(dimensions.width/drawScale, dimensions.height/drawScale);
    Graphics optionsGraphics = optionsImage.getGraphics();

    // Build the necessary images.
    if( reinit )
    {
      initialize();
    }

    // Set up some initial parameters.
    int xDraw = (optionsImage.getWidth() / 2) - (template.graphicsOptionWidth / 2);
    int yDraw = yDrawStart.geti();
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = (int)(template.graphicsOptionHeight * 1.5);

    // Loop through and draw everything.
    GameOption<?>[] allOptions = myControl.gameOptions;
    for( int i = 0; i < allOptions.length; ++i, yDraw += ySpacing )
    {
      template.drawGameOption(optionsGraphics, xDraw, yDraw, allOptions[i]);
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    selectedGameOption.set(myControl.optionSelector.getSelectionNormalized());
    yDraw = firstOptionY + (int) (ySpacing * selectedGameOption.get()) + (3); // +3 to center.
    xDraw += (template.graphicsOptionWidth - template.optionSettingPanel.getWidth() - 8); // Subtract 8 to center the arrows around the option setting panel.

    optionsGraphics.drawImage(template.optionArrows,
                              xDraw, yDraw,
                              template.optionArrows.getWidth(), template.optionArrows.getHeight(),
                              null);

    // Try to get the selected panel on-screen with a panel-height worth of buffer.
    int yDrawDest = yDrawStart.getDestination() + (int) (ySpacing * selectedGameOption.getDestination()) + (3);
    if( yDrawDest > optionsImage.getHeight() - template.graphicsOptionHeight )
    {
      int offset = -ySpacing + optionsImage.getHeight() - yDrawDest;
      yDrawStart.set(yDrawStart.geti() + offset);
    }
    if( yDrawDest < template.graphicsOptionHeight )
    {
      int offset = template.graphicsOptionHeight - yDrawDest;
      yDrawStart.set(yDrawStart.geti() + offset);
    }

    // Redraw to the screen at scale.
    g.drawImage(optionsImage, 0, 0, optionsImage.getWidth()*drawScale, optionsImage.getHeight()*drawScale, null);
  }

}
