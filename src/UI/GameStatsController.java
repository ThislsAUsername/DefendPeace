package UI;

import UI.InputHandler.InputAction;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Army;
import Engine.GameInstance;
import Engine.OptionSelector;

public class GameStatsController implements InfoController
{
  private GameInstance myGame;
  private ArrayList<ArrayList<InfoPage>> infoPages;
  private ArrayList<Army> armies;

  private OptionSelector pageSelector;
  private int shiftDown = 0;

  public GameStatsController( GameInstance game )
  {
    myGame = game;

    infoPages = new ArrayList<ArrayList<InfoPage>>();
    armies = new ArrayList<>();

    ArrayList<InfoPage> headers = new ArrayList<InfoPage>();
    headers.add(new InfoPage(InfoPage.PageType.CO_HEADERS));
    infoPages.add(headers);
    armies.add(myGame.armies[myGame.getActiveCOIndex()]);

    for(Army co : myGame.armies)
    {
      ArrayList<InfoPage> status = new ArrayList<InfoPage>();
      status.add(new InfoPage(InfoPage.PageType.GAME_STATUS));
      infoPages.add(status);
      armies.add(co);
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
  public Army getSelectedArmy()
  {
    return armies.get(pageSelector.getSelectionNormalized());
  }

  @Override
  public ArrayList<CommanderInfo> getSelectedCOInfoList()
  {
    ArrayList<CommanderInfo> output = new ArrayList<>();
    for( Commander co : getSelectedArmy().cos )
    {
      output.add(co.coInfo);
    }
    return output;
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
  public int getPageListCount()
  {
    return pageSelector.size();
  }
}
