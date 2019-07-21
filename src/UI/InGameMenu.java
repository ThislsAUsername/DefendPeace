package UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import Engine.OptionSelector;

/**
 * Provides a generic interface for an in-game menu. The template parameter can be used to
 * make this class work with any type of option. Further flexibility can be achieved by
 * subclassing (e.g. if you want custom formatting of the menu-option text).
 * @param <T>
 */
public class InGameMenu<T>
{
  private ArrayList<T> menuOptions;

  private OptionSelector optionSelector;

  // Simply records whether this menu has been reset since the last time it was checked.
  // Used to help the renderer figure out whether to regenerate the menu image.
  private boolean wasReset;

  public InGameMenu(Collection<T> options)
  {
    this(options, new OptionSelector(options.size()));
  }

  public InGameMenu(T[] options)
  {
    this(new ArrayList<T>(Arrays.asList(options)));
  }

  public InGameMenu(T[] options, OptionSelector selector)
  {
    this(new ArrayList<T>(Arrays.asList(options)), selector);
  }

  public InGameMenu(Collection<T> options, OptionSelector selector)
  {
    if (options.size() != selector.size())
      throw new IllegalArgumentException("Number of options doesn't match the size of the OptionSelector");
    menuOptions = new ArrayList<T>();
    menuOptions.addAll(options);
    optionSelector = selector;
    wasReset = true;
  }

  /**
   * Set the selection back to the first option.
   */
  public void zero()
  {
    optionSelector.setSelectedOption(0);
  }

  /**
   * Re-initializes this object with the new set of options.
   * @param newOptions
   */
  public void resetOptions(Collection<T> newOptions)
  {
    menuOptions.clear();
    menuOptions.addAll(newOptions);
    optionSelector.reset( menuOptions.size() );
    wasReset = true;
  }

  /**
   * Re-initializes this object with the new set of options.
   * @param newOptions
   */
  public void resetOptions(T[] newOptions)
  {
    menuOptions.clear();
    for( T t : newOptions )
    {
      menuOptions.add(t);
    }
    optionSelector.reset( menuOptions.size() );
    wasReset = true;
  }

  /**
   * Re-initializes this object with the new set of options.
   * @param newOptions
   */
  public void resetOptions(ArrayList<T> newOptions)
  {
    menuOptions.clear();
    menuOptions.addAll(newOptions);
    optionSelector.reset( menuOptions.size() );
    wasReset = true;
  }

  /**
   * Returns the value of wasReset, and unsets it. Intended to be checked before rendering,
   * to determine whether the menu should be re-rendered, or just drawn.
   * NOTE: This assumes there is only one consumer of this function.
   *
   * @return Returns true if wasReset() has been called before, false otherwise.
   */
  public boolean wasReset()
  {
    boolean oldValue = wasReset;
    wasReset = false;
    return oldValue;
  }

  /**
   * Move UP or DOWN the menu options. The internal OptionSelector allows
   * wrap-around if the index goes too far.
   * @param action
   */
  public void handleMenuInput(InputHandler.InputAction action)
  {
    switch (action)
    {
      case UP:
      case DOWN:
        optionSelector.handleInput( action );
        break;
      case LEFT:
      case RIGHT:
      case ENTER:
      case BACK:
      case NO_ACTION:
      default:
        System.out.println("WARNING! gameMenu.handleMenuInput() was given invalid action enum (" + action + ")");
    }
  }

  /**
   * @return The index of the currently-selected menu item.
   */
  public int getSelectionNumber()
  {
    return optionSelector.getSelectionNormalized();
  }

  /**
   * @return Sets the menu highlight to the indicated index.
   */
  public void setSelectionNumber(int index)
  {
    optionSelector.setSelectedOption(index);
  }

  /**
   * @return The object currently highlighted by the menu cursor.
   */
  public T getSelectedOption()
  {
    return menuOptions.get( optionSelector.getSelectionNormalized() );
  }

  /**
   * @return The object at the specified index.
   */
  public T getOption( int index )
  {
    if( menuOptions.size() > index && index >= 0 )
    {
      return menuOptions.get( index );
    }
    // If index is not a valid option, give nothing back.
    System.out.println("WARNING: Attempting to retrieve invalid index in InGameMenu.getOption().");
    return null;
  }

  public int getNumOptions()
  {
    return menuOptions.size();
  }

  /**
   * Returns a string representation of the specified menu option.
   */
  public String getOptionString( int index )
  {
    return menuOptions.get(index).toString();
  }

  /**
   * Returns string representations of all menu options.
   */
  public ArrayList<String> getAllOptions()
  {
    ArrayList<String> out = new ArrayList<String>();
    for (T option : menuOptions)
    {
      out.add(option.toString());
    }
    return out;
  }
}
