package Engine.GameInput;

public class ConfirmExit extends GameInputState<ConfirmExit.ConfirmExitEnum>
{
  enum ConfirmExitEnum
  {
    SAVE_GAME, LEAVE_MAP, EXIT_GAME
  };

  public ConfirmExit(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(ConfirmExitEnum.values());
  }

  @Override
  public GameInputState<?> select(ConfirmExit.ConfirmExitEnum option)
  {
    GameInputState<?> next = this;
    switch(option)
    {
      case SAVE_GAME:
        // Go back to the main menu, but save our game state first.
        next = new ExitToMainMenu(myStateData, true);
        break;
      case LEAVE_MAP:
        // Go back to the main menu.
        next = new ExitToMainMenu(myStateData, false);
        break;
      case EXIT_GAME:
        // Exit the application entirely.
        System.exit(0);
        break;
    }
    
    return next;
  }
}
