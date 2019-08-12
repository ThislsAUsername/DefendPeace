package Units;

import java.util.Vector;

import Engine.UnitActionType;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class BBoatModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 7500;
  private static final int MAX_FUEL = 60;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int VISION_RANGE = 1;
  private static final int MOVE_POWER = 7;

  private static final MoveType moveType = new FloatLight();
  private static final UnitActionType[] actions = UnitActionType.TRANSPORT_ACTIONS;

  public BBoatModel()
  {
    super("BBoat", UnitEnum.BBOAT, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, new WeaponModel[0]);
    holdingCapacity = 2;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
    possibleActions.add(UnitActionType.REPAIR_UNIT);
  }
}
