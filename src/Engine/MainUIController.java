package Engine;

import UI.InputHandler;
import UI.MainMenuController;

public class MainUIController implements IController
{
  private static final long serialVersionUID = 5548786952371603112L;

  public enum SubMenu { MAIN, GAME_SETUP, OPTIONS };
  private SubMenu currentSubMenu = SubMenu.MAIN;
  
  private MainMenuController mainMenu = new MainMenuController();

  public int getHighlightedOption()
  {
    int option = -1;
    switch( currentSubMenu )
    {
      case GAME_SETUP:
        break;
      case MAIN:
        option = mainMenu.getHighlightedOption();
        break;
      case OPTIONS:
        break;
        default:
          System.out.println("Warning: Invalid submenu selected in MainController.");
    }
    return option;
  }

  @Override // From IController
  public boolean handleInput(InputHandler.InputAction action)
  {
    boolean exitGame = false;

    switch( currentSubMenu )
    {
      case GAME_SETUP:
        //exitGame = handleGameSetupMenu(action);
        if(exitGame)
        {
          currentSubMenu = SubMenu.MAIN;
          exitGame = false;
        }
        break;
      case MAIN:
        exitGame = mainMenu.handleInput(action);
        break;
      case OPTIONS:
        //exitGame = handleOptionsMenu(action);
        if(exitGame)
        {
          currentSubMenu = SubMenu.MAIN;
          exitGame = false;
        }
        break;
      default:
        System.out.println("Warning: Invalid input " + action + " in MainUIController.");
    }

    return exitGame;
  }
}
