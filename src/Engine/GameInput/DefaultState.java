package Engine.GameInput;

import java.util.ArrayList;

import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;

/************************************************************
 * State for before any user input has been received, or    *
 * immediately after reset() has been called.               *
 ************************************************************/
class DefaultState extends GameInputState
{
  public DefaultState(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    // Default state - they can just click anywhere.
    return new OptionSet(InputType.FREE_TILE_SELECT);
  }

  @Override
  public GameInputState select(XYCoord coord)
  {
    GameInputState next = this;
    Location loc = myStateData.gameMap.getLocation(coord);
    Unit resident = loc.getResident();
    if( null != resident
        && (!resident.isTurnOver    // If it's our unit and the unit ready to go.
        || resident.CO != myStateData.commander // Also allow checking the move radius of others' units.
        ))
    {
      // We are considering moving a unit.
      myStateData.unitActor = resident;
      next = new SelectMoveLocation(myStateData);
    }
    else if( (loc.getOwner() == myStateData.commander) && myStateData.commander.getShoppingList(loc.getEnvironment().terrainType).size() > 0 )
    {
      // We are considering a new unit purchase.
      ArrayList<UnitModel> buildables = myStateData.commander.getShoppingList(loc.getEnvironment().terrainType);
      myStateData.menuOptions = buildables;
      next = new SelectUnitProduction(myStateData, buildables, coord);
    }
    else
    {
      // Other options go here.
      next = new SelectMetaAction(myStateData);
    }
    return next;
  }
}