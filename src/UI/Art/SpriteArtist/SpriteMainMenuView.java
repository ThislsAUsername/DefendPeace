package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.IView;
import Engine.MainController;

public class SpriteMainMenuView implements IView
{
  MainController controller = null;

  // Note that menuBGColors must be defined to match MainController.menuOptions.
  private Color[] menuBGColors = {new Color(218,38,2), new Color(111,218,2), new Color(206,224,234)};
  int highestOption = menuBGColors.length - 1;
  int drawScale = 1;

  private Dimension dimensions = new Dimension(240*drawScale, 160*drawScale);
  private int optionSeparationX = dimensions.width / 6; // So we can evenly space the seven visible options.
  private int optionSeparationY = dimensions.height / 6;

  private double animHighlightedOption = 0;

  public SpriteMainMenuView( MainController control )
  {
    controller = control;
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
    // Find out where we are.
    int highlightedOption = controller.getHighlightedOption();
    
    // Draw background.
    drawMenuBG(g, highlightedOption);

    // We start by assuming the highlighted option will be drawn centered.
    int xBasisLoc = getViewWidth() / 2;
    int yBasisLoc = getViewHeight() / 2;

    // If we are moving from one highlighted option to another, calculate the intermediate draw location.
    double slide = 0;
    if( animHighlightedOption != highlightedOption )
    {
      slide = calculateSlideAmount(animHighlightedOption, highlightedOption);
      animHighlightedOption += slide;
    }

    // Figure out where to actually draw the currently-highlighted option. Note that this changes 
    //   immediately when up or down is pressed, and the new option becomes the basis for drawing.
    xBasisLoc += (animHighlightedOption - highlightedOption) * optionSeparationX;
    yBasisLoc += (animHighlightedOption - highlightedOption) * optionSeparationY;

    // Draw the center option, then the two adjacent, then the next two, until we are off the screen.
    int layer = 0; // We start by drawing all options 0 distance from the highlighted one.
    for(int offsetY = yBasisLoc, offsetX = xBasisLoc;
        // Our check ensures that we keep going until we are off the visible screen.
        (yBasisLoc - Math.abs(offsetY) >= 0) || (yBasisLoc + Math.abs(offsetY) < 160);
        ++layer)
    {
      // Figure out how far from the basis to draw this option.
      offsetX = xBasisLoc + (layer * optionSeparationX);
      offsetY = yBasisLoc + (layer * optionSeparationY);

      drawMenuOption(g, highlightedOption - layer, offsetX, offsetY);
      
      // Draw the menu option equidistant from the highlighted option on the other side.
      if(layer > 0)
      {
        offsetX = xBasisLoc - (layer * optionSeparationX);
        offsetY = yBasisLoc - (layer * optionSeparationY);
        drawMenuOption(g, highlightedOption + layer, offsetX, offsetY);
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
    g.fillRect(0, 0,  240, 68);
    g.drawLine(0, 70, 240, 70);
    g.drawLine(0, 89, 240, 89);
    g.fillRect(0, 92, 240, 68);
  }

  /**
   * Calculate the distance to move the menu option images to make it look like the menu slides around instead of
   * just snapping at each button-press. Distance moved per frame is proportional to distance from goal location.
   * It is expected that currentNum and targetNum will not different by more than 1.0. Note that currentNum and 
   * targetNum correspond to (relative) positions, not to pixels, and the logic in this function is made accordingly.
   */
  private double calculateSlideAmount(double currentNum, int targetNum)
  {
    double animMoveFraction = 0.3; // Movement cap to prevent over-speedy menu movement.
    double animSnapDistance = 0.05; // Minimum distance at which point we just snap into place.
    double slide = 0; // Return value; the distance we actually are going to move.
    double diff = Math.abs(targetNum - currentNum);
    int sign = (targetNum > currentNum)?1:-1; // Since we took abs(), make sure we can correct the sign.
    if( diff < animSnapDistance )
    { // If we are close enough, just move the exact distance.
      slide = diff;
    }
    else
    { // Move a fixed fraction of the remaining distance.
      slide = diff * animMoveFraction;
    }
    
    return slide*sign;
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
      SpriteLibrary.drawImageCenteredOnPoint(g, menuText, x, y, 1);
    }
  }
}
