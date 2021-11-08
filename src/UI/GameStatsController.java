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
  private ArrayList<ArrayList<InfoPage>> infoPages;
  private ArrayList<Commander> commanders;

  private OptionSelector pageSelector;
  private int shiftDown = 0;

  public GameStatsController( GameInstance game )
  {
    myGame = game;

    infoPages = new ArrayList<ArrayList<InfoPage>>();
    commanders = new ArrayList<>();

    ArrayList<InfoPage> headers = new ArrayList<InfoPage>();
    headers.add(new InfoPage(InfoPage.PageType.CO_HEADERS));
    infoPages.add(headers);
    commanders.add(myGame.commanders[myGame.getActiveCOIndex()]);

    for(Commander co : myGame.commanders)
    {
      ArrayList<InfoPage> status = new ArrayList<InfoPage>();
      status.add(new InfoPage(InfoPage.PageType.GAME_STATUS));
      infoPages.add(status);
      commanders.add(co);
    }

    pageSelector = new OptionSelector(infoPages.size());
  }

  @Override
  public boolean handleInput( InputAction action )
  {
    boolean goBack = false;
    switch( action )
    {
      case DOWN:
        shiftDown++;
        break;
      case UP:
        shiftDown--;
        break;
      case LEFT:
      case RIGHT:
        // Left/Right changes which sub-page has focus.
        shiftDown = 0;
        pageSelector.handleInput(action);
        break;
      case SELECT:
      case BACK:
        // Reset the selectors and leave this menu.
        pageSelector.setSelectedOption(0);
        goBack = true;
        break;
        default:
          // Other actions are not supported.
    }
    return goBack;
  }

  @Override
  public Commander getSelectedCO()
  {
    return commanders.get(pageSelector.getSelectionNormalized());
  }

  @Override
  public int getShiftDown()
  {
    return shiftDown;
  }

  @Override
  public ArrayList<InfoPage> getSelectedPages()
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
