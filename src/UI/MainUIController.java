package UI;

import java.io.File;
import Engine.Driver;
import Engine.GameInstance;
import Engine.IController;
import Engine.MapController;
import Engine.OptionSelector;

public class MainUIController implements IController
{
  public enum SubMenu { MAIN, GAME_SETUP, OPTIONS };
  private SubMenu currentSubMenuType = SubMenu.MAIN;

  // NOTE: This list of menu options is mirrored by the Sprite of option images we get from SpriteLibrary.
  final int NEW_GAME = 0;
  final int CONTINUE = 1;
  final int OPTIONS = 2;
  final int QUIT = 3;
  final int numMenuOptions = 4;

  private OptionSelector optionSelector = new OptionSelector(numMenuOptions);

  private MapSelectController gameSetup = new MapSelectController();
  
  public SubMenu getSubMenuType()
  {
    return currentSubMenuType;
  }

  public OptionSelector getOptionSelector()
  {
    return optionSelector;
  }
  
  public MapSelectController getGameSetupController()
  {
    return gameSetup;
  }

  @Override // From IController
  public boolean handleInput(InputHandler.InputAction action)
  {
    boolean exitGame = false;

    switch( currentSubMenuType )
    {
      case GAME_SETUP:
        // Pass the input action along to the active sub-handler.
        exitGame = gameSetup.handleInput(action);
        if(exitGame)
        {
          // If the subMenu was not MAIN, we go back to MAIN.
          currentSubMenuType = SubMenu.MAIN;
          exitGame = false;
        }
        break;
      case MAIN:
        exitGame = handleMainMenuInput(action);
        break;
      case OPTIONS:
        // Since different graphics engines could implement different available
        //   options, we cannot handle this input directly. Instead, just feed
        //   the user inputs to the graphics engine directly.
        exitGame = Driver.getInstance().gameGraphics.handleOptionsInput(action);

        //exitGame = handleOptionsMenu(action);
        if(exitGame)
        {
          // If the subMenu was not MAIN, we go back to MAIN.
          currentSubMenuType = SubMenu.MAIN;
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
          break;
          case CONTINUE: // TODO: make a menu to let you pick your save file
            GameInstance oldGame = null;

            final File folder = new File("save/");
            if( folder.canRead() )
            {
              for( final File fileEntry : folder.listFiles() )
              {
                String filename = fileEntry.getName();
                // Look for files with our extension
                if( !fileEntry.isDirectory() && filename.endsWith(".svp") )
                {
                  oldGame = GameInstance.loadSave(filename);
                  break; // just load the first one we find
                }
              }
            }

            if( null != oldGame )
            {
              MapView mv = Driver.getInstance().gameGraphics.createMapView(oldGame);
              MapController mapController = new MapController(oldGame, mv, false);

              // Mash the big red button and start the game.
              Driver.getInstance().changeGameState(mapController, mv);
            }
            break;
          case OPTIONS:
            currentSubMenuType = SubMenu.OPTIONS;
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
}
