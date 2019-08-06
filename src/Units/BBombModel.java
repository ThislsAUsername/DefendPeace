package Units;

import Engine.UnitActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class BBombModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 25000;
  private static final int MAX_FUEL = 45;
  private static final int IDLE_FUEL_BURN = 5;
  private static final int VISION_RANGE = 1;
  private static final int MOVE_POWER = 9;

  private static final MoveType moveType = new Flight();
  private static final UnitActionType[] actions = UnitActionType.BASIC_ACTIONS;
  private static final WeaponModel[] weapons = { };

  public BBombModel()
  {
    super("BBomb", UnitEnum.BBOMB, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
    possibleActions.add(new UnitActionType.Explode(5, 3));
  }
}
