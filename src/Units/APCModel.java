package Units;

import java.util.Vector;

import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Engine.DamageChart;
import Engine.MapController;
import Engine.DamageChart.UnitEnum;;

public class APCModel extends UnitModel {

	private static final MoveType moveType = new Tread();

	public APCModel() {
		super("APC", UnitEnum.APC, 550, 42, 0, 5, moveType);
		MapController.GameAction[] actions = {MapController.GameAction.LOAD, MapController.GameAction.UNLOAD, MapController.GameAction.WAIT};
		possibleActions = actions;
		holdingCapacity = 1;
		UnitEnum[] carryable = {DamageChart.UnitEnum.INFANTRY, DamageChart.UnitEnum.MECH};
		holdables = new Vector<UnitEnum>(carryable.length);
		for (int i = 0; i < holdables.capacity(); i++) {
			holdables.add(carryable[i]);
		}
	}
}
