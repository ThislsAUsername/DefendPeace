package Engine.GameInput;

/** A terminal state that indicates the game is over, no win or lose. */
public class ExitToMainMenu extends GameInputState<Object>
{
  public ExitToMainMenu(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(GameInputHandler.InputType.LEAVE_MAP);
  }
}
