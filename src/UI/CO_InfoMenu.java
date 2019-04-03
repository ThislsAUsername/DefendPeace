package UI;

import UI.InputHandler.InputAction;
import CommandingOfficers.Commander;
import CommandingOfficers.COMaker.InfoPage;
import Engine.GameInstance;
import Engine.IController;
import Engine.OptionSelector;

public class CO_InfoMenu implements IController
{
  private GameInstance myGame;
  
  private OptionSelector coOptionSelector;
  private OptionSelector[] pageSelectors;

  public CO_InfoMenu( GameInstance game )
  {
    myGame = game;
    
    coOptionSelector = new OptionSelector(myGame.commanders.length);
    pageSelectors = new OptionSelector[myGame.commanders.length];
    for( int i = 0; i < myGame.commanders.length; ++i )
    {
      pageSelectors[i] = new OptionSelector(myGame.commanders[i].coInfo.maker.infoPages.size());
    }
  }

  public boolean handleInput( InputAction action )
  {
    boolean goBack = false;
    switch( action )
    {
      case DOWN:
      case UP:
        // Up/Down changes which CO has focus.
        coOptionSelector.handleInput( action );
        break;
      case LEFT:
      case RIGHT:
        // Left/Right changes which sub-page has focus.
        pageSelectors[coOptionSelector.getSelectionNormalized()].handleInput(action);
        break;
      case ENTER:
      case BACK:
        // Reset the selectors and leave this menu.
        coOptionSelector.setSelectedOption(0);
        for( int i = 0; i < pageSelectors.length; ++i )
        {
          pageSelectors[i].setSelectedOption(0);
        }
        goBack = true;
        break;
      case NO_ACTION:
        default:
          // Other actions are not supported.
    }
    return goBack;
  }

  public Commander getSelectedCO()
  {
    return myGame.commanders[coOptionSelector.getSelectionNormalized()];
  }

  public InfoPage getPageSelection()
  {
    return getSelectedCO().coInfo.maker.infoPages.get(pageSelectors[coOptionSelector.getSelectionNormalized()].getSelectionNormalized());
  }
  
  public GameInstance getGame()
  {
    return myGame;
  }
}
