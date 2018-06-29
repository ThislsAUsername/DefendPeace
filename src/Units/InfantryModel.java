package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.Weapons.InfantryMGun;
import Units.Weapons.WeaponModel;

public class InfantryModel extends UnitModel
{
  private static final int UNIT_COST = 1000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MOVE_POWER = 3;

  private static final MoveType moveType = new FootStandard();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.CAPTURE, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new InfantryMGun() };

  public InfantryModel()
  {
    super("Infantry", UnitEnum.INFANTRY, ChassisEnum.TROOP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, MOVE_POWER, moveType, actions, weapons);
  }
}
