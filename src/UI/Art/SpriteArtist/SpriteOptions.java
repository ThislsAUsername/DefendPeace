package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

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
  private static boolean coordinatesOn = false;

  private static Dimension dimensions = new Dimension(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);

  // Set up configurable options.
  private static GameOption<Integer> drawScaleOption = new GameOptionInt("Draw Scale", 1, 6, 1, DRAWSCALE_DEFAULT);
  private static GameOptionBool animationsOption = new GameOptionBool("Animations", true);
  private static GameOptionBool coordinatesOption = new GameOptionBool("Show Coords", false);
  private static GameOption<?>[] allOptions = { drawScaleOption, animationsOption, coordinatesOption };
  private static OptionSelector highlightedOption = new OptionSelector(allOptions.length);
  private static SlidingValue animHighlightedOption;

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

  public static boolean getCoordinatesEnabled()
  {
    return coordinatesOn;
  }

  static void initialize()
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
      ArrayList<?> allItems = allOptions[i].optionList;
      for( int j = 0; j < allItems.size(); ++j )
      {
        if( allItems.get(j).toString().length() > maxItemLen )
        {
          maxItemLen = allItems.get(j).toString().length();
        }
      }
    }

    // This panel will hold the name of the option.
    optionNamePanel = generateOptionPanel(maxNameLen, SpriteUIUtils.MENUBGCOLOR);
    // This panel will hold the current setting for the option.
    optionSettingPanel = generateOptionPanel(maxItemLen, SpriteUIUtils.MENUBGCOLOR);
    optionSettingPanelChanged = generateOptionPanel(maxItemLen, SpriteUIUtils.MENUHIGHLIGHTCOLOR);
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
    ag.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    ag.fillPolygon(lXPoints, lYPoints, lXPoints.length);
    ag.fillPolygon(rXPoints, rYPoints, rXPoints.length);

    animHighlightedOption = new SlidingValue(0);

    // Load saved settings from disk, if they exist.
    loadSettingsFromDisk();
  }

  /**
   * Build an image for a floating panel to hold the specified text length, and return it.
   * @param length The max text length intended to be shown on this panel.
   */
  static BufferedImage generateOptionPanel(int length, Color fgColor)
  {
    int w = (2 * textBuffer) + (letterWidth * length);
    int h = (textBuffer) + (SpriteLibrary.getLettersUppercase().getFrame(0).getHeight());
    int sh = 3; // Extra vertical space to fit in the shadow effect.
    int sw = 2;

    BufferedImage panel = new BufferedImage(w + sw, h + sh, BufferedImage.TYPE_INT_ARGB);

    Graphics g = panel.getGraphics();

    // Draw the shadow.
    g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
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
    coordinatesOn = coordinatesOption.getSelectedObject();
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
  private static final String DRAWSCALE_KEY = "Drawscale";
  private static final String ANIMATION_KEY = "Animation";
  private static final String COORDINATES_KEY = "ShowCoords";

  private static void saveSettingsToDisk()
  {
    try
    {
      File keyFile = new File(KEYS_FILENAME);
      FileWriter writer = new FileWriter(keyFile, false);

      StringBuffer buf = new StringBuffer();
      buf.append(DRAWSCALE_KEY).append(" ").append(drawScaleOption.getSelectionNormalized()+1).append("\n");
      buf.append(ANIMATION_KEY).append(" ").append(animationsOption.getSelectionNormalized()).append("\n");
      buf.append(COORDINATES_KEY).append(" ").append(coordinatesOption.getSelectionNormalized()).append("\n");
      writer.write(buf.toString());
      writer.close();
    }
    catch( IOException ioe )
    {
      System.out.println("Error! Failed to save graphics settings file!.\n  " + ioe.toString());
    }
  }

  private static void loadSettingsFromDisk()
  {
    // Load keys file if it exists
    File keyFile = new File(KEYS_FILENAME);
    if( keyFile.exists() )
    {
      try
      {
        Scanner scanner = new Scanner(keyFile);
        while(scanner.hasNextLine())
        {
          Scanner linescan = new Scanner(scanner.nextLine());
          String key = linescan.next();

          switch(key)
          {
            case DRAWSCALE_KEY:
              drawScaleOption.setSelectedOption(Integer.parseInt(linescan.next())-1);
              drawScale = drawScaleOption.getSelectedObject();
              dimensions.setSize(WINDOWWIDTH_DEFAULT * drawScale, WINDOWHEIGHT_DEFAULT * drawScale);
              break;
            case ANIMATION_KEY:
              animationsOption.setSelectedOption(Integer.parseInt(linescan.next()));
              animationsOn = animationsOption.getSelectedObject();
              break;
            case COORDINATES_KEY:
              coordinatesOption.setSelectedOption(Integer.parseInt(linescan.next()));
              coordinatesOn = coordinatesOption.getSelectedObject();
              break;
              default:
                System.out.println("WARNING! Unrecognized key '" + key + "' in graphics settings file!");
          }

          linescan.close();
        }
        scanner.close();
      }
      catch(FileNotFoundException fnfe)
      {
        System.out.println("Somehow we failed to find the keys file after checking that it exists! Using defaults.");
      }
      catch( InputMismatchException ime )
      {
        System.out.println("Encountered an error while parsing keys file! Using defaults.");
      }
    } // ~if file exists
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
    int xDraw = (optionsImage.getWidth() / 2) - (graphicsOptionWidth / 2);
    int yDraw = graphicsOptionHeight;
    int firstOptionY = yDraw; // Hold onto this to draw the selector arrows.
    int ySpacing = (graphicsOptionHeight + (optionNamePanel.getHeight() / 2));

    // Loop through and draw everything.
    for( int i = 0; i < allOptions.length; ++i, yDraw += ySpacing )
    {
      drawGameOption(optionsGraphics, xDraw, yDraw, allOptions[i]);
    }

    // Draw the arrows around the highlighted option, animating movement when switching.
    yDraw = firstOptionY + (int) (ySpacing * animHighlightedOption.get()) + (3); // +3 to center.
    xDraw += (graphicsOptionWidth - optionSettingPanel.getWidth() - 8); // Subtract 5 to center the arrows around the option setting panel.

    optionsGraphics.drawImage(optionArrows, xDraw, yDraw, optionArrows.getWidth(), optionArrows.getHeight(), null);

    // Redraw to the screen at scale.
    g.drawImage(optionsImage, 0, 0, optionsImage.getWidth()*drawScale, optionsImage.getHeight()*drawScale, null);
  }

  static void drawGameOption(Graphics g, int x, int y, GameOption<?> opt)
  {
    int drawBuffer = textBuffer;

    // Draw the name panel and the name.
    g.drawImage(optionNamePanel, x, y, optionNamePanel.getWidth(), optionNamePanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.optionName, x + drawBuffer, y + drawBuffer);

    // Draw the setting panel and the setting value.
    x = x + (optionNamePanel.getWidth() + (3 * letterWidth));
    BufferedImage settingPanel = (opt.isChanged()) ? optionSettingPanelChanged : optionSettingPanel;
    g.drawImage(settingPanel, x, y, settingPanel.getWidth(), settingPanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.getCurrentValueText(), x + drawBuffer, y + drawBuffer);
  }
}
