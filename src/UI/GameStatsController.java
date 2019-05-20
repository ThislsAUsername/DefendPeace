package UI;

import UI.InputHandler.InputAction;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameInstance;
import Engine.OptionSelector;

public class GameStatsController implements InfoController
{
  private GameInstance myGame;
  private ArrayList<InfoPage> infoPages;
  
  private OptionSelector coOptionSelector;
  private OptionSelector pageSelector;

  public GameStatsController( GameInstance game )
  {
    myGame = game;

    infoPages = new ArrayList<InfoPage>();
    infoPages.add(new InfoPage(InfoPage.PageType.CO_HEADERS));
    infoPages.add(new InfoPage(InfoPage.PageType.GAME_STATUS));
    
    coOptionSelector = new OptionSelector(myGame.commanders.length);
    pageSelector = new OptionSelector(infoPages.size());
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
        pageSelector.handleInput(action);
        break;
      case ENTER:
      case BACK:
        // Reset the selectors and leave this menu.
        coOptionSelector.setSelectedOption(0);
        pageSelector.setSelectedOption(0);
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
    return infoPages.get(pageSelector.getSelectionNormalized());
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
