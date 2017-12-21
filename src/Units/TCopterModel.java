package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;

public class TCopterModel extends UnitModel
{

  private static final MoveType moveType = new Flight();
  private static final ActionType[] actions = { ActionType.UNLOAD, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.AIRPORT };

  public TCopterModel()
  {
    super("T Copter", UnitEnum.T_COPTER, 5000, 99, 2, 6, moveType, actions, healHabs, null);
    holdingCapacity = 1;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }
}
