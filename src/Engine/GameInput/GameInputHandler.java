package Engine.GameInput;

import java.util.ArrayList;
import java.util.Stack;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.Path;
import Engine.XYCoord;
import Terrain.GameMap;

/************************************************************
 * Handles converting user input into game actions.
 ************************************************************/
public class GameInputHandler
{
  private StateData myStateData = null;
  private Stack<GameInputState> myStateStack = null;
  private StateChangedCallback myCallback = null;

  public enum InputMode { FREE_TILE_SELECT, PATH_SELECT, MENU_SELECT, CONSTRAINED_TILE_SELECT, ACTION_READY };

  public GameInputHandler(GameMap map, Commander currentPlayer, StateChangedCallback callback)
  {
    myStateStack = new Stack<GameInputState>();
    myStateData = new StateData(map, currentPlayer);
    myStateStack.push(new DefaultState(myStateData));
    myCallback = callback;
  }

  public GameInputHandler(GameMap map, Commander currentPlayer)
  {
    this(map, currentPlayer, null);
  }

  /**
   * Back up to the previous state.
   */
  public GameInputState back()
  {
    GameInputState oldCurrentState = null;
    GameInputState newCurrentState = null;
    if( !myStateStack.isEmpty() )
    {
      oldCurrentState = myStateStack.pop();
      oldCurrentState.back(); // Undo any StateData changes.
    }
    else
    {
      System.out.println("WARNING! InputStateHandler state stack is empty!");
      oldCurrentState = new DefaultState(myStateData);
    }

    // If the stack is now empty, put this guy back again.
    // The last one should be DefaultState.
    if( myStateStack.isEmpty() )
    {
      myStateStack.push(oldCurrentState);
    }

    // Set newCurrentState to whatever is now at the top of th stack.
    newCurrentState = peekCurrentState();

    // If we are in a different state now than before, notify the callback.
    if( oldCurrentState != newCurrentState )
    {
      myCallback.onStateChange();
    }
    return newCurrentState;
  }

  /**
   * Choose the passed-in option for the current state, triggering a transition to the
   * next state. If no transition is possible, the state will not change.
   * @param option - The chosen menu option, from among those provided by OptionSet.getMenuOptions().
   * @return The OptionSet for the next (and now current) state.
   */
  public InputMode select(Object option)
  {
    GameInputState current = peekCurrentState();
    GameInputState next = current.select(option);
    pushNextState(current, next);
    return next.getOptions().inputMode;
  }

  /**
   * Choose the passed-in option for the current state, triggering a transition to the
   * next state. If no transition is possible, the state will not change.
   * @param option - The chosen coordinate, drawn from those given by OptionSet.getCoordinateOptions().
   * @return The OptionSet for the next (and now current) state.
   */
  public InputMode select(XYCoord coord)
  {
    GameInputState current = peekCurrentState();
    GameInputState next = current.select(coord);
    pushNextState(current, next);
    return next.getOptions().inputMode;
  }

  /**
   * Choose the passed-in option for the current state, triggering a transition to the
   * next state. If no transition is possible, the state will not change.
   * @param option - The chosen coordinate, drawn from those given by OptionSet.getCoordinateOptions().
   * @return The OptionSet for the next (and now current) state.
   */
  public InputMode select(Path path)
  {
    GameInputState current = peekCurrentState();
    GameInputState next = current.select(path);
    pushNextState(current, next);
    return next.getOptions().inputMode;
  }

  public InputMode reset()
  {
    // Unwind the stack, all the way back to the starting state.
    myStateStack.clear();
    myStateData = new StateData(myStateData.gameMap, myStateData.commander);
    myStateStack.push(new DefaultState(myStateData));
    return peekCurrentState().getOptions().inputMode;
  }

  /** Get the current state, but don't pop it off the stack. */
  private GameInputState peekCurrentState()
  {
    if( myStateStack.isEmpty() )
    {
      System.out.println("WARNING! GameActionBuilder has no state active! Creating default.");
      myStateStack.push(new DefaultState(myStateData));
    }
    return myStateStack.peek();
  }

  /** Push next onto the state stack if it is not the same object as current. */
  private void pushNextState(GameInputState current, GameInputState next)
  {
    if(current != next)
    {
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

  /** @return The currently-recommended input mode. */
  public InputMode getInputMode()
  {
    return peekCurrentState().getOptions().inputMode;
  }

  /** @return The current set of coordinates from which to choose, or null we are not selecting a location. */
  public ArrayList<XYCoord> getCoordinateOptions()
  {
    return peekCurrentState().getOptions().getCoordinateOptions();
  }

  /************************************************************
   * Interface so classes can be notified of state changes.
   ************************************************************/
  public static interface StateChangedCallback
  {
    public void onStateChange();
  }
}
