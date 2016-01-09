package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;

public class APCModel extends UnitModel {

	private static final MoveType moveType = new Tread();
	private static final ActionType[] actions = {ActionType.UNLOAD, ActionType.WAIT};

	public APCModel() {
		super("APC", UnitEnum.APC, 5000, 42, 0, 5, moveType, actions, null);
		possibleActions = actions;
		holdingCapacity = 1;
		UnitEnum[] carryable = {Units.UnitModel.UnitEnum.INFANTRY, Units.UnitModel.UnitEnum.MECH};
		holdables = new Vector<UnitEnum>(carryable.length);
		for (int i = 0; i < holdables.capacity(); i++) {
			holdables.add(carryable[i]);
		}
	}
}
