package Engine.GameInput;

import java.util.ArrayList;

import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;

/************************************************************
 * Allows selecting an action's target.                     *
 ************************************************************/
class SelectActionTarget extends GameInputState<XYCoord>
{
  public SelectActionTarget(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    ArrayList<XYCoord> targets = myStateData.actionSet.getTargetedLocations();

    // We can only attack the selected tiles, and they may be disjoint, so use constrained tile select.
    return new OptionSet(InputType.CONSTRAINED_TILE_SELECT, targets);
  }

  @Override
  public GameInputState<?> select(XYCoord targetLocation)
  {
    GameInputState<?> next = this;

    // Find the action that this target location belongs to.
    // By virtue of the fact that GameActionSets should be homogenous, and it should be
    // nonsensical to have two actions of the same type targeting the same location, this
    // should be enough to uniquely identify the action we want.
    // Well OK, UNLOAD action sets can have different actions with the same target location.
    //   Fortunately, we don't handle those here.
    for( int i = 0; i < myStateData.actionSet.getTargetedLocations().size(); ++i )
    {
      // If the selected target location corresponds to this action, keep it selected.
      if( targetLocation.equals(myStateData.actionSet.getSelected().getTargetLocation()) )
      {
        // ActionReady will just choose whatever action is selected to perform, so no changes here.
        next = new ActionReady(myStateData);
        break;
      }

      // That wasn't it; move to the next one.
      myStateData.actionSet.next();
    }

    // If we couldn't find an action for this location, return the current state.
    return next;
  }
}