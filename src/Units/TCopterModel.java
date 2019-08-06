package Units;

import java.util.Vector;

import Engine.UnitActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class TCopterModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 5000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 2;
  private static final int VISION_RANGE = 2;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new Flight();
  private static final UnitActionType[] actions = UnitActionType.TRANSPORT_ACTIONS;

  public TCopterModel()
  {
    super("T Copter", UnitEnum.T_COPTER, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, new WeaponModel[0]);
    holdingCapacity = 1;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }
}
