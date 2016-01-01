package Units;

import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.Weapons.InfantryMGun;
import Units.Weapons.MechMGun;
import Units.Weapons.MechRocket;
import Units.Weapons.WeaponModel;
import Engine.MapController.GameAction;

public class InfantryModel extends UnitModel {

	private static final MoveType moveType = new FootStandard();
	private static final GameAction[] actions = {GameAction.ATTACK, GameAction.CAPTURE, GameAction.WAIT};
	private static final WeaponModel[] weapons = {new InfantryMGun()};
	
	public InfantryModel() {
		super("Infantry", Units.UnitModel.UnitEnum.INFANTRY, 300, 99, 0, 3, moveType, actions, weapons);
	}
}
