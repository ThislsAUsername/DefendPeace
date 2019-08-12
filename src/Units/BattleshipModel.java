package Units;

import Engine.UnitActionType;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.BattleshipCannon;
import Units.Weapons.WeaponModel;

public class BattleshipModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 28000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int VISION_RANGE = 2;
  private static final int MOVE_POWER = 5;

  private static final MoveType moveType = new FloatHeavy();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new BattleshipCannon() };

  public BattleshipModel()
  {
    super("Battleship", UnitEnum.BATTLESHIP, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
