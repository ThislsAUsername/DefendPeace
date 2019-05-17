package Engine.GameInput;

import java.util.ArrayList;
import java.util.HashMap;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.XYCoord;
import Units.Unit;

/************************************************************
 * State to choose which Unit will be kicked off the bus.   *
 ************************************************************/
public class SelectCargo extends GameInputState<Object> // Object, not Unit, because "DONE" is a valid option
{
  private static final String DONE_OPTION = "DONE";
  public SelectCargo(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    // Collect the names of all units held by unitActor.
    ArrayList<Object> cargoes = new ArrayList<Object>();
    for( int i = 0; i < myStateData.unitActor.heldUnits.size(); ++i )
    {
      // Don't include the unit if it's already set to be unloaded.
      if( (null == myStateData.unitLocationMap) || !myStateData.unitLocationMap.containsKey(myStateData.unitActor.heldUnits.get(i)) )
      {
        cargoes.add( myStateData.unitActor.heldUnits.get(i) );
      }
    }
    if( null != myStateData.unitLocationMap && myStateData.unitLocationMap.size() > 0 )
    {
      // If at least one unit is being offboarded already, offer a "DONE" option so we aren't forced to unload everything.
      cargoes.add(DONE_OPTION);
    }
    Object[] cargoArray = new Object[cargoes.size()];
    cargoes.toArray(cargoArray);
    return new OptionSet(cargoArray);
  }

  @Override
  public GameInputState<?> select(Object option)
  {
    GameInputState<?> next = this;

    // Add a unitLocationMap to our state if we don't have one already.
    if( null == myStateData.unitLocationMap )
    {
      myStateData.unitLocationMap = new HashMap<Unit, XYCoord>();
    }

    if( option.equals(DONE_OPTION) )
    {
      // Since we don't want to drop any additional units, build the GameAction and move to ActionReady.
      GameAction ga = new GameAction.UnloadAction(myStateData.unitActor, myStateData.path, myStateData.unitLocationMap);

      // Override the current ActionSet with a new one, since we just redefined it.
      myStateData.actionSet = new GameActionSet( ga, true );
      next = new ActionReady(myStateData);
    }
    for( Unit cargo : myStateData.unitActor.heldUnits )
    {
      if(cargo == option)
      {
        next = new SelectCargoDropLocation(myStateData, cargo);
        break;
      }
    }

    return next;
  }

  @Override
  public void back()
  {
    if( null != myStateData.unitLocationMap && myStateData.unitLocationMap.isEmpty() )
    {
      myStateData.unitLocationMap = null;
    }
  }
}