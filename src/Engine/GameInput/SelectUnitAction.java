package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameActionSet;
import Engine.UnitActionFactory;
import Engine.Combat.DamagePopup;
import Units.GBAFEActions;

/************************************************************
 * State to allow selecting an action for a unit.           *
 ************************************************************/
class SelectUnitAction extends GameInputState<GameActionSet>
{
  // Don't provide a default value, or it will override the value
  // set by the call to initOptions() in the super-constructor.
  private ArrayList<GameActionSet> myUnitActions;

  public SelectUnitAction(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    // Get the set of actions this unit could perform from the target location.
    myUnitActions = myStateData.unitActor.getPossibleActions(myStateData.gameMap, myStateData.path);

    return new OptionSet(myUnitActions.toArray());
  }

  @Override
  public void consider(GameActionSet menuOption)
  {
    myStateData.damagePopups = new ArrayList<DamagePopup>();
    // If there's a preview and no targeting step, we have to preview now
    if( !menuOption.isTargetRequired() )
      myStateData.damagePopups = menuOption.getSelected().getDamagePopups(myStateData.gameMap);
  }
  @Override
  public GameInputState<?> select(GameActionSet menuOption)
  {
    GameInputState<?> next = this;

    // Find the set in myUnitActions. We iterate because it
    // 1) allows us to avoid a cast, and
    // 2) ensures that the provided menuOption is actually one we support.
    GameActionSet chosenSet = null;
    if( null != menuOption )
    {
      for( GameActionSet set : myUnitActions )
      {
        if( set == menuOption )
        {
          chosenSet = set;
          break;
        }
      }
    }

    if( null != chosenSet )
    {
      // Add the action set to our state data.
      myStateData.actionSet = chosenSet;

      // If the action type requires no target, then we are done collecting input.
      if( !chosenSet.isTargetRequired() )
      {
        next = new ActionReady(myStateData);
      }
      else
      {
        UnitActionFactory actionType = chosenSet.getSelected().getType();
        // We might need more input before an action is ready; What kind of input depends on the type of action.
        if( UnitActionFactory.UNLOAD == actionType )
          // We need to select a unit to unload.
          next = new SelectCargo(myStateData);
        else if( UnitActionFactory.LAUNCH == actionType )
          // We need to select a unit to launch.
          next = new SelectLaunchable(myStateData);
        else if( actionType.shouldConfirm )
          // Confirm deletion. Don't want angry users rising up with pitchforks.
          next = new ConfirmUnitAction(myStateData);
        else if( GBAFEActions.RescueUnitFactory.instance == actionType || GBAFEActions.TakeUnitFactory.instance == actionType )
          next = new SelectActionCantoTarget(myStateData);
        else if( GBAFEActions.DropUnitFactory.instance == actionType )
          next = SelectCantoDropLocation.build(myStateData, null, myStateData.unitActor.heldUnits.get(0));
        else
          // Generic targeting.
          next = new SelectActionTarget(myStateData);
      }
    }
    return next;
  }

  @Override
  public void back()
  {
    myStateData.actionSet = null;
    myStateData.damagePopups.clear();
  }
}
