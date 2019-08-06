package Units;

import Engine.UnitActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.Weapons.ReconMGun;
import Units.Weapons.WeaponModel;

public class ReconModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 4000;
  private static final int MAX_FUEL = 80;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_RANGE = 5;
  private static final int MOVE_POWER = 8;

  private static final MoveType moveType = new Tires();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new ReconMGun() };

  public ReconModel()
  {
    super("Recon", Units.UnitModel.UnitEnum.RECON, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
