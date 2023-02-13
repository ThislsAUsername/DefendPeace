package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import Engine.ConfigUtils;
import Engine.Driver;
import Engine.OptionSelector;
import UI.GameOption;
import UI.GameOptionBool;
import UI.GameOptionInt;
import UI.InputHandler;
import UI.SlidingValue;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;

public class SpriteOptions
{
  // Define global settings.
  private static final int WINDOWWIDTH_DEFAULT = 240;
  private static final int WINDOWHEIGHT_DEFAULT = 160;
  private static final int DRAWSCALE_DEFAULT = 2;
  private static int drawScale = DRAWSCALE_DEFAULT;
  private static boolean animationsOn = true;

  public enum SelectedUnitThreatAreaMode
  {
    All, Current, Future, None;
    @Override
    public String toString()
    {
      return super.toString().replace("_", " ");
    }
  }

  private static Dimension dimensions = new Dimension(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);

  // Set up configurable options.
  private static GameOption<Integer> drawScaleOption = new GameOptionInt("Draw Scale", 1, 6, 1, DRAWSCALE_DEFAULT);
  private static GameOptionBool animationsOption = new GameOptionBool("Animations", true);
  private static GameOption<SelectedUnitThreatAreaMode> selectedUnitThreatModeOption
          = new GameOption<SelectedUnitThreatAreaMode>("Show selected unit threat", SelectedUnitThreatAreaMode.values(), 0);
  private static GameOption<?>[] allOptions = { drawScaleOption, animationsOption, selectedUnitThreatModeOption };
  private static OptionSelector highlightedOption = new OptionSelector(allOptions.length);
  private static SlidingValue animHighlightedOption;

  private static HorizontalSelectorTemplate template = new HorizontalSelectorTemplate();

  public static Dimension getScreenDimensions()
  {
    return dimensions;
  }

  public static void setScreenDimensions(int width, int height)
  {
    dimensions.setSize(width, height);
  }

  public static int getDrawScale()
  {
    return drawScale;
  }

  public static boolean getAnimationsEnabled()
  {
    return animationsOn;
  }

  public static SelectedUnitThreatAreaMode getSelectedUnitThreatAreaMode()
  {
    return selectedUnitThreatModeOption.getSelectedObject();
  }

  static void initialize()
  {
    template.initialize(allOptions);

    animHighlightedOption = new SlidingValue(0);
    // Load saved settings from disk, if they exist.
    loadSettingsFromDisk();
  }

  public static boolean handleOptionsInput(InputHandler.InputAction action)
  {
    boolean exitMenu = false;

    switch (action)
    {
      case SELECT:
        applyConfigOptions();
        break;
      case BACK:
        resetConfigOptions();
        exitMenu = true;
        break;
      case UP:
      case DOWN:
        highlightedOption.handleInput(action);
        animHighlightedOption.set(highlightedOption.getSelectionNormalized());
        break;
      case LEFT:
      case RIGHT:
        allOptions[highlightedOption.getSelectionNormalized()].handleInput(action);
        break;
      case SEEK:
      case VIEWMODE:
        break;
    }

    return exitMenu;
  }

  /**
   * Take the settings currently held in the ConfigOption objects and persist them
   * in the class data.
   */
  private static void applyConfigOptions()
  {
    // Persist the values in the GameOption objects.
    for( GameOption<?> go : allOptions )
    {
      go.storeCurrentValue();
    }

    // Store the options locally.
    drawScale = drawScaleOption.getSelectedObject();
    animationsOn = animationsOption.getSelectedObject();
    saveSettingsToDisk();

    // Apply effects.
    dimensions.setSize(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);
    Driver.getInstance().updateView(); // Tell the driver to look at these settings again.
  }

  /**
   * Set the config options to the values currently stored in the class data.
   */
  private static void resetConfigOptions()
  {
    for( GameOption<?> go : allOptions )
    {
      go.loseChanges();
    }

    highlightedOption.setSelectedOption(0);
    animHighlightedOption.set(0);
  }

  //////////////////////////////////////////////////////////////////////
  //  File utility functions.
  //////////////////////////////////////////////////////////////////////
  private static final String KEYS_FILENAME = "res/graphics_options.txt";

  private static void saveSettingsToDisk()
  {
    if( !ConfigUtils.writeConfigs(KEYS_FILENAME, Arrays.asList(allOptions)) )
      System.out.println("Unable to write graphics options to file.");
  }

  private static void loadSettingsFromDisk()
  {
    boolean allValid = ConfigUtils.readConfigs(KEYS_FILENAME, Arrays.asList(allOptions));
    if( !allValid )
      System.out.println("Unable to read all graphics options from file.");

    drawScale = drawScaleOption.getSelectedObject();
    dimensions.setSize(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);

    animationsOn = animationsOption.getSelectedObject();
  }

  //////////////////////////////////////////////////////////////////////
  //  Drawing code below.
  //////////////////////////////////////////////////////////////////////

  public static void draw(Graphics g)
  {
    // Draw a fancy background.
    DiagonalBlindsBG.draw(g);

    // Create an un-scaled image to draw everything at real size before scaling it to the screen.
    BufferedImage optionsImage = SpriteLibrary.createTransparentSprite(dimensions.width/drawScale, dimensions.height/drawScale);
    Graphics optionsGraphics = optionsImage.getGraphics();

    // Set up some initial parameters.
    int xDraw = (optionsImage.getWidth() / 2) - (template.graphicsOptionWidth / 2);
    int yDraw = template.graphicsOptionHeight;
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = (template.graphicsOptionHeight + (template.optionNamePanel.getHeight() / 2));

    // Loop through and draw everything.
    for( int i = 0; i < allOptions.length; ++i, yDraw += ySpacing )
    {
      template.drawGameOption(optionsGraphics, xDraw, yDraw, allOptions[i]);
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    yDraw = firstOptionY + (int) (ySpacing * animHighlightedOption.get()) + (3); // +3 to center.
    xDraw += (template.graphicsOptionWidth - template.optionSettingPanel.getWidth() - 8); // Subtract 5 to center the arrows around the option setting panel.

    optionsGraphics.drawImage(template.optionArrows, xDraw, yDraw, template.optionArrows.getWidth(), template.optionArrows.getHeight(), null);

    // Redraw to the screen at scale.
    g.drawImage(optionsImage, 0, 0, optionsImage.getWidth()*drawScale, optionsImage.getHeight()*drawScale, null);
  }

}
