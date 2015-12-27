package Units;

import Units.MoveTypes.FootStandard;
import Engine.DamageChart.UnitEnum;;

public class InfantryModel extends UnitModel {
	public InfantryModel() {
		super("Infantry", UnitEnum.INFANTRY, 99, 0, 3, null);
	}
	public InfantryModel(FootStandard move) {
		super("Infantry", UnitEnum.INFANTRY, 99, 0, 3, move);
	}
}
