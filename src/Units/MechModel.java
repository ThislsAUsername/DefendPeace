package Units;

import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Engine.MapController;
import Engine.DamageChart.UnitEnum;;

public class MechModel extends UnitModel {

	private static final MoveType moveType = new FootMech();

	public MechModel() {
		super("Mech", UnitEnum.MECH, 420, 99, 0, 2, moveType);
		MapController.GameAction[] actions = {MapController.GameAction.ATTACK, MapController.GameAction.CAPTURE, MapController.GameAction.WAIT};
		possibleActions = actions;
	}
}
