package UI;

import Engine.IController;
import Engine.OptionSelector;
import Terrain.MapLibrary;
import UI.InputHandler.InputAction;

public class MapSelectController implements IController
{
  private OptionSelector optionSelector = new OptionSelector( MapLibrary.getMapList().size() );
  private GameOptionSetupController gameOptionsMenu;

  private boolean isInSubmenu = false;

  public int getSelectedOption()
  {
    return optionSelector.getSelectionNormalized();
  }

  public boolean inSubmenu()
  {
    return isInSubmenu;
  }

  public GameOptionSetupController getSubController()
  {
    return gameOptionsMenu;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;

    if(isInSubmenu)
    {
      exitMenu = gameOptionsMenu.handleInput(action);
      if(exitMenu)
      {
        isInSubmenu = false;

        // If BACK was chosen for the child menu, then we take control again (if
        // the child menu exited via entering a game, then action will be ENTER).
        if(action == InputAction.BACK)
        {
          // Don't pass control back up the chain.
          exitMenu = false;
        }
        else
        {
          optionSelector.setSelectedOption(0);
        }
      }
    }
    else
    {
      exitMenu = handleMapSelectInput(action);
    }
    return exitMenu;
  }

  private boolean handleMapSelectInput(InputAction action)
  {
    boolean exitMenu = false;
    switch(action)
    {
      case ENTER:
        // Create the GameBuilder with the selected map, and transition to the CO select screen.
        // If we go forward/back a few times, the old copies of these get replaced and garbage-collected.
        GameBuilder gameBuilder = new GameBuilder( MapLibrary.getMapList().get( optionSelector.getSelectionNormalized() ) );
        gameOptionsMenu = new GameOptionSetupController( gameBuilder );
        isInSubmenu = true;
        break;
      case BACK:
        exitMenu = true;
        break;
      case DOWN:
      case UP:
      case LEFT:
      case RIGHT:
        optionSelector.handleInput(action);
        break;
      case NO_ACTION:
        break;
        default:
          System.out.println("Warning: Unsupported input " + action + " in map select menu.");
    }
    return exitMenu;
  }
}
