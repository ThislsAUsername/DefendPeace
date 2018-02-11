package Units;

import java.util.ArrayList;
import java.util.Vector;

import Engine.GameAction;
import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;

public class APCModel extends UnitModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.RESUPPLY, ActionType.UNLOAD, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };

  public APCModel()
  {
    super("APC", UnitEnum.APC, ChassisEnum.TANK, 5000, 70, 0, 6, moveType, actions, healHabs, null);
    holdingCapacity = 1;
    UnitEnum[] carryable = { Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH };
    holdables = new Vector<UnitEnum>(carryable.length);
    for( int i = 0; i < holdables.capacity(); i++ )
    {
      holdables.add(carryable[i]);
    }
  }

  /**
   * APCs re-supply any adjacent allies at the beginning of every turn. Make it so.
   */
  @Override
  public ArrayList<GameAction> getTurnInitActions(Unit self)
  {
    ArrayList<GameAction> actions = new ArrayList<GameAction>(1);
    actions.add(new GameAction.ResupplyAction(self));
    return actions;
  }
}
