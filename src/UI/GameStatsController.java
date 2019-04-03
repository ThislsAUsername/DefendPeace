package UI;

import UI.InputHandler.InputAction;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.COMaker.InfoPage;
import Engine.GameInstance;
import Engine.OptionSelector;

public class GameStatsController implements InfoController
{
  private GameInstance myGame;
  
  private OptionSelector coOptionSelector;
  private OptionSelector[] pageSelectors;

  public GameStatsController( GameInstance game )
  {
    myGame = game;
    
    coOptionSelector = new OptionSelector(myGame.commanders.length);
    pageSelectors = new OptionSelector[myGame.commanders.length];
    for( int i = 0; i < myGame.commanders.length; ++i )
    {
      pageSelectors[i] = new OptionSelector(myGame.commanders[i].coInfo.maker.infoPages.size());
    }
  }

  @Override
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

  @Override
  public Commander getSelectedCO()
  {
    return myGame.commanders[coOptionSelector.getSelectionNormalized()];
  }

  @Override
  public InfoPage getSelectedPage()
  {
    return getSelectedCO().coInfo.maker.infoPages.get(pageSelectors[coOptionSelector.getSelectionNormalized()].getSelectionNormalized());
  }
  
  @Override
  public GameInstance getGame()
  {
    return myGame;
  }

  @Override
  public CommanderInfo getSelectedCOInfo()
  {
    return getSelectedCO().coInfo;
  }
}
