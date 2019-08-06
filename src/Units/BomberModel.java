package Units;

import Engine.UnitActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.BomberBombs;
import Units.Weapons.WeaponModel;

public class BomberModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 22000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 5;
  private static final int VISION_RANGE = 2;
  private static final int MOVE_POWER = 7;

  private static final MoveType moveType = new Flight();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new BomberBombs() };

  public BomberModel()
  {
    super("Bomber", UnitEnum.BOMBER, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
