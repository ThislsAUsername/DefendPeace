package UI;

import UI.InputHandler.InputAction;
import Engine.OptionSelector;

public class CO_InfoMenu
{
  private OptionSelector coOptionSelector;
  private OptionSelector pageSelector;
  // 3 pages: CO flavor info, CO Power descriptions, unit strengths.
  private final int NUM_PAGES = 3;

  public CO_InfoMenu( int numCOs )
  {
    coOptionSelector = new OptionSelector( numCOs );
    pageSelector = new OptionSelector( NUM_PAGES );
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

  public int getCoSelection()
  {
    return coOptionSelector.getSelectionNormalized();
  }

  public int getPageSelection()
  {
    return pageSelector.getSelectionNormalized();
  }
}
