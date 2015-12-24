package Units;

import Units.MoveTypes.FootStandard;

public class Infantry extends UnitModel {
	public Infantry() {
		super("Infantry", 99, 0, 3, null);
	}
	public Infantry(FootStandard move) {
		super("Infantry", 99, 0, 3, move);
	}
}
