package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class TCopterModel extends UnitModel
{
  private static final int UNIT_COST = 5000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 2;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new Flight();
  private static final ActionType[] actions = { ActionType.UNLOAD, ActionType.WAIT };

  public TCopterModel()
  {
    super("T Copter", UnitEnum.T_COPTER, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, MOVE_POWER, moveType, actions, new WeaponModel[0]);
    holdingCapacity = 1;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }
}
