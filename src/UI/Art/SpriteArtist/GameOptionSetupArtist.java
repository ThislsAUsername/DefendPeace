package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Terrain.MapInfo;
import UI.GameOption;
import UI.GameOptionSetupController;
import UI.SlidingValue;

public class GameOptionSetupArtist
{
  private static GameOptionSetupController myControl = null;
  private static SlidingValue selectedGameOption = new SlidingValue(0);

  private static int graphicsOptionWidth = 0; // Set in initialize().
  private static int graphicsOptionHeight = 0; // Set in initialize().
  private static BufferedImage optionNamePanel = null;
  private static BufferedImage optionSettingPanel = null;
  private static BufferedImage optionArrows = null;

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

    // This panel will hold the name of the option.
    optionNamePanel = SpriteOptions.generateOptionPanel(maxNameLen, SpriteUIUtils.MENUBGCOLOR);
    // This panel will hold the current setting for the option.
    optionSettingPanel = SpriteOptions.generateOptionPanel(maxItemLen, SpriteUIUtils.MENUBGCOLOR);
    int letterWidth = SpriteLibrary.getLettersUppercase().getFrame(0).getWidth();
    int itemWidth = optionSettingPanel.getWidth() + letterWidth * 2; // dual-purpose buffer, also used for the switching arrows

    graphicsOptionWidth = optionNamePanel.getWidth() + itemWidth + letterWidth; // Plus some space for a buffer between panels.
    graphicsOptionHeight = optionNamePanel.getHeight();

    // Make points to define the two selection arrows.
    int[] lXPoints = { 0, 5, 5, 0 };
    int[] lYPoints = { 4, -1, 11, 5 };
    int[] rXPoints = { itemWidth, itemWidth+5, itemWidth+5, itemWidth };
    int[] rYPoints = { -1, 4, 5, 10 };
    // Build an image with the selection arrows.
    optionArrows = new BufferedImage(itemWidth+7, 10, BufferedImage.TYPE_INT_ARGB);
    Graphics ag = optionArrows.getGraphics();
    ag.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    ag.fillPolygon(lXPoints, lYPoints, lXPoints.length);
    ag.fillPolygon(rXPoints, rYPoints, rXPoints.length);
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
    int xDraw = (optionsImage.getWidth() / 2) - (graphicsOptionWidth / 2);
    int yDraw = graphicsOptionHeight;
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = (graphicsOptionHeight + (optionNamePanel.getHeight() / 2));

    // Loop through and draw everything.
    GameOption<?>[] allOptions = myControl.gameOptions;
    for( int i = 0; i < allOptions.length; ++i, yDraw += ySpacing )
    {
      drawGameOption(optionsGraphics, xDraw, yDraw, allOptions[i]);
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    selectedGameOption.set(myControl.optionSelector.getSelectionNormalized());
    yDraw = firstOptionY + (int) (ySpacing * selectedGameOption.get()) + (3); // +3 to center.
    xDraw += (graphicsOptionWidth - optionSettingPanel.getWidth() - 8); // Subtract 8 to center the arrows around the option setting panel.

    optionsGraphics.drawImage(optionArrows, xDraw, yDraw, optionArrows.getWidth(), optionArrows.getHeight(), null);

    // Redraw to the screen at scale.
    g.drawImage(optionsImage, 0, 0, optionsImage.getWidth()*drawScale, optionsImage.getHeight()*drawScale, null);
  }

  static void drawGameOption(Graphics g, int x, int y, GameOption<?> opt)
  {
    int textBuffer = 4;

    // Draw the name panel and the name.
    g.drawImage(optionNamePanel, x, y, optionNamePanel.getWidth(), optionNamePanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.optionName, x + textBuffer, y + textBuffer);

    // Draw the setting panel and the setting value.
    x = x + (optionNamePanel.getWidth() + (3 * SpriteLibrary.getLettersLowercase().getFrame(0).getWidth()));
    BufferedImage settingPanel = (opt.isChanged()) ? optionSettingPanel : optionSettingPanel;
    g.drawImage(settingPanel, x, y, settingPanel.getWidth(), settingPanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.getCurrentValueText(), x + textBuffer, y + textBuffer);
  }
}
