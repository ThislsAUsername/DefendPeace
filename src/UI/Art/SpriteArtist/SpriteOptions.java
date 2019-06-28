package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.Driver;
import Engine.OptionSelector;
import UI.InputHandler;
import UI.SlidingValue;
import Units.Weapons.Weapon;

public class SpriteOptions
{
  // Define global settings.
  private static final int WINDOWWIDTH_DEFAULT = 240;
  private static final int WINDOWHEIGHT_DEFAULT = 160;
  private static final int DRAWSCALE_DEFAULT = 2;
  private static int drawScale = DRAWSCALE_DEFAULT;
  private static boolean animationsOn = true;

  private static Dimension dimensions = new Dimension(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);

  // Set up configurable options.
  private static GraphicsOption drawScaleOption = new GraphicsOption("Draw Scale", 1, 6, DRAWSCALE_DEFAULT);
  private static GraphicsOption animationsOption = new GraphicsOption("Animations", true);
  private static GraphicsOption damageSystemOption = new GraphicsOption("Damage System", Weapon.stratDescriptions, 0);
  private static GraphicsOption[] allOptions = { drawScaleOption, animationsOption, damageSystemOption };
  private static OptionSelector highlightedOption = new OptionSelector(allOptions.length);
  private static SlidingValue animHighlightedOption;

  private static final Color MENUFRAMECOLOR = new Color(169, 118, 65);
  private static final Color MENUBGCOLOR = new Color(234, 204, 154);
  private static final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

  private static boolean initialized = false;
  private static int letterWidth = SpriteLibrary.getLettersUppercase().getFrame(0).getWidth();
  private static int textBuffer = 4;
  private static int graphicsOptionWidth = 0; // Set in initialize().
  private static int graphicsOptionHeight = 0; // Set in initialize().
  private static BufferedImage optionNamePanel = null;
  private static BufferedImage optionSettingPanel = null;
  private static BufferedImage optionSettingPanelChanged = null;
  private static BufferedImage optionArrows = null;

  public static Dimension getScreenDimensions()
  {
    return dimensions;
  }

  public static void setScreenDimensions(int width, int height)
  {
    dimensions.setSize(width, height);
    Driver.getInstance().updateView(); // Tell the driver to look at these settings again.
  }

  public static int getDrawScale()
  {
    return drawScale;
  }

  public static boolean getAnimationsEnabled()
  {
    return animationsOn;
  }

  private static void initialize()
  {
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
      String[] allItems = allOptions[i].optionList;
      for( int j = 0; j < allItems.length; ++j )
      {
        if( allItems[j].length() > maxItemLen )
        {
          maxItemLen = allItems[j].length();
        }
      }
    }

    // This panel will hold the name of the option.
    optionNamePanel = generateOptionPanel(maxNameLen, MENUBGCOLOR);
    // This panel will hold the current setting for the option.
    optionSettingPanel = generateOptionPanel(maxItemLen, MENUBGCOLOR);
    optionSettingPanelChanged = generateOptionPanel(maxItemLen, MENUHIGHLIGHTCOLOR);
    int itemWidth = optionSettingPanel.getWidth()+ letterWidth * 2; // dual purpose buffer, also used for the switching arrows

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
    ag.setColor(MENUFRAMECOLOR);
    ag.fillPolygon(lXPoints, lYPoints, lXPoints.length);
    ag.fillPolygon(rXPoints, rYPoints, rXPoints.length);

    animHighlightedOption = new SlidingValue(0);

    initialized = true;
  }

  /**
   * Build an image for a floating panel to hold the specified text length, and return it.
   * @param length The max text length intended to be shown on this panel.
   */
  private static BufferedImage generateOptionPanel(int length, Color fgColor)
  {
    int w = (2 * textBuffer) + (letterWidth * length);
    int h = (textBuffer) + (SpriteLibrary.getLettersUppercase().getFrame(0).getHeight());
    int sh = 3; // Extra vertical space to fit in the shadow effect.
    int sw = 2;

    BufferedImage panel = new BufferedImage(w + sw, h + sh, BufferedImage.TYPE_INT_ARGB);

    Graphics g = panel.getGraphics();

    // Draw the shadow.
    g.setColor(MENUFRAMECOLOR);
    g.fillRect(sw, sh, w, h);

    // Draw the writing surface.
    g.setColor(fgColor);
    g.fillRect(0, 0, w, h);

    return panel;
  }

  public static boolean handleOptionsInput(InputHandler.InputAction action)
  {
    boolean exitMenu = false;

    switch (action)
    {
      case ENTER:
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
      case NO_ACTION:
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
    // Persist the values in the GraphicsOption objects.
    for( GraphicsOption go : allOptions )
    {
      go.storeCurrentValue();
    }

    // Store the options locally.
    drawScale = drawScaleOption.getSelectionNormalized();
    animationsOn = animationsOption.getSelectionNormalized() != 0;
    Weapon.currentStrategy = damageSystemOption.getSelectionNormalized();

    // Apply effects.
    dimensions.setSize(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);
    Driver.getInstance().updateView(); // Tell the driver to look at these settings again.
  }

  /**
   * Set the config options to the values currently stored in the class data.
   */
  private static void resetConfigOptions()
  {
    for( GraphicsOption go : allOptions )
    {
      go.loseChanges();
    }

    highlightedOption.setSelectedOption(0);
    animHighlightedOption.set(0);
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

    // Build the necessary images.
    if( !initialized )
    {
      initialize();
    }

    // Set up some initial parameters.
    int xDraw = (optionsImage.getWidth() / 2) - (graphicsOptionWidth / 2);
    int yDraw = graphicsOptionHeight;
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = (graphicsOptionHeight + (optionNamePanel.getHeight() / 2));

    // Loop through and draw everything.
    for( int i = 0; i < allOptions.length; ++i, yDraw += ySpacing )
    {
      drawGraphicsOption(optionsGraphics, xDraw, yDraw, allOptions[i]);
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    yDraw = firstOptionY + (int) (ySpacing * animHighlightedOption.get()) + (3); // +3 to center.
    xDraw += (graphicsOptionWidth - optionSettingPanel.getWidth() - 8); // Subtract 5 to center the arrows around the option setting panel.

    optionsGraphics.drawImage(optionArrows, xDraw, yDraw, optionArrows.getWidth(), optionArrows.getHeight(), null);

    // Redraw to the screen at scale.
    g.drawImage(optionsImage, 0, 0, optionsImage.getWidth()*drawScale, optionsImage.getHeight()*drawScale, null);
  }

  private static void drawGraphicsOption(Graphics g, int x, int y, GraphicsOption opt)
  {
    int drawBuffer = textBuffer;

    // Draw the name panel and the name.
    g.drawImage(optionNamePanel, x, y, optionNamePanel.getWidth(), optionNamePanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.optionName, x + drawBuffer, y + drawBuffer);

    // Draw the setting panel and the setting value.
    x = x + (optionNamePanel.getWidth() + (3 * letterWidth));
    BufferedImage settingPanel = (opt.isChanged()) ? optionSettingPanelChanged : optionSettingPanel;
    g.drawImage(settingPanel, x, y, settingPanel.getWidth(), settingPanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.getSettingValueText(), x + drawBuffer, y + drawBuffer);
  }

  private static class GraphicsOption extends OptionSelector
  {
    public final String optionName;
    public final int minOption;
    public final String[] optionList;
    private int storedValue = 0;

    public GraphicsOption(String name, String[] Options, int defaultValue)
    {
      super(Options.length);
      minOption = 0;
      optionName = name;
      setSelectedOption(defaultValue);
      optionList = Options;
    }
    public GraphicsOption(String name, int min, int max, int defaultValue)
    {
      super(max - min);
      minOption = min;
      optionName = name;
      setSelectedOption(defaultValue);
      optionList = new String[1 + max - min];
      for( int i = 0; i <= max - min; i++ )
      {
        optionList[i] = "" + (min + i);
      }
    }
    public GraphicsOption(String name, boolean defaultValue)
    {
      super(2); // No min/max means this is a boolean choice.
      minOption = 0;
      optionName = name;
      optionList = new String[] { "Off", "On" };
      if( defaultValue ) setSelectedOption(1);
      storeCurrentValue();
    }
    @Override
    public int getSelectionNormalized()
    {
      return super.getSelectionNormalized() + minOption;
    }
    @Override
    public void setSelectedOption(int value)
    {
      super.setSelectedOption(value - minOption);
      storedValue = getSelectionNormalized();
    }
    public String getSettingValueText()
    {
      return optionList[getSelectionNormalized()-minOption];
    }
    public void storeCurrentValue()
    {
      storedValue = getSelectionNormalized();
    }
    public boolean isChanged()
    {
      return (storedValue != getSelectionNormalized());
    }
    public void loseChanges()
    {
      setSelectedOption(storedValue);
    }
  }
}
