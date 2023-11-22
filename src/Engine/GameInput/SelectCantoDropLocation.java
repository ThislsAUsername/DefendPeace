package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameInput.GameInputHandler.InputType;
import Terrain.GameMap;
import Units.GBAFEActions;
import Units.Unit;

class SelectCantoDropLocation extends GameInputState<XYCoord>
{
  Unit takenFrom, myDrop;

  private SelectCantoDropLocation(StateData data, Unit takenFrom, Unit newDrop)
  {
    super(data);
    this.takenFrom = takenFrom;
    myDrop = newDrop;
  }
  public static SelectCantoDropLocation build(StateData data, Unit takenFrom, Unit newDrop)
  {
    // The joys of data smuggling
    data.unitLauncher = data.unitActor;
    data.unitActor = newDrop;
    SelectCantoDropLocation next = new SelectCantoDropLocation(data, takenFrom, newDrop);

    data.unitActor = data.unitLauncher;
    data.unitLauncher = null;
    return next;
  }

  @Override
  protected OptionSet initOptions()
  {
    GameMap map = myStateData.gameMap;
    GamePath movePath = myStateData.path;
    Unit actor = myStateData.unitLauncher;
    ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, actor, movePath.getEndCoord(), myStateData.unitActor);

    GameAction action = myStateData.actionSet.getSelected();
    UnitActionFactory actionType = action.getType();
    if( GBAFEActions.TakeUnitFactory.instance == actionType )
    {
      dropoffLocations.add(myStateData.path.getEndCoord()); // Enable choosing to not drop a freshly TAKEn unit
      GBAFEActions.TakeUnitAction takeAction = (GBAFEActions.TakeUnitAction) action;
      GameAction nonDrop = new GBAFEActions.TakeUnitAction(actor, movePath, takeAction.target, takeAction.cargo, null, takeAction.canto);
      myStateData.actionSet.getGameActions().add(nonDrop);
      myStateData.actionSet = new GameActionSet(myStateData.actionSet.getGameActions(), true);
    }
    return new OptionSet(InputType.CONSTRAINED_TILE_SELECT, dropoffLocations);
  }

  @Override
  public GameInputState<?> select(XYCoord inputLocation)
  {
    XYCoord targetLocation = inputLocation;
    if( targetLocation.equals(myStateData.path.getEndCoord()) )
      targetLocation = null;
    // Sync up our target with the actionSet, so SMCL can use the actionSet's target
    for( int i = 0; i < myStateData.actionSet.getTargetedLocations().size(); ++i )
    {
      GameAction selectedAction = myStateData.actionSet.getSelected();
      XYCoord actionTarget = selectedAction.getTargetLocation();
      if( targetLocation == null )
      {
        if( null == actionTarget )
          break;
        myStateData.actionSet.next();
        continue;
      }
      if( targetLocation.equals(actionTarget) )
        break;
      myStateData.actionSet.next();
    }

    GameInputState<?> next = SelectMoveCantoLocation.build(myStateData, myStateData.unitActor, myStateData.path.getEndCoord(), takenFrom, myDrop);

    return next;
  }

  @Override
  public boolean isTargeting()
  {
    return true;
  }
}