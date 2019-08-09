package Units;

import Engine.UnitActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.AntiAirMGun;
import Units.Weapons.WeaponModel;

public class AntiAirModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 8000;
  private static final int MAX_FUEL = 60;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_RANGE = 2;
  private static final int MOVE_POWER = 6;
  private static final MoveType moveType = new Tread();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new AntiAirMGun() };

  public AntiAirModel()
  {
    super("Anti-Air", UnitEnum.ANTI_AIR, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
