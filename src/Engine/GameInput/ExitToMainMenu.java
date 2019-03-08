package Engine.GameInput;

/** A terminal state that indicates the game is over, no win or lose. */
public class ExitToMainMenu extends GameInputState<Object>
{
  public ExitToMainMenu(StateData data, boolean save)
  {
    super(data);
    if (save)
      myOptions = new OptionSet(GameInputHandler.InputType.SAVE_AND_QUIT);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(GameInputHandler.InputType.LEAVE_MAP);
  }
}
