package UI;

public class GameMenu
{

  private Enum options[];

  public enum MenuType
  {
    ACTION, PRODUCTION, METAACTION
  };

  public final MenuType menuType;
  private int selectedOption = 0;

  public GameMenu(MenuType menuType, Enum[] options)
  {
    this.menuType = menuType;
    this.options = options;
  }

  public void handleMenuInput(InputHandler.InputAction action)
  {
    switch (action)
    {
      case UP:
        if( selectedOption > 0 )
        {
          selectedOption--;
        }
        break;
      case DOWN:
        if( selectedOption < options.length - 1 )
        {
          selectedOption++;
        }
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
   * @return The current index of the cursor within the menu.
   */
  public int getSelectionNumber()
  {
    return selectedOption;
  }

  /**
   * @return The enum currently highlighted by the menu cursor.
   */
  public Enum getSelectedAction()
  {
    return options[selectedOption];
  }

  /**
   * @return An Enum[] of the current menu options.
   */
  public Enum[] getOptions()
  {
    return options;
  }

  public int getNumOptions()
  {
    return options.length;
  }
}
