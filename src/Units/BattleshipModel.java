package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.BattleshipCannon;
import Units.Weapons.WeaponModel;

public class BattleshipModel extends UnitModel
{
  private static final int UNIT_COST = 28000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int MOVE_POWER = 5;

  private static final MoveType moveType = new FloatHeavy();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new BattleshipCannon() };

  public BattleshipModel()
  {
    super("Battleship", UnitEnum.BATTLESHIP, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, MOVE_POWER, moveType, actions, weapons);
  }
}
