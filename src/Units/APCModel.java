package Units;

import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Engine.MapController;
import Engine.DamageChart.UnitEnum;;

public class APCModel extends UnitModel {

	private static final MoveType moveType = new Tread();

	public APCModel() {
		super("APC", UnitEnum.APC, 1, 550, 42, 0, 5, moveType);
		MapController.GameAction[] actions = {MapController.GameAction.LOAD, MapController.GameAction.UNLOAD, MapController.GameAction.WAIT};
		possibleActions = actions;
	}
}
