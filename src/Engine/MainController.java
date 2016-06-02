package Engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Terrain.GameMap;
import UI.InputHandler;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteMapView;

public class MainController extends javax.swing.JPanel implements IController, IView
{
  private static final long serialVersionUID = 5548786952371603112L;

  private IController activeSubController;
  private IView activeSubView;

  private static final int NEW_GAME = 0;
  private static final int OPTIONS = 1;
  private static final int QUIT = 2;
  // This list of menu options is mirrored by the Sprite of option images we get from SpriteLibrary.
  private static final int[] menuOptions = {NEW_GAME, OPTIONS, QUIT};
  private int highlightedOption = 0;
  private int highestOption = menuOptions[menuOptions.length-1];
  
  private Color[] menuBGColors = {new Color(218,38,2), new Color(111,218,2), new Color(206,224,234)};

  private Dimension dimensions = new Dimension(240, 160);

  private int optionSeparationX = dimensions.width / 6; // So we can evenly space the five visible options.
  private int optionSeparationY = dimensions.height / 6;

  private double animHighlightedOption = 0;

  public MainController()
  {
    // Initialize member data.
    setPreferredSize(dimensions);
    
    // At game start, MainController will handle the inputs/rendering.
    activeSubView = null;
    activeSubController = null;
  }

  @Override
  public boolean handleInput(InputHandler.InputAction action)
  {
    boolean exitGame = false;

    // Route input to the correct module based on current game state.
    // If a state change is needed, handle that.
    if(null != activeSubController)
    {
      boolean done = activeSubController.handleInput(action);
      if(done)
      {
        activeSubController = null;
      }
    }
    else
    {
      // We are in the main game menu.
      switch( action )
      {
        case ENTER:
          switch( highlightedOption )
          {
            case NEW_GAME:
            // TODO: Move all this stuff where it belongs, whenever that exists.
            Commander co1 = new CmdrStrong();
            Commander co2 = new Commander();
            Commander[] cos = { co1, co2 };

            cos[0].myColor = Color.pink;
            cos[1].myColor = Color.cyan;
            GameMap map = new GameMap(cos);
            GameInstance newGame = new GameInstance(map, cos);

            SpriteMapView smv = new SpriteMapView(newGame);
            activeSubView = smv;
            setPreferredSize(activeSubView.getPreferredDimensions());
            MapController mapController = new MapController(newGame, smv);

            activeSubController = mapController;
            break;
            case OPTIONS:
              System.out.println("WARNING! Options menu not supported yet!");
              break;
            case QUIT:
              // Let the caller know we are done here.
              exitGame = true;
              break;
              default:
                System.out.println("WARNING! Invalid menu option chosen.");
          }
          break;
        case DOWN:
          highlightedOption--;
          // If we go outside the valid range, reset things to where they should be.
          if(highlightedOption < 0)
          {
            highlightedOption += menuOptions.length;
            animHighlightedOption += menuOptions.length;
          }
          break;
        case UP:
          highlightedOption++;
          // If we go outside the valid range, reset things to where they should be.
          if(highlightedOption > highestOption)
          {
            highlightedOption -= menuOptions.length;
            animHighlightedOption -= menuOptions.length;
          }
          break;
          default:
            // Other actions (LEFT, RIGHT, BACK) not supported in the main menu.
      }
    }

    return exitGame;
  }

  @Override
  public void paintComponent(Graphics g)
  {
    // Draw whatever should be drawn.
    if(null != activeSubView)
    {
      activeSubView.render(g);
    }
    else // We are in the main menu. Draw it.
    {
      render(g);
    }
  }

  ///////////////////////////////////////////////////////////////
  // IView methods
  ///////////////////////////////////////////////////////////////

  @Override
  public void render(Graphics g)
  {
    // Draw background.
    drawMenuBG(g);

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

  @Override
  public Dimension getPreferredDimensions()
  {
    if(null != activeSubView)
    {
      System.out.println("Size " + activeSubView.getPreferredDimensions().width + ", " + activeSubView.getPreferredDimensions().height);
      return activeSubView.getPreferredDimensions();
    }
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

  ///////////////////////////////////////////////////////////////
  // Helper functions
  ///////////////////////////////////////////////////////////////

  private void drawMenuBG(Graphics g)
  {
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
