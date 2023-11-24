package Engine;

import UI.InputHandler.InputAction;

/**
 * Keeps track of the currently-highlighted option in a list.
 */
public class OptionSelector
{
  private int numOptions;
  private int highlightedOption;

  /**
   * Initializes the OptionSelector with num options.
   * @param num The number of valid options. This is assumed to be >= 1.
   */
  public OptionSelector(int num)
  {
    this(num, 0);
  }
  public OptionSelector(int num, int newSelection)
  {
    numOptions = num;
    highlightedOption = newSelection;
  }

  /**
   * Re-initializes the OptionSelector with num options.
   * @param newNum The number of valid options. This is assumed to be >= 1.
   */
  public void reset(int newNum)
  {
    reset(newNum, 0);
  }
  public void reset(int newNum, int newSelection)
  {
    numOptions = newNum;
    highlightedOption = newSelection;
  }

  /**
   * Sets the highlighted option to the indicated number.
   */
  public void setSelectedOption(int num)
  {
    // If the values are close, hakuna matata
    if( Math.abs(highlightedOption - num) < 2 )
    {
      highlightedOption = num;
      return;
    }

    // Otherwise, figure out the smallest shift to get our result
    final int oldNormal = normalize(highlightedOption, numOptions);
    final int newNormal = normalize(num, numOptions);
    final int shiftNaive = newNormal - oldNormal;
    int shiftCandidate = shiftNaive;
    if( Math.abs(shiftNaive) > numOptions / 2 )
    {
      // The new shift should have the opposite sign of the naive one
      if( shiftNaive > 0 )
        shiftCandidate = shiftNaive - numOptions;
      else
        shiftCandidate = shiftNaive + numOptions;
    }
    highlightedOption += shiftCandidate;
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
    return normalize(highlightedOption, numOptions);
  }

  /**
   * @return The input index, normalized to within the valid range.
   */
  public static int normalize(int optionAbsolute, int numOptions)
  {
    if( numOptions < 1 )
    {
      throw new IndexOutOfBoundsException("OptionSelector has no options to select!");
    }
    final int highestOption = numOptions - 1;
    int chosenOption = optionAbsolute;
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

  public int handleInput(InputAction action)
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
      case SELECT:
        break;
      default:
        System.out.println("Warning: Unexpected input received in OptionSelector.");
    }
    return getSelectionNormalized();
  }
  
  public int size()
  {
    return numOptions;
  }
}
