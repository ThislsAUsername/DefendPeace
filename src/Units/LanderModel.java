package Units;

import java.util.Vector;

import Engine.UnitActionType;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class LanderModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 12000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int VISION_RANGE = 1;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new FloatLight();
  private static final UnitActionType[] actions = UnitActionType.TRANSPORT_ACTIONS;

  public LanderModel()
  {
    super("Lander", UnitEnum.LANDER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, new WeaponModel[0]);
    holdingCapacity = 2;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH, Units.UnitModel.UnitEnum.APC,
        Units.UnitModel.UnitEnum.RECON, Units.UnitModel.UnitEnum.ARTILLERY, Units.UnitModel.UnitEnum.ANTI_AIR,
        Units.UnitModel.UnitEnum.TANK, Units.UnitModel.UnitEnum.MD_TANK, Units.UnitModel.UnitEnum.NEOTANK,
        Units.UnitModel.UnitEnum.MOBILESAM, Units.UnitModel.UnitEnum.ROCKETS };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }
}
