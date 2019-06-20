package Engine;

import UI.InputHandler.InputAction;

/**
 * Keeps track of the currently-highlighted option in a list.
 */
public class OptionSelector
{
  private int numOptions;
  private int highestOption;
  private int highlightedOption;

  /**
   * Initializes the OptionSelector with num options.
   * @param num The number of valid options. This is assumed to be >= 1.
   */
  public OptionSelector(int num)
  {
    numOptions = num;
    highestOption = num-1;
    highlightedOption = 0;
  }

  /**
   * Re-initializes the OptionSelector with num options.
   * @param newNum The number of valid options. This is assumed to be >= 1.
   */
  public void reset(int newNum)
  {
    numOptions = newNum;
    highestOption = newNum-1;
    highlightedOption = 0;
  }

  /**
   * Sets the highlighted option to the indicated number.
   */
  public void setSelectedOption(int num)
  {
    highlightedOption = num;
  }

  /**
   * OptionSelector doesn't bother with normalizing the index, meaning the
   * index it stores internally may be invalid to the real list it represents.
   * If you want to see the current internal index, get it here.
   * @return The current non-normalized selected index, e.g. the number of UP
   * inputs minus the number of DOWN inputs over the life of this object.
   */
  public int getSelectionAbsolute()
  {
    if( numOptions < 1 )
    {
      throw new IndexOutOfBoundsException("OptionSelector has no options to select!");
    }
    return highlightedOption;
  }

  /**
   * @return The currently-highlighted index, normalized to within the valid range.
   */
  public int getSelectionNormalized()
  {
    if( numOptions < 1 )
    {
      throw new IndexOutOfBoundsException("OptionSelector has no options to select!");
    }
    int chosenOption = highlightedOption;
    for(;chosenOption < 0; chosenOption += numOptions);
    for(;chosenOption > highestOption; chosenOption -= numOptions);
    return chosenOption;
  }

  public void next()
  {
    highlightedOption++;
  }

  public void prev()
  {
    highlightedOption--;
  }

  public void handleInput(InputAction action)
  {
    switch( action )
    {
      case DOWN:
      case RIGHT:
        next();
        break;
      case UP:
      case LEFT:
        prev();
        break;
      case BACK:
      case ENTER:
      case NO_ACTION:
        break;
      default:
        System.out.println("Warning: Unexpected input received in OptionSelector.");
    }
  }
  
  public int size()
  {
    return numOptions;
  }
}
