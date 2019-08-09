package Units;

import Engine.UnitActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.StealthShots;
import Units.Weapons.WeaponModel;

public class StealthModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 24000;
  private static final int MAX_FUEL = 60;
  private static final int IDLE_FUEL_BURN = 5;
  private static final int VISION_RANGE = 4;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new Flight();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new StealthShots() };

  public StealthModel()
  {
    super("Stealth", UnitEnum.STEALTH, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
    addStealthAction();
  }
  
  protected void addStealthAction()
  {
    possibleActions.add(new UnitActionType.Transform(UnitEnum.STEALTH_HIDE, "HIDE"));
  }
}
