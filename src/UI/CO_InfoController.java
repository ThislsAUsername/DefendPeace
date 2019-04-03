package UI;

import UI.InputHandler.InputAction;
import CommandingOfficers.Commander;
import CommandingOfficers.COMaker.InfoPage;
import Engine.GameInstance;
import Engine.IController;
import Engine.OptionSelector;

public class CO_InfoController implements InfoController
{
  private GameInstance myGame;
  
  private OptionSelector coOptionSelector;
  private OptionSelector[] pageSelectors;

  public CO_InfoController( GameInstance game )
  {
    myGame = game;
    
    coOptionSelector = new OptionSelector(myGame.commanders.length);
    pageSelectors = new OptionSelector[myGame.commanders.length];
    for( int i = 0; i < myGame.commanders.length; ++i )
    {
      pageSelectors[i] = new OptionSelector(myGame.commanders[i].coInfo.maker.infoPages.size());
    }
  }

  /* (non-Javadoc)
   * @see UI.CO_InfoController#handleInput(UI.InputHandler.InputAction)
   */
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

  /* (non-Javadoc)
   * @see UI.CO_InfoController#getSelectedCO()
   */
  @Override
  public Commander getSelectedCO()
  {
    return myGame.commanders[coOptionSelector.getSelectionNormalized()];
  }

  /* (non-Javadoc)
   * @see UI.CO_InfoController#getPageSelection()
   */
  @Override
  public InfoPage getSelectedPage()
  {
    return getSelectedCO().coInfo.maker.infoPages.get(pageSelectors[coOptionSelector.getSelectionNormalized()].getSelectionNormalized());
  }
  
  /* (non-Javadoc)
   * @see UI.CO_InfoController#getGame()
   */
  @Override
  public GameInstance getGame()
  {
    return myGame;
  }
}
