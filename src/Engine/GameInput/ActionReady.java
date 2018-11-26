package Engine.GameInput;

/************************************************************
 * Terminal state - just provides the selected action.      *
 ************************************************************/
class ActionReady extends GameInputState
{
  public ActionReady(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(myStateData.actionSet.getSelected());
  }
}