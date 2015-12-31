package Units;

import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Engine.MapController;

public class InfantryModel extends UnitModel {

	private static final MoveType moveType = new FootStandard();

	public InfantryModel() {
		super("Infantry", Units.UnitModel.UnitEnum.INFANTRY, 300, 99, 0, 3, moveType);
		MapController.GameAction[] actions = {MapController.GameAction.ATTACK, MapController.GameAction.CAPTURE, MapController.GameAction.WAIT};
		possibleActions = actions;
	}
}
