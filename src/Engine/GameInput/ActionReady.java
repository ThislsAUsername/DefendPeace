package Engine.GameInput;

import Engine.GameAction;
import Engine.UnitActionLifecycles.LaunchLifecycle.LaunchAction;

/************************************************************
 * Terminal state - just provides the selected action.      *
 ************************************************************/
class ActionReady extends GameInputState<Object>
{
  public ActionReady(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    GameAction readiedAction = myStateData.actionSet.getSelected();
    // If the unit needs to be launched, wrap its action in a launch action.
    if (null != myStateData.unitLauncher)
      readiedAction = new LaunchAction(myStateData.unitLauncher, myStateData.unitActor, readiedAction);
    return new OptionSet(readiedAction);
  }
}