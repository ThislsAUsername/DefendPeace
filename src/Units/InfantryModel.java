package Units;

import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Engine.MapController.GameAction;

public class InfantryModel extends UnitModel {

	private static final MoveType moveType = new FootStandard();
	private static final GameAction[] actions = {GameAction.ATTACK, GameAction.CAPTURE, GameAction.WAIT};

	public InfantryModel() {
		super("Infantry", Units.UnitModel.UnitEnum.INFANTRY, 300, 99, 0, 3, moveType, actions);
	}
}
