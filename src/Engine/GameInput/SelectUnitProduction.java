package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.XYCoord;
import Units.UnitModel;

/************************************************************
 * Presents options for building a unit.                    *
 ************************************************************/
class SelectUnitProduction extends GameInputState
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
      String[] modelStrings = new String[myStateData.menuOptions.size()];
      for( int i = 0; i < myStateData.menuOptions.size(); ++i )
      {
        modelStrings[i] = myStateData.menuOptions.get(i).toString();
      }
      options = new OptionSet(modelStrings);
    }
    return options;
  }

  @Override
  public GameInputState select(Object option)
  {
    GameInputState next = this;

    if( null != option && null != myUnitModels )
    {
      for( UnitModel model : myUnitModels )
      {
        if( option.equals(model.toString()))
        {
          myStateData.actionSet = new GameActionSet(new GameAction.UnitProductionAction(myStateData.gameMap, myStateData.commander, model, myProductionLocation), false);
          next = new ActionReady(myStateData);
        }
      }
    }

    return next;
  }
}