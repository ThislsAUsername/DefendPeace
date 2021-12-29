package Engine.GameInput;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;
import Terrain.MapLocation;
import Units.Unit;
import Units.UnitModel;

/************************************************************
 * State for before any user input has been received, or    *
 * immediately after reset() has been called.               *
 ************************************************************/
class DefaultState extends GameInputState<XYCoord>
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
  public GameInputState<?> select(XYCoord coord)
  {
    GameInputState<?> next = this;
    MapLocation loc = myStateData.gameMap.getLocation(coord);
    Unit resident = loc.getResident();
    if( null != resident
        && (!resident.isTurnOver    // If it's our unit and the unit is ready to go.
        || resident.CO.army != myStateData.army // Also allow checking the move radius of others' units.
        ))
    {
      // We are considering moving a unit.
      next = SelectMoveLocation.build(myStateData, resident, new XYCoord(resident.x, resident.y));
    }
    else if( myStateData.army.canBuyOn(loc) )
    {
      // We are considering a new unit purchase.
      Commander buyer = loc.getOwner();
      ArrayList<UnitModel> buildables = buyer.getShoppingList(loc);
      myStateData.menuOptions = SelectUnitProduction.buildDisplayStrings(buyer, buildables, coord);
      next = new SelectUnitProduction(myStateData, buyer, buildables, coord);
    }
    else
    {
      // Other options go here.
      next = new SelectMetaAction(myStateData);
    }
    return next;
  }
}