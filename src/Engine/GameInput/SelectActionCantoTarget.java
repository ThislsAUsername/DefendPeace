package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;
import Terrain.GameMap;
import Units.GBAFEActions;
import Units.Unit;
import Units.UnitModel;

class SelectActionCantoTarget extends SelectActionTarget
{
  public SelectActionCantoTarget(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    GameMap map = myStateData.gameMap;
    GamePath movePath = myStateData.path;
    XYCoord moveLocation = movePath.getEndCoord();
    Unit actor = myStateData.unitActor;

    ArrayList<XYCoord> pickupLocations = new ArrayList<>();
    ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, 1, 1);

    for( XYCoord loc : locations )
    {
      Unit other = map.getLocation(loc).getResident();
      if( other != null
          && !actor.CO.isEnemy(other.CO) && other != actor
          && canSupport(map, actor, movePath, other)
          )
      {
        pickupLocations.add(loc);
      }
    }

    return new OptionSet(InputType.CONSTRAINED_TILE_SELECT, pickupLocations);
  }
  public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
  {
    GameAction selectedAction = myStateData.actionSet.getSelected();
    if( GBAFEActions.RescueUnitFactory.instance == selectedAction.getType() )
      return !other.model.isAny(UnitModel.SHIP) && !(other.heldUnits.size() > 0);
    // else TAKE
    return other.heldUnits.size() > 0 && !(other.heldUnits.get(0).heldUnits.size() > 0);
  }

  @Override
  public GameInputState<?> select(XYCoord targetLocation)
  {
    GameInputState<?> next = this;

    Unit target = myStateData.gameMap.getResident(targetLocation);

    UnitActionFactory actionType = myStateData.actionSet.getSelected().getType();

    if( GBAFEActions.RescueUnitFactory.instance == actionType )
      next = SelectMoveCantoLocation.build(myStateData, myStateData.unitActor, myStateData.path.getEndCoord(), target, null);
    if( GBAFEActions.TakeUnitFactory.instance == actionType )
      next = SelectCantoDropLocation.build(myStateData, target, target.heldUnits.get(0));

    return next;
  }

  @Override
  public void consider(XYCoord coord)
  {
    // Overriding because the parent consider() barfs on null
  }
}