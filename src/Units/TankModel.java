package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.TankCannon;
import Units.Weapons.TankMGun;
import Units.Weapons.WeaponModel;

public class TankModel extends UnitModel
{
  private static final int UNIT_COST = 7000;
  private static final int MAX_FUEL = 70;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new TankCannon(), new TankMGun() };

  public TankModel()
  {
    super("Tank", UnitEnum.TANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, MOVE_POWER, moveType, actions, weapons);
  }
}
