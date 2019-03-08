package Engine.GameInput;

/** A terminal state that indicates the game is over, no win or lose. */
public class ExitToMainMenu extends GameInputState<Object>
{
  final boolean shouldSaveGame;
  
  public ExitToMainMenu(StateData data, boolean save)
  {
    super(data);
    shouldSaveGame = save;
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(GameInputHandler.InputType.LEAVE_MAP);
  }
}
