package Units;

import Units.MoveTypes.MoveType;

public class UnitModel {
	public String name;
	public double maxHP;
	public double COStr;
	public double CODef;

	public int fuelMax;
	public int idleFuelBurn;
	public int movePower;
	public MoveType propulsion;
	
	public UnitModel(String pName, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion) {
		name 		 = pName;
		maxHP        = 100;
		COStr        = 100;
		CODef        = 100;
		fuelMax		 = pFuelMax;
		idleFuelBurn = pIdleFuelBurn;
		movePower    = pMovePower;
		propulsion   = pPropulsion;
	}
}
