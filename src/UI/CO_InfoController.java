package UI;

import UI.InputHandler.InputAction;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;

import java.util.ArrayList;

import Engine.Army;
import Engine.GameInstance;
import Engine.OptionSelector;

public class CO_InfoController implements InfoController
{
  private ArrayList<CommanderInfo> coInfos;
  private ArrayList<Commander> coList = new ArrayList<>();
  private GameInstance myGame;

  int shiftDown = 0;
  private OptionSelector coOptionSelector;

  public CO_InfoController( GameInstance game )
  {
    myGame = game;
    ArrayList<CommanderInfo> infos = new ArrayList<>();
    
    for( Army army : myGame.armies )
      for( Commander co : army.cos )
      {
        infos.add(co.coInfo);
        coList.add(co);
      }
    
    init(infos);
    coOptionSelector.setSelectedOption(myGame.getActiveCOIndex());
  }

  public CO_InfoController( ArrayList<CommanderInfo> infos, int startingIndex )
  {
    init(infos);
    coOptionSelector.setSelectedOption(startingIndex);
  }
  
  private void init( ArrayList<CommanderInfo> infos )
  {
    coInfos = infos;
    
    coOptionSelector = new OptionSelector(coInfos.size());
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
        // Left/Right changes which CO has focus.
        shiftDown = 0;
        coOptionSelector.handleInput( action );
        break;
      case SELECT:
      case BACK:
        // Reset the selectors and leave this menu.
        coOptionSelector.setSelectedOption(0);
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
    if (coList.isEmpty())
      return null;
    return coList.get(coOptionSelector.getSelectionNormalized()).army;
  }

  @Override
  public int getShiftDown()
  {
    return shiftDown;
  }

  @Override
  public ArrayList<InfoPage> getSelectedPages()
  {
    return getSelectedCOInfo().iterator().next().infoPages;
  }

  @Override
  public GameInstance getGame()
  {
    return myGame;
  }

  @Override
  public ArrayList<CommanderInfo> getSelectedCOInfo()
  {
    ArrayList<CommanderInfo> output = new ArrayList<>();
    output.add(coInfos.get(coOptionSelector.getSelectionNormalized()));
    return output;
  }

  @Override
  public int getPageListCount()
  {
    return coOptionSelector.size();
  }
}
