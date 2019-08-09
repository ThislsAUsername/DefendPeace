package Units;

import Engine.UnitActionType;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.CruiserMGun;
import Units.Weapons.CruiserTorpedoes;
import Units.Weapons.WeaponModel;

public class CruiserModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 18000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int VISION_RANGE = 3;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new FloatHeavy();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS; // TODO: carry copters?
  private static final WeaponModel[] weapons = { new CruiserTorpedoes(), new CruiserMGun() };

  public CruiserModel()
  {
    super("Cruiser", UnitEnum.CRUISER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
  }
}
