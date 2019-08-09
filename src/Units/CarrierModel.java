package Units;

import java.util.Vector;

import Engine.UnitActionType;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.CarrierMissiles;
import Units.Weapons.WeaponModel;

public class CarrierModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 30000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int VISION_RANGE = 4;
  private static final int MOVE_POWER = 5;

  private static final MoveType moveType = new FloatHeavy();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new CarrierMissiles() };

  public CarrierModel()
  {
    super("Carrier", UnitEnum.CARRIER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
    holdingCapacity = 2;
    UnitEnum[] carryable = { UnitEnum.B_COPTER, UnitEnum.T_COPTER, UnitEnum.FIGHTER, UnitEnum.BOMBER, UnitEnum.STEALTH, UnitEnum.BBOMB };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }
}
