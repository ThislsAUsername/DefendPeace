package Engine;

import java.awt.Color;

import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Terrain.GameMap;
import Terrain.MapLibrary;
import UI.InputHandler;
import UI.Art.SpriteArtist.SpriteMapView;

public class MainUIController implements IController
{
  private static final long serialVersionUID = 5548786952371603112L;

  public enum SubMenu { MAIN, GAME_SETUP, OPTIONS };
  private SubMenu currentSubMenuType = SubMenu.MAIN;

  // NOTE: This list of menu options is mirrored by the Sprite of option images we get from SpriteLibrary.
  final int NEW_GAME = 0;
  final int OPTIONS = 1;
  final int QUIT = 2;
  final int numMenuOptions = 3;

  private OptionSelector optionSelector = new OptionSelector(numMenuOptions);;

  public SubMenu getSubMenuType()
  {
    return currentSubMenuType;
  }

  public OptionSelector getOptionSelector()
  {
    return optionSelector;
  }

  @Override // From IController
  public boolean handleInput(InputHandler.InputAction action)
  {
    boolean exitGame = false;

    switch( currentSubMenuType )
    {
      case GAME_SETUP:
        exitGame = handleGameSetupMenuInput(action);
        if(exitGame)
        {
          // If the subMenu was not MAIN, we go back to MAIN.
          currentSubMenuType = SubMenu.MAIN;
          optionSelector.reset(numMenuOptions);
          exitGame = false;
        }
        break;
      case MAIN:
        exitGame = handleMainMenuInput(action);
        break;
      case OPTIONS:
        //exitGame = handleOptionsMenu(action);
        if(exitGame)
        {
          // If the subMenu was not MAIN, we go back to MAIN.
          currentSubMenuType = SubMenu.MAIN;
          optionSelector.reset(numMenuOptions);
          exitGame = false;
        }
        break;
      default:
        System.out.println("Warning: Invalid input " + action + " in MainUIController.");
    }

    return exitGame;
  }

  private boolean handleMainMenuInput(InputHandler.InputAction action)
  {
    boolean exitMenu = false;

    switch( action )
    {
      case ENTER:
        // Grab the current option and roll it back to within the valid range, then evaluate.
        int chosenOption = optionSelector.getSelectionNormalized();

        switch( chosenOption )
        {
          case NEW_GAME:
            currentSubMenuType = SubMenu.GAME_SETUP;
            optionSelector.reset(MapLibrary.getMapList().size());
          break;
          case OPTIONS:
            System.out.println("WARNING! Options menu not supported yet!");
            break;
          case QUIT:
            // Let the caller know we are done here.
            exitMenu = true;
            break;
            default:
              System.out.println("WARNING! Invalid menu option chosen.");
        }
        break;
      case DOWN:
      case UP:
      case LEFT:
      case RIGHT:
        // Pass along to the OptionSelector.
        optionSelector.handleInput(action);
        break;
      case BACK: // There's no "back" from here. Select Quit to exit.
      case NO_ACTION:
        default:
          // Other actions (LEFT, RIGHT, BACK) not supported in the main menu.
    }

    return exitMenu;
  }

  private boolean handleGameSetupMenuInput(InputHandler.InputAction action)
  {
    boolean exitMenu = false;
    switch(action)
    {
      case ENTER:
        // TODO: Move CO/color selection stuff to where it belongs, whenever that exists.
        Commander co1 = new CmdrStrong();
        Commander co2 = new Commander();
        Commander[] cos = { co1, co2 };

        cos[0].myColor = Color.pink;
        cos[1].myColor = Color.cyan;

        // Build the new map and create the game instance.
        GameMap map = new GameMap( cos, MapLibrary.getMapList().get( optionSelector.getSelectionNormalized() ) );
        GameInstance newGame = new GameInstance(map, cos);

        SpriteMapView smv = new SpriteMapView(newGame);
        MapController mapController = new MapController(newGame, smv);

        // Mash the big red button and start the game.
        Driver.getInstance().changeGameState(mapController, smv);
        exitMenu = true;
        break;
      case BACK:
        exitMenu = true;
        break;
      case DOWN:
      case UP:
        optionSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
      case NO_ACTION:
        default:
          System.out.println("Warning: Unsupported input " + action + " in game setup menu.");
    }
    return exitMenu;
  }
}
