package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class LanderModel extends UnitModel
{
  private static final int UNIT_COST = 12000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 1;
  private static final int MOVE_POWER = 6;

  private static final MoveType moveType = new FloatLight();
  // TODO: Currently, transports can unload units wherever the transport happens to be, so long as there is valid terrain for the units to end up on.
  // As the source material limits copters to land unloading and landers to shoals, it stands to reason we should support that limitation.
  private static final ActionType[] actions = { ActionType.UNLOAD, ActionType.WAIT };

  public LanderModel()
  {
    super("Lander", UnitEnum.LANDER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, MOVE_POWER, moveType, actions, new WeaponModel[0]);
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
