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
  private Color[] menuBGColors = {new Color(218,38,2), new Color(111,218,2), new Color(206,224,234)};
  int highestOption = menuBGColors.length - 1;
  int drawScale = 3;

  private Dimension dimensions = new Dimension(240*drawScale, 160*drawScale);
  private int optionSeparationX = dimensions.width / 6; // So we can evenly space the seven visible options.
  private int optionSeparationY = dimensions.height / 6;

  private double animHighlightedOption = 0;

  public SpriteMainUIView( MainUIController control )
  {
    controller = control;
    SpriteGameSetupMenuArtist.setDimensions(dimensions);
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return dimensions;
  }

  @Override
  public int getViewWidth()
  {
    return dimensions.width;
  }

  @Override
  public int getViewHeight()
  {
    return dimensions.height;
  }

  @Override
  public void render(Graphics g)
  {
    switch(controller.getSubMenuType())
    {
      case GAME_SETUP:
        SpriteGameSetupMenuArtist.draw(g, controller.getGameSetupController());
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
    int xCenter = getViewWidth() / 2;
    int yCenter = getViewHeight() / 2;

    // If we are moving from one highlighted option to another, calculate the intermediate draw location.
    if( animHighlightedOption != highlightedOption )
    {
      double slide = SpriteUIUtils.calculateSlideAmount(animHighlightedOption, highlightedOption);
      animHighlightedOption += slide;
    }

    // Figure out where to actually draw the currently-highlighted option. Note that this changes 
    //   immediately when up or down is pressed, and the new option becomes the basis for drawing.
    final int xBasisLoc = (int) (xCenter - (animHighlightedOption - highlightedOption) * optionSeparationX);
    final int yBasisLoc = (int) (yCenter - (animHighlightedOption - highlightedOption) * optionSeparationY);

    // Draw the center option, then the two adjacent, then the next two, until we are off the screen.
    int layer = 0; // We start by drawing all options 0 distance from the highlighted one.
    for(int drawY = yBasisLoc, drawX = xBasisLoc;
        // This check ensures that we keep going until we are off the visible screen.
        (drawY > 0 && drawY < getViewHeight());
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
    int frameWidth = getViewWidth();
    g.fillRect(0, 0,  frameWidth, 68*drawScale);
    g.fillRect(0, 70*drawScale, frameWidth, drawScale);
    g.fillRect(0, 89*drawScale, frameWidth, drawScale);
    g.fillRect(0, 92*drawScale, frameWidth, 68*drawScale);
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
    if( y > -1*menuText.getHeight() && y < getViewHeight() + menuText.getHeight())
    {
      SpriteLibrary.drawImageCenteredOnPoint(g, menuText, x, y, drawScale);
    }
  }
}
