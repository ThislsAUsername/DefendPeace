package Engine.GameInput;

import java.util.Collection;
import java.util.Stack;

import Engine.Army;
import Engine.GameAction;
import Engine.OptionSelector;
import Engine.XYCoord;
import Engine.GameInput.GameInputState.StateData;
import Terrain.GameMap;
import UI.InGameMenu;
import Units.Unit;

/************************************************************
 * Handles converting user input into game actions.
 ************************************************************/
public class GameInputHandler
{
  public StateData myStateData = null;
  private Stack<GameInputState<?>> myStateStack = null;
  private StateChangedCallback myCallback = null;

  public enum InputType { FREE_TILE_SELECT, PATH_SELECT, MENU_SELECT, CONSTRAINED_TILE_SELECT, ACTION_READY, SAVE, LEAVE_MAP, CO_STATS, CO_INFO, DAMAGE_CHART };

  public GameInputHandler(GameMap map, Army currentPlayer, StateChangedCallback callback)
  {
    myStateStack = new Stack<GameInputState<?>>();
    myStateData = new StateData(map, currentPlayer);
    myStateStack.push(new DefaultState(myStateData));
    myCallback = callback;
  }

  /**
   * Back up to the previous state.
   */
  public GameInputState<?> back()
  {
    GameInputState<?> oldCurrentState = null;
    GameInputState<?> newCurrentState = null;
    if( !myStateStack.isEmpty() )
    {
      oldCurrentState = myStateStack.pop();
      oldCurrentState.back(); // Undo any StateData changes.
    }
    else
    {
      System.out.println("WARNING! InputStateHandler state stack is empty!");
      myStateData = new StateData(myStateData.gameMap, myStateData.army);
      oldCurrentState = new DefaultState(myStateData);
    }

    // If the stack is now empty, put this guy back again.
    // The last one should be DefaultState.
    if( myStateStack.isEmpty() )
    {
      myStateStack.push(oldCurrentState);
    }

    // Set newCurrentState to whatever is now at the top of the stack.
    newCurrentState = peekCurrentState();

    // If we are in a different state now than before, notify the callback.
    if( oldCurrentState != newCurrentState )
    {
      myCallback.onStateChange();
    }
    return newCurrentState;
  }

  /**
   * Tentatively select, i.e. hover over a new option
   * @param option - The chosen menu option, from among those provided by OptionSet.getMenuOptions().
   */
  public <T> void consider(T option)
  {
    @SuppressWarnings("unchecked")
    GameInputState<T> current = (GameInputState<T>) peekCurrentState();
    current.consider(option);
  }
  /**
   * Choose the passed-in option for the current state, triggering a transition to the
   * next state. If no transition is possible, the state will not change.
   * @param option - The chosen menu option, from among those provided by OptionSet.getMenuOptions().
   * @return The OptionSet for the next (and now current) state.
   */
  public <T> InputType select(T option)
  {
    @SuppressWarnings("unchecked")
    GameInputState<T> current = (GameInputState<T>) peekCurrentState();
    GameInputState<?> next = current.select(option);
    pushNextState(next);
    return peekCurrentState().getOptions().inputType;
  }

  public InputType reset()
  {
    // Unwind the stack, all the way back to the starting state.
    myStateStack.clear();
    myStateData = new StateData(myStateData.gameMap, myStateData.army);
    myStateStack.push(new DefaultState(myStateData));
    return peekCurrentState().getOptions().inputType;
  }

  /** Get the current state, but don't pop it off the stack. */
  private GameInputState<?> peekCurrentState()
  {
    if( myStateStack.isEmpty() )
    {
      System.out.println("WARNING! GameActionBuilder has no state active! Creating default.");
      myStateData = new StateData(myStateData.gameMap, myStateData.army);
      myStateStack.push(new DefaultState(myStateData));
    }
    return myStateStack.peek();
  }

  /** Push next onto the state stack if it is not the same object as current. */
  private void pushNextState(GameInputState<?> next)
  {
    // States should return themselves if they receive invalid input; a next
    // state of null is equivalent to a back() command.
    GameInputState<?> current = peekCurrentState();
    if(null == next)
    {
      back();
    }
    else if(current != next)
    {
      // Push the next state if it's valid.
      myStateStack.push(next);

      // Let the callback know we did something.
      if( null != myCallback )
      {
        myCallback.onStateChange();
      }
    }
  }

  /** @return The action to execute, if one is ready, or null if not. */
  public GameAction getReadyAction()
  {
    return peekCurrentState().getOptions().getAction();
  }

  /** @return The currently-valid menu-option strings, or null if no menu is active. */
  public Object[] getMenuOptions()
  {
    return peekCurrentState().getOptions().getMenuOptions();
  }
  public InGameMenu<? extends Object> getMenu()
  {
    return peekCurrentState().getMenu();
  }

  /** @return The currently-recommended input mode. */
  public InputType getInputType()
  {
    return peekCurrentState().getOptions().inputType;
  }

  /** @return The OptionSelector for the current state, or null if it does not provide options. */
  public OptionSelector getOptionSelector()
  {
    return peekCurrentState().getOptionSelector();
  }

  /** @return The current set of coordinates from which to choose, or null if we are not selecting a location. */
  public Collection<XYCoord> getCoordinateOptions()
  {
    return peekCurrentState().getOptions().getCoordinateOptions();
  }

  public XYCoord getUnitCoord()
  {
    return myStateData.unitCoord;
  }

  public Unit getActingUnit()
  {
    return myStateData.unitActor;
  }

  public boolean shouldLeaveMap()
  {
    InputType action = getInputType();
    return action == InputType.LEAVE_MAP;
  }

  public boolean isTargeting()
  {
    return peekCurrentState().isTargeting();
  }

  /************************************************************
   * Interface so classes can be notified of state changes.
   ************************************************************/
  public static interface StateChangedCallback
  {
    public void onStateChange();
  }
}
