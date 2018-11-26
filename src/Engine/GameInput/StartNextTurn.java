package Engine.GameInput;

/** A terminal state that signals it's time to begin the next turn. */
public class StartNextTurn extends GameInputState
{
  public StartNextTurn(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(GameInputHandler.InputType.END_TURN);
  }
}
