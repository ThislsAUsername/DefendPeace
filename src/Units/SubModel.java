package Units;

import Engine.UnitActionType;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.SubTorpedoes;
import Units.Weapons.WeaponModel;

public class SubModel extends UnitModel
{
  private static final int UNIT_COST = 20000;
  private static final int MAX_FUEL = 60;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int VISION_RANGE = 5;
  private static final int MOVE_POWER = 6;

  // TODO: add submerge
  private static final MoveType moveType = new FloatHeavy();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new SubTorpedoes() };

  public SubModel()
  {
    super("Submarine", UnitEnum.SUB, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
