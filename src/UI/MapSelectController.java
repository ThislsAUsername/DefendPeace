package UI;

import Engine.IController;
import Engine.OptionSelector;
import Terrain.MapInfo.MapNode;
import Terrain.MapLibrary;
import UI.InputHandler.InputAction;

public class MapSelectController implements IController
{
  public MapNode currentNode = MapLibrary.getMapGraph();
  private OptionSelector optionSelector = new OptionSelector( optCount() );

  private GameOptionSetupController gameOptionsMenu;

  private boolean isInSubmenu = false;

  public int getSelectedOption()
  {
    return optionSelector.getSelectionNormalized();
  }
  public int optCount()
  {
    return currentNode.children.size();
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
      case SELECT:
        MapNode pick = currentNode.children.get(optionSelector.getSelectionNormalized());
        if( pick.result == null )
        {
          currentNode = pick;
          optionSelector.reset(optCount());
          break;
        }
        // Create the GameBuilder with the selected map, and transition to the CO select screen.
        // If we go forward/back a few times, the old copies of these get replaced and garbage-collected.
        GameBuilder gameBuilder = new GameBuilder( pick.result );
        gameOptionsMenu = new GameOptionSetupController( gameBuilder );
        isInSubmenu = true;
        break;
      case BACK:
        if( currentNode.parent == null )
        {
          exitMenu = true;
          break;
        }
        int myIndex = currentNode.parent.children.indexOf(currentNode);
        currentNode = currentNode.parent;
        optionSelector.reset(optCount(), myIndex);
        break;
      case DOWN:
      case UP:
      case LEFT:
      case RIGHT:
        optionSelector.handleInput(action);
        break;
        default:
          System.out.println("Warning: Unsupported input " + action + " in map select menu.");
    }
    return exitMenu;
  }
}
