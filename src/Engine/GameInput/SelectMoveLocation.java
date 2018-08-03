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
    // Get valid move locations and highlight the relevant map tiles.
    ArrayList<XYCoord> moveLocations = Utils.findPossibleDestinations(myStateData.unitActor, myStateData.gameMap);
    for( XYCoord xy : moveLocations )
    {
      myStateData.gameMap.getLocation(xy.xCoord, xy.yCoord).setHighlight(true);
    }
    // Build and return our OptionSet.
    return new OptionSet(InputType.PATH_SELECT, moveLocations);
  }

  @Override
  public GameInputState select(Path path)
  {
    GameInputState next = this;
    if( myStateData.unitActor.CO != myStateData.commander )
    {
      // We'll be changing state, so unset the map highlights.
      for( XYCoord xy : myOptions.getCoordinateOptions() )
      {
        myStateData.gameMap.getLocation(xy.xCoord, xy.yCoord).setHighlight(false);
      }

      next = null;
    }
    else if( myOptions.getCoordinateOptions().contains(new XYCoord(path.getEnd().x, path.getEnd().y))
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

  @Override
  public void back()
  {
    myStateData.path = null;
  }
}