package UI;

import UI.InputHandler.InputAction;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;

import java.util.ArrayList;

import CommandingOfficers.COMaker.InfoPage;
import Engine.GameInstance;
import Engine.OptionSelector;

public class CO_InfoController implements InfoController
{
  private ArrayList<CommanderInfo> coInfos;
  
  private OptionSelector coOptionSelector;
  private OptionSelector[] pageSelectors;

  public CO_InfoController( Commander[] cos )
  {
    ArrayList<CommanderInfo> infos = new ArrayList<CommanderInfo>();
    
    for( Commander co : cos )
    {
      infos.add(co.coInfo);
    }
    
    init(infos);
  }

  public CO_InfoController( ArrayList<CommanderInfo> infos )
  {
    init(infos);
  }
  
  private void init( ArrayList<CommanderInfo> infos )
  {
    coInfos = infos;
    
    coOptionSelector = new OptionSelector(coInfos.size());
    pageSelectors = new OptionSelector[coInfos.size()];
    for( int i = 0; i < coInfos.size(); ++i )
    {
      pageSelectors[i] = new OptionSelector(coInfos.get(i).maker.infoPages.size());
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
    return null;
  }

  @Override
  public InfoPage getSelectedPage()
  {
    return getSelectedCOInfo().maker.infoPages.get(pageSelectors[coOptionSelector.getSelectionNormalized()].getSelectionNormalized());
  }
  
  @Override
  public GameInstance getGame()
  {
    return null;
  }

  @Override
  public CommanderInfo getSelectedCOInfo()
  {
    return coInfos.get(coOptionSelector.getSelectionNormalized());
  }
}
