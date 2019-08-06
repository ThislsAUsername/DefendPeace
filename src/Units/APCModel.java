package Units;

import java.util.ArrayList;
import java.util.Vector;

import Engine.GameAction;
import Engine.UnitActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.WeaponModel;

public class APCModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 5000;
  private static final int MAX_FUEL = 70;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_RANGE = 1;
  private static final int MOVE_POWER = 6;
  private static final MoveType moveType = new Tread();
  private static final UnitActionType[] actions = UnitActionType.APC_ACTIONS;

  public APCModel()
  {
    super("APC", UnitEnum.APC, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, new WeaponModel[0]);
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
