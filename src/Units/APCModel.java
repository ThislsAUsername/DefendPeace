package Units;

import java.util.ArrayList;
import java.util.Vector;

import Engine.GameAction.ActionType;
import Engine.TurnInitAction;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;

public class APCModel extends UnitModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.UNLOAD, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };

  public APCModel()
  {
    super("APC", UnitEnum.APC, ChassisEnum.TRUCK, 5000, 70, 0, 6, moveType, actions, healHabs, null);
    holdingCapacity = 1;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }

  @Override
  public void getTurnInitActions(ArrayList<TurnInitAction> actions)
  {
    actions.add(new TurnInitAction.ResupplyAction());
  }
}
