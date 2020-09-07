package Engine.GameInput;

import java.util.ArrayList;

import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;
import Units.Unit;

/************************************************************
 * State to allow choosing a unit's path.                   *
 ************************************************************/
class SelectMoveLocation extends GameInputState<XYCoord>
{
  public final boolean canEndOnOccupied = true;
  private XYCoord oldUnitCoord;

  private SelectMoveLocation(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    // Get valid move locations and return our OptionSet.
    ArrayList<XYCoord> moveLocations = 
        Utils.findPossibleDestinations(myStateData.unitCoord, myStateData.unitActor,
                                       myStateData.gameMap, canEndOnOccupied);
    if (null != myStateData.unitLauncher)
      moveLocations.remove(myStateData.unitCoord); // Prevent returning to the spot of the launch
    buildMovePath(myStateData.unitCoord.xCoord, myStateData.unitCoord.yCoord);
    return new OptionSet(InputType.PATH_SELECT, moveLocations);
  }

  @Override
  public void consider(XYCoord coord)
  {
    buildMovePath(coord.xCoord, coord.yCoord);
  }
  @Override
  public GameInputState<?> select(XYCoord coord)
  {
    GameInputState<?> next = this;
    if( myStateData.unitActor.CO != myStateData.commander )
    {
      // Tell the handler to go back to the previous state.
      next = null;
    }
    else if( (null != myStateData.path) && (myStateData.path.getPathLength() > 0)
        && myOptions.getCoordinateOptions().contains(myStateData.path.getEndCoord())
        && myStateData.path.getEndCoord().equals(coord)
        && Utils.isPathValid(myStateData.unitCoord, myStateData.unitActor, myStateData.path, myStateData.gameMap, canEndOnOccupied) )
    {
      // Construct the next state instance.
      next = new SelectUnitAction(myStateData);
      if (next.mySelector.size() < 1)
        next = this; // If there ain't no actions, don't do nuthin'
    }
    return next;
  }

  public static SelectMoveLocation build(StateData data, Unit mover, XYCoord startCoord)
  {
    // Can have more than one of these on the stack, so we need to persist the unit coord
    XYCoord oldUnitCoord = data.unitCoord;

    data.unitActor = mover;
    data.unitCoord = startCoord;
    data.path = new Path();

    SelectMoveLocation next = new SelectMoveLocation(data);
    next.oldUnitCoord = oldUnitCoord;

    return next;
  }
  @Override
  public void back()
  {
    myStateData.path = null;
    myStateData.unitActor = null;
    myStateData.unitCoord = oldUnitCoord;
  }

  /**
   * Constructs a unit's movement path, one tile at a time, as the user moves the cursor around the map.
   * If the current movement path is impossible, it will attempt to regenerate a path from scratch.
   */
  private void buildMovePath(int x, int y)
  {
    if( null == myStateData.path )
    {
      myStateData.path = new Path();
    }

    // If the new point already exists on the path, cut the extraneous points out.
    for( int i = 0; i < myStateData.path.getPathLength(); ++i )
    {
      if( myStateData.path.getWaypoint(i).x == x && myStateData.path.getWaypoint(i).y == y )
      {
        myStateData.path.snip(i);
        break;
      }
    }

    myStateData.path.addWaypoint(x, y);

    Unit actor = myStateData.unitActor;
    XYCoord coord = myStateData.unitCoord;
    boolean canEndOnOccupied = true;
    if( !Utils.isPathValid(coord, actor, myStateData.path, myStateData.gameMap, canEndOnOccupied) )
    {
      // The currently-built path is invalid. Try to generate a new one (may still return null).
      myStateData.path = Utils.findShortestPath(coord, actor, x, y, myStateData.gameMap);
    }
  }
}