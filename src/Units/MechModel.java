package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Units.Weapons.MechMGun;
import Units.Weapons.MechZooka;
import Units.Weapons.WeaponModel;

public class MechModel extends UnitModel {

	private static final MoveType moveType = new FootMech();
	private static final ActionType[] actions = {ActionType.ATTACK, ActionType.CAPTURE, ActionType.WAIT};
	private static final Terrains[] healHabs = {Terrains.CITY, Terrains.FACTORY, Terrains.HQ};
	private static final WeaponModel[] weapons = {new MechZooka(), new MechMGun()};

	public MechModel() {
		super("Mech", Units.UnitModel.UnitEnum.MECH, 2500, 99, 0, 2, moveType, actions, healHabs, weapons);
	}
}
