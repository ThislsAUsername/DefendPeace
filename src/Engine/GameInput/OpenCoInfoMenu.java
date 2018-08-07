package Engine.GameInput;

import Engine.GameInput.GameInputHandler.InputType;

/**
 * Terminal state that just tells MapController to switch to the CO info menu.
 */
public class OpenCoInfoMenu extends GameInputState
{
  public OpenCoInfoMenu(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(InputType.CO_INFO);
  }
}
