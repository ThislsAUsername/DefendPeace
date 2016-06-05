package Engine;

import UI.InputHandler.InputAction;

/**
 * Keeps track of the currently-highlighted option in a list.
 */
public class OptionSelector implements IController
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
   * Allows for reinitializing the OptionSelector with new bounds. Sets the
   * current index back to 0.
   * @param num The number of valid options. This is assumed to be >= 1.
   */
  public void reset(int num)
  {
    numOptions = num;
    highestOption = num-1;
    highlightedOption = 0;
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
    return highlightedOption;
  }

  /**
   * @return The currently-highlighted index, normalized to within the valid range.
   */
  public int getSelectionNormalized()
  {
    int chosenOption = highlightedOption;
    for(;chosenOption < 0; chosenOption += numOptions);
    for(;chosenOption > highestOption; chosenOption -= numOptions);
    return chosenOption;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    switch( action )
    {
      case DOWN:
      case RIGHT:
        highlightedOption++;
        break;
      case UP:
      case LEFT:
        highlightedOption--;
        break;
      case BACK:
      case ENTER:
      case NO_ACTION:
        break;
      default:
        System.out.println("Warning: Unexpected input received in OptionSelector.");
    }
    return false; // OptionSelector isn't actually in control of anything, really.
  }
}
