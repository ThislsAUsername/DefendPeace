package Units;

import Engine.UnitActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.CopterMGun;
import Units.Weapons.CopterRockets;
import Units.Weapons.WeaponModel;

public class BCopterModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 9000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 2;
  private static final int VISION_RANGE = 3;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new Flight();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new CopterRockets(), new CopterMGun() };

  public BCopterModel()
  {
    super("B Copter", UnitEnum.B_COPTER, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
