package Units;

import Units.MoveTypes.FootStandard;
import Engine.DamageChart.UnitEnum;;

public class Infantry extends UnitModel {
	public Infantry() {
		super("Infantry", UnitEnum.INFANTRY, 99, 0, 3, null);
	}
	public Infantry(FootStandard move) {
		super("Infantry", UnitEnum.INFANTRY, 99, 0, 3, move);
	}
}
