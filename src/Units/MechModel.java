package Units;

import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Units.Weapons.MechMGun;
import Units.Weapons.MechZooka;
import Units.Weapons.WeaponModel;
import Engine.MapController.GameAction;;

public class MechModel extends UnitModel {

	private static final MoveType moveType = new FootMech();
	private static final GameAction[] actions = {GameAction.ATTACK, GameAction.CAPTURE, GameAction.WAIT};
	private static final WeaponModel[] weapons = {new MechZooka(), new MechMGun()};

	public MechModel() {
		super("Mech", Units.UnitModel.UnitEnum.MECH, 420, 99, 0, 2, moveType, actions, weapons);
		possibleActions = actions;
	}
}
