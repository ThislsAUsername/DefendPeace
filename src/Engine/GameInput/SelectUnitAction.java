package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameActionSet;
import Engine.UnitActionFactory;
import Engine.Combat.DamagePopup;
import UI.InGameMenu;
import UI.InGameMenu.MenuOption;

/************************************************************
 * State to allow selecting an action for a unit.           *
 ************************************************************/
class SelectUnitAction extends GameInputState<MenuOption<GameActionSet>>
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
    myUnitActions = myStateData.unitActor.getGUIActions(myStateData.gameMap, myStateData.path);

    ArrayList<MenuOption<GameActionSet>> opts = new ArrayList<>();
    for(GameActionSet gas : myUnitActions)
    {
      MenuOption<GameActionSet> mo = new MenuOption<>(gas);
      mo.enabled = !gas.isInvalidChoice;
      opts.add(mo);
    }

    return new OptionSet(opts.toArray());
  }
  @SuppressWarnings("unchecked")
  @Override
  public InGameMenu<? extends Object> getMenu()
  {
    ArrayList<MenuOption<GameActionSet>> opts = new ArrayList<>();
    for( Object o : myOptions.getMenuOptions() )
      opts.add((MenuOption<GameActionSet>)o);
    return new InGameMenu<GameActionSet>(opts, getOptionSelector(), false);
  }

  @Override
  public void consider(MenuOption<GameActionSet> menuOption)
  {
    myStateData.damagePopups = new ArrayList<DamagePopup>();
    // If there's a preview and no targeting step, we have to preview now
    if( !menuOption.item.isTargetRequired() )
      myStateData.damagePopups = menuOption.item.getSelected().getDamagePopups(myStateData.gameMap);
  }
  @Override
  public GameInputState<?> select(MenuOption<GameActionSet> menuOption)
  {
    GameInputState<?> next = this;
    if( !menuOption.enabled )
      return next;

    // Find the set in myUnitActions. We iterate because it
    // 1) allows us to avoid a cast, and
    // 2) ensures that the provided menuOption is actually one we support.
    GameActionSet chosenSet = null;
    if( null != menuOption )
    {
      for( GameActionSet set : myUnitActions )
      {
        if( set == menuOption.item )
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
