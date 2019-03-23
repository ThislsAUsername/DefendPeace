package UI;

import UI.InputHandler.InputAction;
import CommandingOfficers.Commander;
import Engine.OptionSelector;

public class CO_InfoMenu
{
  private OptionSelector coOptionSelector;
  private OptionSelector[] pageSelectors;

  public CO_InfoMenu( Commander[] COs )
  {
    coOptionSelector = new OptionSelector(COs.length);
    pageSelectors = new OptionSelector[COs.length];
    for( int i = 0; i < COs.length; ++i )
    {
      pageSelectors[i] = new OptionSelector(COs[i].coInfo.maker.infoPages.size());
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

  public int getCoSelection()
  {
    return coOptionSelector.getSelectionNormalized();
  }

  public int getPageSelection()
  {
    return pageSelectors[coOptionSelector.getSelectionNormalized()].getSelectionNormalized();
  }
}
