package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.XYCoord;
import Units.UnitModel;

/************************************************************
 * Presents options for building a unit.                    *
 ************************************************************/
class SelectUnitProduction extends GameInputState<UnitModel>
{
  private ArrayList<UnitModel> myUnitModels = null;
  private XYCoord myProductionLocation = null;

  public SelectUnitProduction(StateData data, ArrayList<UnitModel> buildables, XYCoord buildLocation)
  {
    super(data);
    myUnitModels = buildables;
    myProductionLocation = buildLocation;
  }

  @Override
  protected OptionSet initOptions()
  {
    OptionSet options = null;
    if( null != myStateData.menuOptions )
    {
      options = new OptionSet(myStateData.menuOptions.toArray());
    }
    return options;
  }

  @Override
  public GameInputState<?> select(UnitModel option)
  {
    GameInputState<?> next = this;

    if( null != option && null != myUnitModels )
    {
      for( UnitModel model : myUnitModels )
      {
        if( option == model )
        {
          myStateData.actionSet = new GameActionSet(new GameAction.UnitProductionAction(myStateData.commander, model, myProductionLocation), false);
          next = new ActionReady(myStateData);
        }
      }
    }

    return next;
  }
}