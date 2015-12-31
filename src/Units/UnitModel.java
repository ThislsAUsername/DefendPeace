package Units;

import java.util.Vector;

import Engine.MapController.GameAction;
import Units.MoveTypes.MoveType;

public class UnitModel {
	public enum UnitEnum{INFANTRY, MECH, MECHZOOKA, APC};
	
	public String name;
	public UnitEnum type;
	public int moneyCost = 9001;
	public int maxFuel;
	public int idleFuelBurn;
	public int movePower;
	public MoveType propulsion;
	public GameAction[] possibleActions;
	
	public double maxHP;
	public int holdingCapacity;
	public Vector<UnitEnum> holdables;
	public double COStr;
	public double CODef;

	public int minRange = 1;
	public int maxRange = 1;
	
	public UnitModel(String pName, UnitEnum pType, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion, GameAction[] actions) {
		name 		 = pName;
		type 		 = pType;
		moneyCost	 = cost;
		maxFuel		 = pFuelMax;
		idleFuelBurn = pIdleFuelBurn;
		movePower    = pMovePower;
		propulsion   = pPropulsion;
		possibleActions = actions;
		
		maxHP        = 100;
		COStr        = 100;
		CODef        = 100;
		holdingCapacity = 0;
	}
}
