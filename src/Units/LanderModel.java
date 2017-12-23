package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.MoveType;

public class LanderModel extends UnitModel
{

  private static final MoveType moveType = new FloatLight();
  // TODO: Currently, transports can unload units wherever the transport happens to be, so long as there is valid terrain for the units to end up on.
  // As the source material limits copters to land unloading and landers to shoals, it stands to reason we should support that limitation.
  private static final ActionType[] actions = { ActionType.UNLOAD, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.SEAPORT };

  public LanderModel()
  {
    super("Lander", UnitEnum.LANDER, 12000, 99, 1, 6, moveType, actions, healHabs, null);
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
