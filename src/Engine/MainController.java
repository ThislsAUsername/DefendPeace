package Engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Terrain.GameMap;
import UI.InputHandler;
import UI.Art.SpriteArtist.SpriteMainMenuView;
import UI.Art.SpriteArtist.SpriteMapView;

public class MainController extends javax.swing.JPanel implements IController
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

  private Dimension dimensions = new Dimension(240, 160);

  public MainController()
  {
    // Initialize member data.
    setPreferredSize(dimensions);
    
    // At game start, MainController will handle the inputs/rendering.
    activeSubView = new SpriteMainMenuView(this);
    activeSubController = null;
  }

  public int getHighlightedOption()
  {
    return highlightedOption;
  }

  @Override // From IController
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
          // Grab the current option and roll it back to within the valid range, then evaluate.
          int chosenOption = highlightedOption;
          for(;chosenOption < 0; chosenOption += menuOptions.length);
          for(;chosenOption > highestOption; chosenOption -= menuOptions.length);

          switch( chosenOption )
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
          // Worry about normalizing when we actually hit enter.
          highlightedOption--;
          break;
        case UP:
          // Worry about normalizing when we actually hit enter.
          highlightedOption++;
          break;
          default:
            // Other actions (LEFT, RIGHT, BACK) not supported in the main menu.
      }
    }

    return exitGame;
  }

  @Override // from JComponent
  public void paintComponent(Graphics g)
  {
    // Draw whatever should be drawn.
    activeSubView.render(g);
  }

  @Override // from JComponent
  public Dimension getPreferredSize()
  {
    if(null != activeSubView)
    {
      System.out.println("Size " + activeSubView.getPreferredDimensions().width + ", " + activeSubView.getPreferredDimensions().height);
      return activeSubView.getPreferredDimensions();
    }
    return dimensions;
  }
}
