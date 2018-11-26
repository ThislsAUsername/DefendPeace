package Engine.GameInput;

public class ConfirmExit extends GameInputState<Object>
{
  private enum ConfirmExitEnum
  {
    LEAVE_MAP, EXIT_GAME
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
  public GameInputState<?> select(Object option)
  {
    GameInputState<?> next = this;
    
    if( ConfirmExitEnum.LEAVE_MAP == option )
    {
      // Go back to the main menu.
      next = new ExitToMainMenu(myStateData);
    }
    else if( ConfirmExitEnum.EXIT_GAME == option )
    {
      // Exit the application entirely.
      System.exit(0);
    }
    
    return next;
  }
}
