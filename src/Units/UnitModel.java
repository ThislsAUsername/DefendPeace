package Units;

import Engine.DamageChart.UnitEnum;
import Units.MoveTypes.MoveType;

public class UnitModel {
	public String name;
	public UnitEnum type;
	public double maxHP;
	public double COStr;
	public double CODef;

	public int maxFuel;
	public int idleFuelBurn;
	public int movePower;
	public MoveType propulsion;
	
	public UnitModel(String pName, UnitEnum pType, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion) {
		name 		 = pName;
		type 		 = pType;
		maxHP        = 100;
		COStr        = 100;
		CODef        = 100;
		maxFuel		 = pFuelMax;
		idleFuelBurn = pIdleFuelBurn;
		movePower    = pMovePower;
		propulsion   = pPropulsion;
	}
}
