package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.Driver;
import Engine.OptionSelector;
import UI.InputHandler;
import Units.Weapons.Weapon;

public class SpriteOptions
{
  // Define global settings.
  private static final int WINDOWWIDTH_DEFAULT = 240;
  private static final int WINDOWHEIGHT_DEFAULT = 160;
  private static final int DRAWSCALE_DEFAULT = 2;
  private static int drawScale = DRAWSCALE_DEFAULT;

  private static Dimension dimensions = new Dimension(WINDOWWIDTH_DEFAULT*drawScale, WINDOWHEIGHT_DEFAULT*drawScale);

  // Set up configurable options.
  private static GraphicsOption drawScaleOption = new GraphicsOption("Draw Scale", 1, 6, DRAWSCALE_DEFAULT);
  private static GraphicsOption dummyOption = new GraphicsOption("Dummy option");
  private static GraphicsOption damageStrategyOption = new GraphicsOption("Damage Strategy", 0, 2, 0);
  private static GraphicsOption[] allOptions = {drawScaleOption, dummyOption, damageStrategyOption};
  private static OptionSelector highlightedOption = new OptionSelector(allOptions.length);
  private static double animHighlightedOption = 0;

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

  public static int getDrawScale()
  {
    return drawScale;
  }

  private static void initialize()
  {
    int maxNameLen = 0;
    // Calculate the size of the longest option panel needed.
    for(int i = 0; i < allOptions.length; ++i)
    {
      if(allOptions[i].optionName.length() > maxNameLen)
      {
        maxNameLen = allOptions[i].optionName.length();
      }
    }

    // This panel will hold the name of the option.
    optionNamePanel = generateOptionPanel(maxNameLen, MENUBGCOLOR);
    // This panel will hold the current setting for the option.
    optionSettingPanel = generateOptionPanel(3, MENUBGCOLOR); // All settings fit within 3 characters.
    optionSettingPanelChanged = generateOptionPanel(3, MENUHIGHLIGHTCOLOR);

    graphicsOptionWidth = optionNamePanel.getWidth() + optionSettingPanel.getWidth()
        + letterWidth*3; // Plus some space for a buffer between panels.
    graphicsOptionHeight = optionNamePanel.getHeight();

    // Make points to define the two selection arrows.
    int[] lXPoints = {0, 5, 5, 0};
    int[] lYPoints = {4, -1, 11, 5};
    int[] rXPoints = {36, 41, 41, 36};
    int[] rYPoints = {-1, 4, 5, 10};
    // Build an image with the selection arrows.
    optionArrows = new BufferedImage(44, 10, BufferedImage.TYPE_INT_ARGB);
    Graphics ag = optionArrows.getGraphics();
    ag.setColor(MENUFRAMECOLOR);
    ag.fillPolygon(lXPoints, lYPoints, lXPoints.length);
    ag.fillPolygon(rXPoints, rYPoints, rXPoints.length);

    initialized = true;
  }

  /**
   * Build an image for a floating panel to hold the specified text length, and return it.
   * @param length The max text length intended to be shown on this panel.
   */
  private static BufferedImage generateOptionPanel(int length, Color fgColor)
  {
    int w = (2*textBuffer) + (letterWidth*length);
    int h = (textBuffer) + (SpriteLibrary.getLettersUppercase().getFrame(0).getHeight());
    int sh = 3; // Extra vertical space to fit in the shadow effect.
    int sw = 2;

    BufferedImage panel = new BufferedImage(w+sw, h+sh, BufferedImage.TYPE_INT_ARGB);

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

    switch(action)
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
        break;
      case LEFT:
      case RIGHT:
        allOptions[highlightedOption.getSelectionNormalized()].handleInput(action);
        break;
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
    drawScale = drawScaleOption.getSelectionNormalized();
    dimensions.setSize(WINDOWWIDTH_DEFAULT*drawScale, WINDOWHEIGHT_DEFAULT*drawScale);
    Weapon.currentStrategy = damageStrategyOption.getSelectionNormalized();
    Driver.getInstance().updateView(); // Tell the driver to look at these settings again.

    for( GraphicsOption go : allOptions )
    {
      go.storeCurrentValue();
    }
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
    animHighlightedOption = 0;
  }

  //////////////////////////////////////////////////////////////////////
  //  Drawing code below.
  //////////////////////////////////////////////////////////////////////

  public static void draw(Graphics g)
  {
    // Draw a fancy background.
    DiagonalBlindsBG.draw(g);

    // Build the necessary images.
    if(!initialized)
    {
      initialize();
    }
    
    // Set up some initial parameters.
    int xDraw = (dimensions.width / 2) - ((drawScale * graphicsOptionWidth) / 2);
    int yDraw = drawScale * graphicsOptionHeight;
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = drawScale * (graphicsOptionHeight + (optionNamePanel.getHeight() / 2));
    
    // Loop through and draw everything.
    for(int i = 0; i < allOptions.length; ++i, yDraw += ySpacing)
    {
      drawGraphicsOption(g, xDraw, yDraw, allOptions[i]);
    }

    // If we are moving from one option to another, calculate the intermediate draw location.
    if( animHighlightedOption != highlightedOption.getSelectionNormalized() )
    {
      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedOption, highlightedOption.getSelectionNormalized());
      animHighlightedOption += slide;
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    yDraw = firstOptionY + (int)(ySpacing * animHighlightedOption) + (3*drawScale); // +3 to center.
    xDraw += (graphicsOptionWidth - optionSettingPanel.getWidth() - 8)*drawScale; // Subtract 5 to center the arrows around the option setting panel.

    g.drawImage(optionArrows, xDraw, yDraw, optionArrows.getWidth()*drawScale, optionArrows.getHeight()*drawScale, null);
  }

  private static void drawGraphicsOption(Graphics g, int x, int y, GraphicsOption opt)
  {
    int drawBuffer = textBuffer * drawScale;

    // Draw the name panel and the name.
    g.drawImage(optionNamePanel, x, y, optionNamePanel.getWidth()*drawScale, optionNamePanel.getHeight()*drawScale, null);
    SpriteLibrary.drawText(g, opt.optionName, x+drawBuffer, y+drawBuffer, drawScale);
    
    // Draw the setting panel and the setting value.
    x = x + drawScale * (optionNamePanel.getWidth()+(3*letterWidth));
    BufferedImage settingPanel = (opt.isChanged()) ? optionSettingPanelChanged : optionSettingPanel;
    g.drawImage(settingPanel, x, y, settingPanel.getWidth()*drawScale, settingPanel.getHeight()*drawScale, null);
    SpriteLibrary.drawText(g, opt.getSettingValueText(), x+drawBuffer, y+drawBuffer, drawScale);
  }

  private static class GraphicsOption extends OptionSelector
  {
    public final String optionName;
    public final int minOption;
    private final boolean isBoolean;
    private int storedValue = 0;
    public GraphicsOption(String name, int min, int max, int defaultValue)
    {
      super(max - min);
      minOption = min;
      optionName = name;
      isBoolean = false;
      setSelectedOption(defaultValue);
      storedValue = defaultValue;
    }
    public GraphicsOption(String name)
    {
      super(2); // No min/max means this is a boolean choice.
      minOption = 0;
      optionName = name;
      isBoolean = true;
    }
    @Override
    public int getSelectionNormalized()
    {
      return super.getSelectionNormalized()+minOption;
    }
    @Override
    public void setSelectedOption(int value)
    {
      super.setSelectedOption(value-minOption);
    }
    public String getSettingValueText()
    {
      String text;
      if(isBoolean)
      {
        text = (getSelectionNormalized() == 1)?"On":"Off";
      }
      else
      {
        text = Integer.toString(getSelectionNormalized());
      }
      return text;
    }
    public void storeCurrentValue()
    {
      storedValue = getSelectionNormalized();
    }
    public boolean isChanged()
    {
      return ( storedValue != getSelectionNormalized() );
    }
    public void loseChanges()
    {
      setSelectedOption(storedValue);
    }
  }
}
