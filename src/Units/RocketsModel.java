package Units;

import Engine.UnitActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.Weapons.RocketRockets;
import Units.Weapons.WeaponModel;

public class RocketsModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 15000;
  private static final int MAX_FUEL = 50;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_RANGE = 1;
  private static final int MOVE_POWER = 5;

  private static final MoveType moveType = new Tires();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new RocketRockets() };

  public RocketsModel()
  {
    super("Rockets", UnitEnum.ROCKETS, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
