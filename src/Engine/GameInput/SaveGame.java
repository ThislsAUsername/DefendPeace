package Engine.GameInput;

/** A terminal state that indicates the user wants to save. */
public class SaveGame extends GameInputState<Object>
{
  public SaveGame(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(GameInputHandler.InputType.SAVE);
  }
}
