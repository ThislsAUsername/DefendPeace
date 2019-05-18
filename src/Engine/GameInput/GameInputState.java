package Engine.GameInput;

import java.util.ArrayList;
import java.util.Map;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.OptionSelector;
import Engine.Path;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;
import Terrain.GameMap;
import Units.Unit;

/************************************************************
 * Abstract base class for all input states.                *
 ************************************************************/
abstract class GameInputState<T>
{
  /** The current GameActionBuilder state data,
   *  shared with all State instances. */
  protected final StateData myStateData;
  protected final OptionSet myOptions;
  protected final OptionSelector mySelector;

  public GameInputState(StateData data)
  {
    myStateData = data;
    myOptions = initOptions();
    mySelector = buildSelector();
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

  protected OptionSelector buildSelector()
  {
    OptionSelector selector = null;
    switch(myOptions.inputType)
    {
      case CONSTRAINED_TILE_SELECT:
        selector = new OptionSelector(myOptions.getCoordinateOptions().size());
        break;
      case MENU_SELECT:
        selector = new OptionSelector(myOptions.getMenuOptions().length);
        break;
      case ACTION_READY:
      case END_TURN:
      case FREE_TILE_SELECT:
      case LEAVE_MAP:
      case PATH_SELECT:
        default:
    }
    return selector;
  }

  public OptionSelector getOptionSelector()
  {
    return mySelector;
  }

  // Default implementations of select() will just keep us
  // in the same state. Subclasses will define transitions.
  public GameInputState<?> select(T option)
  {
    throw new UnsupportedOperationException("Called base GameInputState.select() with input type " + option.getClass());
  }
  
  /** Undo any StateData changes. */
  public void back(){}
}

/************************************************************
 *  Just a struct class to hold information
 *  for easy sharing across State objects.
 ************************************************************/
class StateData
{
  public final GameMap gameMap;
  public final Commander commander;
  public Unit unitActor = null;
  public GameActionSet actionSet = null;
  public Path path = null;
  public ArrayList<? extends Object> menuOptions = null; // Just require a toString().
  public Map<Unit, XYCoord> unitLocationMap = null; // Used to map units to unload locations.
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
class OptionSet
{
  public final InputType inputType;
  private ArrayList<XYCoord> myCoords = null;
  private Object[] myMenuOptions = null;
  private GameAction myAction = null;

  public OptionSet(InputType type)
  {
    inputType = type;
  }

  public OptionSet(InputType type, ArrayList<XYCoord> coords)
  {
    inputType = type;
    myCoords = coords;
  }

  public OptionSet(Object[] menuOptions)
  {
    inputType = InputType.MENU_SELECT;
    myMenuOptions = menuOptions;
  }

  public OptionSet(GameAction action)
  {
    inputType = InputType.ACTION_READY;
    myAction = action;
  }

  public Object[] getMenuOptions()
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