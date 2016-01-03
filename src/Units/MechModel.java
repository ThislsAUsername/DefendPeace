package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Units.Weapons.MechMGun;
import Units.Weapons.MechRocket;
import Units.Weapons.WeaponModel;

public class MechModel extends UnitModel {

	private static final MoveType moveType = new FootMech();
	private static final ActionType[] actions = {ActionType.ATTACK, ActionType.CAPTURE, ActionType.WAIT};
	private static final WeaponModel[] weapons = {new MechRocket(), new MechMGun()};

	public MechModel() {
		super("Mech", Units.UnitModel.UnitEnum.MECH, 420, 99, 0, 2, moveType, actions, weapons);
		possibleActions = actions;
	}
}
