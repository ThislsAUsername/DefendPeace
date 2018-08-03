package Engine.GameInput;

import java.util.ArrayList;

import Engine.XYCoord;
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
    // Use the default OptionSet constructor, which allows free tile selection.
    return new OptionSet();
  }

  @Override
  public GameInputState select(XYCoord coord)
  {
    GameInputState next = this;
    Location loc = myStateData.gameMap.getLocation(coord);
    Unit resident = loc.getResident();
    if( null != resident )
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

  public GameInputState select(Unit unit)
  {
    GameInputState next = this;
    if( unit != null
        && (!unit.isTurnOver    // If it's our unit and it's ready to go.
            || unit.CO != myStateData.commander // Also allow checking the move radius of others' units.
            ))
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