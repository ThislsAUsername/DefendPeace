package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.NeoCannon;
import Units.Weapons.NeoMGun;
import Units.Weapons.WeaponModel;

public class NeotankModel extends UnitModel
{
  private static final int UNIT_COST = 22000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_RANGE = 1;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new NeoCannon(), new NeoMGun() };

  public NeotankModel()
  {
    super("Neotank", UnitEnum.NEOTANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
