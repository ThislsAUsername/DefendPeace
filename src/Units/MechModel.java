package Units;

import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Engine.MapController.GameAction;;

public class MechModel extends UnitModel {

	private static final MoveType moveType = new FootMech();
	private static final GameAction[] actions = {GameAction.ATTACK, GameAction.CAPTURE, GameAction.WAIT};

	public MechModel() {
		super("Mech", Units.UnitModel.UnitEnum.MECH, 420, 99, 0, 2, moveType, actions);
		possibleActions = actions;
//		weapons[0] = UnitEnum.MECHZOOKA;
//		maxAmmos[0] = 2;
//		weapons[1] = UnitEnum.MECH;
//		maxAmmos[1] = -1;
	}
}
