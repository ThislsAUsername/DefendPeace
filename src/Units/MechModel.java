package Units;

import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Engine.DamageChart.UnitEnum;;

public class MechModel extends UnitModel {

	private static final MoveType moveType = new FootMech();

	public MechModel() {
		super("Mech", UnitEnum.MECH, 420, 99, 0, 2, moveType);
	}
}
