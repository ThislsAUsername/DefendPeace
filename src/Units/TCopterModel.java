package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;

public class TCopterModel extends AirModel
{

  private static final ActionType[] actions = { ActionType.UNLOAD, ActionType.WAIT };

  public TCopterModel()
  {
    super("T Copter", UnitEnum.T_COPTER, ChassisEnum.AIR_LOW, 5000, 99, 2, 6, actions, null);
    holdingCapacity = 1;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }
}
