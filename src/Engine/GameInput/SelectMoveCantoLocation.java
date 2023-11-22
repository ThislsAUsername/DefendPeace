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

/************************************************************
 * State to allow choosing a unit's path.                   *
 ************************************************************/
class SelectMoveCantoLocation extends SelectMoveLocation
{
  public final boolean canEndOnOccupied = true;
  Unit takenFrom, myDrop;
  XYCoord dropLoc; // This should *not* be initialized here; it's actually set in the parent constructor via initOptions
  GamePath oldPath;

  private SelectMoveCantoLocation(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    GameMap map = myStateData.gameMap;
    GamePath movePath = (null == oldPath)? myStateData.path : oldPath;
    Unit actor = myStateData.unitActor;
    // very We Enjoy Typing around here
    boolean includeOccupiedDestinations = canEndOnOccupied;
    boolean canTravelThroughEnemies = false;
    int movePoints = actor.getMovePower(map) - movePath.getMoveCost(actor, map);
    ArrayList<XYCoord> destinations = Utils.findFloodFillArea(movePath.getEndCoord(), actor, actor.CO.army, actor.getMoveFunctor(),
                                   Math.min(movePoints, actor.fuel), map, includeOccupiedDestinations, canTravelThroughEnemies);

    GameAction selectedAction = myStateData.actionSet.getSelected();
    if( GBAFEActions.RescueUnitFactory.instance != selectedAction.getType() )
      dropLoc = selectedAction.getTargetLocation(); // Non-Rescue actions target a drop location
    if( null != dropLoc )
      destinations.remove(dropLoc); // Prevent canto'ing onto our drop location
    buildMovePath(myStateData.unitCoord);
    return new OptionSet(InputType.PATH_SELECT, destinations);
  }

  @Override
  public GameInputState<?> select(XYCoord coord)
  {
    boolean valid = true;
    valid &= (null != myStateData.path) && (myStateData.path.getPathLength() > 0);
    valid &= myOptions.getCoordinateOptions().contains(myStateData.path.getEndCoord());
    valid &= myStateData.path.getEndCoord().equals(coord);
    valid &= Utils.isPathValid(myStateData.unitActor, myStateData.path, myStateData.gameMap, canEndOnOccupied);

    Unit resident = myStateData.gameMap.getResident(coord);
    if( resident != null )
    {
      if( null == myDrop )
      {
        // This is a rescue, so we can walk onto our victim
        boolean validTile = false;
        validTile |= resident == takenFrom;
        validTile |= resident == myStateData.unitActor;
        valid &= validTile;
      }
      else
        // This is either TAKE or DROP, so we aren't emptying any space other than our starting space.
        valid &= resident == myStateData.unitActor;
    }

    GameInputState<?> next = this;
    if( !valid )
      return next;

    // Build an action of the original type with our knowledge gathered from the user.
    // We'll override the current ActionSet with a new one, since we just redefined it.
    UnitActionFactory actionType = myStateData.actionSet.getSelected().getType();

    if( GBAFEActions.RescueUnitFactory.instance == actionType )
    {
      GameAction ga = new GBAFEActions.RescueUnitAction(myStateData.unitActor, oldPath, takenFrom, myStateData.path);
      myStateData.actionSet = new GameActionSet( ga, true );
      next = new ActionReady(myStateData);
    }
    else if( GBAFEActions.DropUnitFactory.instance == actionType )
    {
      GameAction ga = new GBAFEActions.DropUnitAction(myStateData.unitActor, oldPath, myDrop, dropLoc, myStateData.path);
      myStateData.actionSet = new GameActionSet( ga, true );
      next = new ActionReady(myStateData);
    }
    else if( GBAFEActions.TakeUnitFactory.instance == actionType )
    {
      GameAction ga = new GBAFEActions.TakeUnitAction(myStateData.unitActor, oldPath, takenFrom, myDrop, dropLoc, myStateData.path);
      myStateData.actionSet = new GameActionSet( ga, true );
      next = new ActionReady(myStateData);
    }

    return next;
  }

  public static SelectMoveCantoLocation build(StateData data, Unit mover, XYCoord cantoStartCoord, Unit takenFrom, Unit newDrop)
  {
    // Can have more than one of these on the stack, so we need to persist the unit coord
    XYCoord oldUnitCoord = data.unitCoord;
    GamePath oldPath = data.path;

    data.unitActor = mover;
    data.unitCoord = cantoStartCoord;
    // We need to smuggle the old path into initOptions, so don't reset this just yet.
    //data.path      = new GamePath();

    SelectMoveCantoLocation next = new SelectMoveCantoLocation(data);
    next.oldUnitCoord = oldUnitCoord;

    next.takenFrom = takenFrom;
    next.myDrop    = newDrop;
    next.oldPath   = oldPath;

    return next;
  }
  @Override
  public void back()
  {
    myStateData.path = oldPath;
    myStateData.unitCoord = oldUnitCoord;
  }
}