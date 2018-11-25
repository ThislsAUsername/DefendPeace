package Engine.GameInput;

import java.util.ArrayList;

import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;

/************************************************************
 * State to allow choosing a unit's path.                   *
 ************************************************************/
class SelectMoveLocation extends GameInputState
{
  public SelectMoveLocation(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    // Get valid move locations and return our OptionSet.
    ArrayList<XYCoord> moveLocations = Utils.findPossibleDestinations(myStateData.unitActor, myStateData.gameMap);
    return new OptionSet(InputType.PATH_SELECT, moveLocations);
  }

  @Override
  public GameInputState select(Path path)
  {
    GameInputState next = this;
    // The input is valid if the unit belongs to the active commander and the chosen path is valid.
    if( (myStateData.unitActor.CO == myStateData.commander) && (null != path) && (path.getPathLength() > 0)
        && myOptions.getCoordinateOptions().contains(new XYCoord(path.getEnd().x, path.getEnd().y))
        && Utils.isPathValid(myStateData.unitActor, path, myStateData.gameMap) )
    {
      // The path ends on a valid move location, and is traversable by the unit. Store it.
      myStateData.path = path;

      // Construct the next state instance.
      next = new SelectUnitAction(myStateData);
    }
    return next;
  }

  @Override
  public void back()
  {
    myStateData.path = null;
  }
}