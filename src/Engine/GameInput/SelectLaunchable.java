package Engine.GameInput;

import java.util.ArrayList;

import Units.Unit;

/************************************************************
 * State to choose which Unit will be kicked off the bus.   *
 ************************************************************/
class SelectLaunchable extends GameInputState<Unit>
{
  public SelectLaunchable(StateData data)
  {
    super(data);
    myStateData.unitLauncher = myStateData.unitActor;
  }

  @Override
  protected OptionSet initOptions()
  {
    // Collect the units held by unitActor.
    ArrayList<Unit> launchables = new ArrayList<Unit>();
    for( Unit cargo : myStateData.unitActor.heldUnits )
    {
      if( !cargo.isTurnOver )
        launchables.add(cargo);
    }
    Unit[] cargoArray = new Unit[launchables.size()];
    launchables.toArray(cargoArray);
    return new OptionSet(cargoArray);
  }

  @Override
  public GameInputState<?> select(Unit option)
  {
    GameInputState<?> next = this;

    next = SelectMoveLocation.build(myStateData, option, myStateData.unitCoord);

    return next;
  }

  @Override
  public void back()
  {
    myStateData.unitActor = myStateData.unitLauncher;
    myStateData.unitLauncher = null;
  }
}