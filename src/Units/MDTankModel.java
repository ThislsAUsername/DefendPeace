package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.MDTankCannon;
import Units.Weapons.MDTankMGun;
import Units.Weapons.WeaponModel;

public class MDTankModel extends UnitModel
{
  private static final int UNIT_COST = 16000;
  private static final int MAX_FUEL = 50;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MOVE_POWER = 5;

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new MDTankCannon(), new MDTankMGun() };

  public MDTankModel()
  {
    super("Medium Tank", UnitEnum.MD_TANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, MOVE_POWER, moveType, actions, weapons);
  }
}
