package Engine;

import java.util.ArrayList;
import java.util.Stack;

import CommandingOfficers.Commander;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;

/************************************************************
 * Handles converting user input into game actions.
 ************************************************************/
public class InputStateHandler
{
  private StateData myStateData = null;
  private Stack<State> myStateStack = null;

  public enum InputMode { FREE_TILE_SELECT, PATH_SELECT, MENU_SELECT, CONSTRAINED_TILE_SELECT, ACTION_READY };

  public InputStateHandler(GameMap map, Commander currentPlayer)
  {
    myStateStack = new Stack<State>();
    myStateData = new StateData(map, currentPlayer);
    myStateStack.push(new DefaultState(myStateData));
  }

  /**
   * Back up to the previous state.
   */
  public State back()
  {
    State previousState = null;
    if( !myStateStack.isEmpty() )
    {
      previousState = myStateStack.pop();
    }
    // If the stack is now empty, put this guy back again.
    // The last one should be DefaultState.
    if( myStateStack.isEmpty() )
    {
      myStateStack.push(previousState);
    }
    return previousState;
  }

  /**
   * Choose the passed-in option for the current state, triggering a transition to the
   * next state. If no transition is possible, the state will not change.
   * @param option - The chosen menu option, from among those provided by OptionSet.getMenuOptions().
   * @return The OptionSet for the next (and now current) state.
   */
  public InputMode select(String option)
  {
    State current = peekCurrentState();
    State next = current.select(option);
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
    State current = peekCurrentState();
    State next = current.select(coord);
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
    State current = peekCurrentState();
    State next = current.select(path);
    pushNextState(current, next);
    return next.getOptions().inputMode;
  }

  public InputMode reset()
  {
    // Unwind the stack, all the way back to the starting state.
    while(myStateStack.size() > 1) back();
    return peekCurrentState().getOptions().inputMode;
  }

  /** Get the current state, but don't pop it off the stack. */
  private State peekCurrentState()
  {
    if( myStateStack.isEmpty() )
    {
      System.out.println("WARNING! GameActionBuilder has no state active! Creating default.");
      myStateStack.push(new DefaultState(myStateData));
    }
    return myStateStack.peek();
  }

  /** Push next onto the state stack if it is not the same object as current. */
  private void pushNextState(State current, State next)
  {
    if(current != next)
    {
      myStateStack.push(next);
    }
  }

  /** @return The action to execute, if one is ready, or null if not. */
  public GameAction getReadyAction()
  {
    return peekCurrentState().getOptions().getAction();
  }

  /** @return The currently-valid menu-option strings, or null if no menu is active. */
  public String[] getMenuOptions()
  {
    return peekCurrentState().getOptions().myMenuOptions;
  }

  /** @return The currently-recommended input mode. */
  public InputMode getInputMode()
  {
    return peekCurrentState().getOptions().inputMode;
  }

  /** @return The current set of coordinates from which to choose, or null we are not selecting a location. */
  public ArrayList<XYCoord> getCoordinateOptions()
  {
    return peekCurrentState().getOptions().myCoords;
  }

  /************************************************************
   *  Just a struct class to hold information
   *  for easy sharing across State objects.
   ************************************************************/
  private class StateData
  {
    public final GameMap gameMap;
    public final Commander commander;
    public Unit unitActor = null;
    public GameActionSet actionSet = null;
    public Path path = null;
    public ArrayList<? extends Object> menuOptions = null; // Just require a toString().
    public StateData(GameMap map, Commander co)
    {
      gameMap = map;
      commander = co;
    }
  }

  /************************************************************
   * Provides access to the valid options for the current state
   * via its accessors. If an accessor returns null, that input
   * type is not supported in this state.
   * The appropriate accessor can be determined by checking for
   * null or by inspecting inputMode.
   * If inputMode==PATH_SELECT or CONSTRAINED_TILE_SELECT,
   *   then use getCoordinateOptions(). 
   * If inputMode==MENU_SELECT, use getMenuOptions().
   * If inputMode==ACTION_READY, use (and execute) getAction(). 
   ************************************************************/
  private static class OptionSet
  {
    public final InputMode inputMode;
    private ArrayList<XYCoord> myCoords = null;
    private String[] myMenuOptions = null;
    private GameAction myAction = null;

    public OptionSet()
    {
      inputMode = InputMode.FREE_TILE_SELECT;
    }

    public OptionSet(InputMode mode, ArrayList<XYCoord> coords)
    {
      inputMode = mode;
      myCoords = coords;
    }

    public OptionSet(String[] menuOptions)
    {
      inputMode = InputMode.MENU_SELECT;
      myMenuOptions = menuOptions;
    }

    public OptionSet(GameAction action)
    {
      inputMode = InputMode.ACTION_READY;
      myAction = action;
    }

    public String[] getMenuOptions()
    {
      return myMenuOptions;
    }

    public ArrayList<XYCoord> getCoordinateOptions()
    {
      return myCoords;
    }

    public GameAction getAction()
    {
      return myAction;
    }
  }

  /************************************************************
   * Abstract base class for all input states.                *
   ************************************************************/
  private static abstract class State
  {
    /** The current GameActionBuilder state data,
     *  shared with all State instances. */
    protected final StateData myStateData;
    protected final OptionSet myOptions;

    public State(StateData data)
    {
      myStateData = data;
      myOptions = initOptions();
    }

    /** Forces each subclass to create an OptionSet.
     *  You shouldn't need anything but the contents
     *  of myStateData to do this. If you want high-
     *  lighted map tiles in this state, then handle
     *  that here too. */
    protected abstract OptionSet initOptions();

    /** Get an object representing the valid selections that may be
     *  made in the current state. */
    public OptionSet getOptions()
    {
      return myOptions;
    }

    // Default implementations of select() will just keep us
    // in the same state. Subclasses will define transitions.
    public State select(Unit unit)
    {
      return this;
    }
    public State select(Path path)
    {
      return null;
    }
    public State select(String option)
    {
      return this;
    }
    public State select(XYCoord coord)
    {
      return this;
    }
  }

  /************************************************************
   * State for before any user input has been received, or    *
   * immediately after reset() has been called.               *
   ************************************************************/
  private static class DefaultState extends State
  {
    public DefaultState(StateData data)
    {
      super(data);
    }

    @Override
    protected OptionSet initOptions()
    {
      // Use the default OptionSet constructor, which allows free tile selection.
      return new OptionSet();
    }

    @Override
    public State select(XYCoord coord)
    {
      State next = this;
      Location loc = myStateData.gameMap.getLocation(coord);
      Unit resident = loc.getResident();
      if( null != resident && resident.CO == myStateData.commander )
      {
        next = select(resident);
      }
      else if( (loc.getOwner() == myStateData.commander) && myStateData.commander.getShoppingList(loc.getEnvironment().terrainType).size() > 0 )
      {
        ArrayList<UnitModel> buildables = myStateData.commander.getShoppingList(loc.getEnvironment().terrainType);
        myStateData.menuOptions = buildables;
        next = new SelectUnitProduction(myStateData, buildables, coord);
      }
      return next;
    }

    public State select(Unit unit)
    {
      State next = this;
      if( unit != null
          && (!unit.isTurnOver    // If it's our unit and it's ready to go.
              // || unit.CO != myStateData.commander // Also allow checking the move radius of others' units.
              ) )
      {
        myStateData.unitActor = unit;
        next = new SelectMoveLocation(myStateData);
      }
      else
      {
        System.out.println("Invalid unit selected.");
        if( unit == null ) System.out.println("  Unit is null.");
        if( unit.isTurnOver ) System.out.println("  Unit has already moved.");
      }
      return next;
    }
  }

  /************************************************************
   * Presents options for building a unit.                    *
   ************************************************************/
  private static class SelectUnitProduction extends State
  {
    private ArrayList<UnitModel> myUnitModels = null;
    private XYCoord myProductionLocation = null;

    public SelectUnitProduction(StateData data, ArrayList<UnitModel> buildables, XYCoord buildLocation)
    {
      super(data);
      myUnitModels = buildables;
      myProductionLocation = buildLocation;
    }

    @Override
    protected OptionSet initOptions()
    {
      OptionSet options = null;
      if( null != myStateData.menuOptions )
      {
        String[] modelStrings = new String[myStateData.menuOptions.size()];
        for( int i = 0; i < myStateData.menuOptions.size(); ++i )
        {
          modelStrings[i] = myStateData.menuOptions.get(i).toString();
        }
        options = new OptionSet(modelStrings);
      }
      return options;
    }

    @Override
    public State select(String option)
    {
      State next = this;

      if( null != option && null != myUnitModels )
      {
        for( UnitModel model : myUnitModels )
        {
          if( option.equals(model.toString()))
          {
            myStateData.actionSet = new GameActionSet(new GameAction.UnitProductionAction(myStateData.gameMap, myStateData.commander, model, myProductionLocation), false);
            next = new ActionReady(myStateData);
          }
        }
      }

      return next;
    }
  }

  /************************************************************
   * State to allow choosing a unit's path.                   *
   ************************************************************/
  private static class SelectMoveLocation extends State
  {
    public SelectMoveLocation(StateData data)
    {
      super(data);
    }

    @Override
    protected OptionSet initOptions()
    {
      // Get valid move locations and highlight the relevant map tiles.
      ArrayList<XYCoord> moveLocations = Utils.findPossibleDestinations(myStateData.unitActor, myStateData.gameMap);
      for( XYCoord xy : moveLocations )
      {
        myStateData.gameMap.getLocation(xy.xCoord, xy.yCoord).setHighlight(true);
      }
      // Build and return our OptionSet.
      return new OptionSet(InputMode.PATH_SELECT, moveLocations);
    }

    @Override
    public State select(Path path)
    {
      State next = this;
      if( myOptions.getCoordinateOptions().contains(new XYCoord(path.getEnd().x, path.getEnd().y))
          && Utils.isPathValid(myStateData.unitActor, path, myStateData.gameMap) )
      {
        // The path ends on a valid move location, and is traversable by the unit. Store it.
        myStateData.path = path;

        // We'll be changing state, so unset the map highlights.
        for( XYCoord xy : myOptions.getCoordinateOptions() )
        {
          myStateData.gameMap.getLocation(xy.xCoord, xy.yCoord).setHighlight(false);
        }

        // Construct the next state instance.
        next = new SelectUnitAction(myStateData);
      }
      return next;
    }
  }

  /************************************************************
   * State to allow selecting an action for a unit.           *
   ************************************************************/
  private static class SelectUnitAction extends State
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

      // Create our new menu options.
      String[] options = new String[myUnitActions.size()];
      for(int i = 0; i < myUnitActions.size(); ++i)
      {
        options[i] = myUnitActions.get(i).toString();
      }
      return new OptionSet(options);
    }

    @Override
    public State select(String menuOption)
    {
      State next = this;
      GameActionSet chosenSet = null;
      if( null != menuOption )
      {
        for(GameActionSet set : myUnitActions)
        {
          if( set.toString().equals(menuOption))
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
          // We need more input before an action is ready; What kind of input depends on the type of action.
          switch(chosenSet.getSelected().getType())
          {
            case ATTACK:
              // We need to select a target to attack.
              next = new SelectActionTarget(myStateData);
              break;
            case UNLOAD:
              // We need to select a unit to unload.
              //next = new SelectCargo(myStateData);
              break;
          }
        }
      }
      return next;
    }
  }

  /************************************************************
   * Allows selecting an action's target.                     *
   ************************************************************/
  private static class SelectActionTarget extends State
  {
    public SelectActionTarget(StateData data)
    {
      super(data);
    }

    @Override
    protected OptionSet initOptions()
    {
      // Set the target-location highlights.
      myStateData.gameMap.clearAllHighlights();
      ArrayList<XYCoord> targets = myStateData.actionSet.getTargetedLocations();
      for( XYCoord targ : targets )
      {
        myStateData.gameMap.getLocation(targ).setHighlight(true);
      }

      // We can only attack the selected tiles, and they may be disjoint, so use constrained tile select.
      return new OptionSet(InputMode.CONSTRAINED_TILE_SELECT, targets);
    }

    @Override
    public State select(XYCoord targetLocation)
    {
      State next = this;

      // Find the action that this target location belongs to.
      // By virtue of the fact that GameActionSets should be homogenous, and it should be
      // nonsensical to have two actions of the same type targeting the same location, this
      // should be enough to uniquely identify the action we want.
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

  /************************************************************
   * Terminal state - just provides the selected action.      *
   ************************************************************/
  private static class ActionReady extends State
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
}
