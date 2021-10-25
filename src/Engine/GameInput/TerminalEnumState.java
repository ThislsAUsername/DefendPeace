package Engine.GameInput;

import Engine.GameInput.GameInputHandler.InputType;

/**
 * Terminal state that just tells MapController to switch to a particular input mode.
 */
public class TerminalEnumState extends GameInputState<Object>
{
  public static InputType state;

  public TerminalEnumState(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(state);
  }
}
