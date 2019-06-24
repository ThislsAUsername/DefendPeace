package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.IView;
import UI.InGameMenu;
import UI.MainUIController;
import UI.MainUIController.SaveInfo;
import UI.SlidingValue;

/**
 * This class is responsible for drawing the main menu visible at game startup.
 */
public class MainUIView implements IView
{
  MainUIController controller = null;

  // Note that menuBGColors must be defined to match MainController.menuOptions.
  private Color[] menuBGColors = {new Color(218,38,2), new Color(0,155,211), new Color(30,218,2), new Color(206,224,234)};
  int highestOption = menuBGColors.length - 1;

  private SlidingValue animHighlightedOption = new SlidingValue(0);

  private int menuWidth;
  private int menuHeight;

  public MainUIView( MainUIController control )
  {
    controller = control;
    menuWidth = SpriteOptions.getScreenDimensions().width / SpriteOptions.getDrawScale();
    menuHeight = SpriteOptions.getScreenDimensions().height / SpriteOptions.getDrawScale();
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return SpriteOptions.getScreenDimensions();
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    menuWidth = width / SpriteOptions.getDrawScale();
    menuHeight = height / SpriteOptions.getDrawScale();
    SpriteOptions.setScreenDimensions(width, height);
  }

  @Override
  public void render(Graphics g)
  {
    switch(controller.getSubMenuType())
    {
      case GAME_SETUP:
        MapSelectMenuArtist.draw(g, controller.getGameSetupController());
        break;
      case MAIN:
      case SAVE_SELECT:
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
    
    BufferedImage menuImage = SpriteLibrary.createTransparentSprite(menuWidth, menuHeight);
    int menuWidth = menuImage.getWidth();
    int menuHeight = menuImage.getHeight();
    Graphics menuGraphics = menuImage.getGraphics();

    // Draw background.
    drawMenuBG(menuGraphics, highlightedOption);

    // We start by assuming the highlighted option will be drawn centered.
    int xCenter = menuWidth / 2;
    int yCenter = menuHeight / 2;

    // If we are moving from one highlighted option to another, calculate the intermediate draw location.
    animHighlightedOption.set(highlightedOption);

    int optionSeparationX = menuWidth / 6; // So we can evenly space the seven visible options.
    int optionSeparationY = menuHeight / 6;

    // Figure out where to actually draw the currently-highlighted option. Note that this changes 
    //   immediately when up or down is pressed, and the new option becomes the basis for drawing.
    final int xBasisLoc = (int) (xCenter - (animHighlightedOption.get() - animHighlightedOption.getDestination()) * optionSeparationX);
    final int yBasisLoc = (int) (yCenter - (animHighlightedOption.get() - animHighlightedOption.getDestination()) * optionSeparationY);

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

      drawMenuOption(menuGraphics, highlightedOption + layer, drawX, drawY);

      // Draw the menu option equidistant from the highlighted option on the other side.
      if(layer > 0)
      {
        drawX = xBasisLoc - (layer * optionSeparationX);
        drawY = yBasisLoc - (layer * optionSeparationY);
        drawMenuOption(menuGraphics, highlightedOption - layer, drawX, drawY);
      }
    }
    
    if (null != controller.saveMenu) // If we've got a save menu on hand, draw it.
    {
      InGameMenu<SaveInfo> sm = controller.saveMenu;
      BufferedImage savesImage = SpriteUIUtils.makeTextMenu(sm.getAllOptions(), sm.getSelectionNumber(), 3, 4);
      SpriteUIUtils.drawImageCenteredOnPoint(menuGraphics, savesImage, xCenter, yCenter);
    }

    // Draw the composited image to the window.
    int drawScale = SpriteOptions.getDrawScale();
    g.drawImage(menuImage, 0, 0, menuImage.getWidth()*drawScale, menuImage.getHeight()*drawScale, null);
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
    int bgHeight = SpriteOptions.getScreenDimensions().height / SpriteOptions.getDrawScale();
    int yCenter = bgHeight / 2;
    int halfTextFrameHeight = (SpriteLibrary.getMainMenuOptions().getFrame(0).getHeight() + 4)/2;

    Color drawColor = menuBGColors[highlightedOption];
    g.setColor(drawColor);
    int frameWidth = menuWidth;
    g.fillRect(0, 0,  frameWidth, yCenter - halfTextFrameHeight - 3);
    g.fillRect(0, yCenter - halfTextFrameHeight - 1, frameWidth, 1);
    g.fillRect(0, yCenter + halfTextFrameHeight + 1, frameWidth, 1);
    g.fillRect(0, yCenter + halfTextFrameHeight + 4, frameWidth, bgHeight - (yCenter + halfTextFrameHeight));
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
    if( y > -1*menuText.getHeight() && y < menuHeight + menuText.getHeight())
    {
      SpriteUIUtils.drawImageCenteredOnPoint(g, menuText, x, y);
    }
  }

  @Override
  public void cleanup()
  {
    // No cleanup required.
  }
}
