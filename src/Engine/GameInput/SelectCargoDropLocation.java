package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputMode;
import Units.Unit;

/************************************************************
 * State to choose where to drop the unit.                  *
 ************************************************************/
class SelectCargoDropLocation extends GameInputState
{
  Unit myCargo = null;

  public SelectCargoDropLocation(StateData data, Unit newDrop)
  {
    super(data);
    myCargo = newDrop;
  }

  @Override
  protected OptionSet initOptions()
  {
    ArrayList<XYCoord> dropoffLocations = myStateData.actionSet.getTargetedLocations();
    dropoffLocations.removeAll(myStateData.unitLocationMap.values()); // Remove any drop locations that are already reserved.
    return new OptionSet(InputMode.CONSTRAINED_TILE_SELECT, dropoffLocations);
  }

  @Override
  public GameInputState select(XYCoord location)
  {
    GameInputState next = this;

    if( myStateData.actionSet.getTargetedLocations().contains(location) )
    {
      // Add the new dropoff to myStateData.
      myStateData.unitLocationMap.put(myCargo, location);

      // If we have the ability to unload another unit as well, go forward to SelectCargo.
      if( myStateData.unitActor.heldUnits.size() > myStateData.unitLocationMap.size() // More units we could unload.
          && getOptions().getCoordinateOptions().size() > myStateData.unitLocationMap.size() ) // More spaces to drop them in.
      {
        next = new SelectCargo(myStateData);
      }
      else
      {
        // Since we can't drop any additional units, build the GameAction and move to ActionReady.
        GameAction ga = new GameAction.UnloadAction(myStateData.gameMap, myStateData.unitActor, myStateData.path, myStateData.unitLocationMap);

        // Override the current ActionSet with a new one, since we just redefined it.
        myStateData.actionSet = new GameActionSet( ga, true );
        next = new ActionReady(myStateData);
      }
    }

    return next;
  }

  @Override
  public void back()
  {
    myStateData.unitLocationMap.remove(myCargo);
  }
}