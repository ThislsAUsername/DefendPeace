package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import UI.MainUIController;

import Engine.IView;

/**
 * This class is responsible for drawing the main menu visible at game startup.
 */
public class SpriteMainUIView implements IView
{
  MainUIController controller = null;

  // Note that menuBGColors must be defined to match MainController.menuOptions.
  private Color[] menuBGColors = {new Color(218,38,2), new Color(30,218,2), new Color(206,224,234)};
  int highestOption = menuBGColors.length - 1;

  private double animHighlightedOption = 0;

  private int windowWidth;
  private int windowHeight;

  public SpriteMainUIView( MainUIController control )
  {
    controller = control;
    windowWidth = SpriteOptions.getScreenDimensions().width;
    windowHeight = SpriteOptions.getScreenDimensions().height;
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return SpriteOptions.getScreenDimensions();
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    windowWidth = width;
    windowHeight = height;
    SpriteOptions.setScreenDimensions(width, height);
  }

  @Override
  public void render(Graphics g)
  {
    switch(controller.getSubMenuType())
    {
      case GAME_SETUP:
        SpriteMapSelectMenuArtist.draw(g, controller.getGameSetupController());
        break;
      case MAIN:
        renderMainMenu(g);
        break;
      case OPTIONS:
        SpriteOptions.draw(g);
        break;
        default:
          System.out.println("Warning: Invalid menu type " + controller.getSubMenuType() + " in SpriteMainUIView.render().");
    }
  }

  private void renderMainMenu(Graphics g)
  {
    // Find out where we are in the absolute, to enable our fancy spinning animation.
    int highlightedOption = controller.getOptionSelector().getSelectionAbsolute();
    
    // Draw background.
    drawMenuBG(g, highlightedOption);

    // We start by assuming the highlighted option will be drawn centered.
    int xCenter = windowWidth / 2;
    int yCenter = 80*SpriteOptions.getDrawScale();

    // If we are moving from one highlighted option to another, calculate the intermediate draw location.
    if( animHighlightedOption != highlightedOption )
    {
      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedOption, highlightedOption);
      animHighlightedOption += slide;
    }

    int optionSeparationX = windowWidth / 6; // So we can evenly space the seven visible options.
    int optionSeparationY = windowHeight / 6;

    // Figure out where to actually draw the currently-highlighted option. Note that this changes 
    //   immediately when up or down is pressed, and the new option becomes the basis for drawing.
    final int xBasisLoc = (int) (xCenter - (animHighlightedOption - highlightedOption) * optionSeparationX);
    final int yBasisLoc = (int) (yCenter - (animHighlightedOption - highlightedOption) * optionSeparationY);

    // Draw the center option, then the two adjacent, then the next two, until we are off the screen.
    int layer = 0; // We start by drawing all options 0 distance from the highlighted one.
    for(int drawY = yBasisLoc, drawX = xBasisLoc;
        // This check ensures that we keep going until we are off the visible screen.
        layer < 4; // Each 'layer' is two menu options; one above and one below the currently-drawn ones.
        ++layer)
    {
      // Figure out how far from the basis to draw this option.
      drawX = xBasisLoc + (layer * optionSeparationX);
      drawY = yBasisLoc + (layer * optionSeparationY);

      drawMenuOption(g, highlightedOption + layer, drawX, drawY);

      // Draw the menu option equidistant from the highlighted option on the other side.
      if(layer > 0)
      {
        drawX = xBasisLoc - (layer * optionSeparationX);
        drawY = yBasisLoc - (layer * optionSeparationY);
        drawMenuOption(g, highlightedOption - layer, drawX, drawY);
      }
    }
  }

  ///////////////////////////////////////////////////////////////
  // Helper functions
  ///////////////////////////////////////////////////////////////

  private void drawMenuBG(Graphics g, int highlightedOption)
  {
    // Normalize this value so we don't get an out of bounds exception.
    for(;highlightedOption < 0; highlightedOption += menuBGColors.length);
    for(;highlightedOption > highestOption; highlightedOption -= menuBGColors.length);

    // Get the background color for this option and draw our fancy pattern.
    Color drawColor = menuBGColors[highlightedOption];
    g.setColor(drawColor);
    int frameWidth = windowWidth;
    int drawScale = SpriteOptions.getDrawScale();
    g.fillRect(0, 0,  frameWidth, 68*drawScale);
    g.fillRect(0, 70*drawScale, frameWidth, drawScale);
    g.fillRect(0, 89*drawScale, frameWidth, drawScale);
    g.fillRect(0, 92*drawScale, frameWidth, windowHeight - (92*drawScale));
  }

  /**
   * Look up the sprite image associated with the provided option, and draw it centered around x and y.
   */
  private void drawMenuOption(Graphics g, int option, int x, int y)
  {
    // Get the correct image from SpriteLibrary, and then hand it back for drawing on location.
    // We don't need to bother normalizing option since Sprite.getFrame() handles that automagically.
    BufferedImage menuText = SpriteLibrary.getMainMenuOptions().getFrame(option);

    // Only draw the image if it will actually show on the screen.
    if( y > -1*menuText.getHeight() && y < windowHeight + menuText.getHeight())
    {
      SpriteLibrary.drawImageCenteredOnPoint(g, menuText, x, y, SpriteOptions.getDrawScale());
    }
  }
}
