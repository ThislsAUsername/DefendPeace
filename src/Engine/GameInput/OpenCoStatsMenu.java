package Engine.GameInput;

import Engine.GameInput.GameInputHandler.InputType;

/**
 * Terminal state that just tells MapController to switch to the CO Stats menu.
 */
public class OpenCoStatsMenu extends GameInputState<Object>
{
  public OpenCoStatsMenu(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(InputType.CO_STATS);
  }
}
