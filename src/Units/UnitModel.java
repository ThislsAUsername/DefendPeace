package Units;

import java.util.ArrayList;
import java.util.Vector;

import Engine.MapController.GameAction;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class UnitModel {
	public enum UnitEnum{INFANTRY, MECH, APC};
	
	public String name;
	public UnitEnum type;
	public int moneyCost = 9001;
	public int maxFuel;
	public int idleFuelBurn;
	public int movePower;
	public MoveType propulsion;
	public GameAction[] possibleActions;
	public WeaponModel[] weaponModels;

	public double maxHP;
	public int holdingCapacity;
	public Vector<UnitEnum> holdables;
	public double COStr;
	public double CODef;
	
	public UnitModel(String pName, UnitEnum pType, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion, GameAction[] actions, WeaponModel[] weapons) {
		name 		 = pName;
		type 		 = pType;
		moneyCost	 = cost;
		maxFuel		 = pFuelMax;
		idleFuelBurn = pIdleFuelBurn;
		movePower    = pMovePower;
		propulsion   = pPropulsion;
		possibleActions = actions;
		weaponModels = weapons;
		
		maxHP        = 100;
		COStr        = 100;
		CODef        = 100;
		holdingCapacity = 0;
	}
}
