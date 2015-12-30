package Units;

import Engine.MapController;
import Engine.DamageChart.UnitEnum;
import Units.MoveTypes.MoveType;

public class UnitModel {
	public String name;
	public UnitEnum type;
	public double maxHP;
	public double COStr;
	public double CODef;
	public MapController.GameAction[] possibleActions;

	public int moneyCost = 9001;
	public int minRange = 1, maxRange = 1;
	public int maxFuel;
	public int idleFuelBurn;
	public int movePower;
	public MoveType propulsion;
	
	public UnitModel(String pName, UnitEnum pType, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion) {
		name 		 = pName;
		type 		 = pType;
		moneyCost	 = cost;
		maxHP        = 100;
		COStr        = 100;
		CODef        = 100;
		maxFuel		 = pFuelMax;
		idleFuelBurn = pIdleFuelBurn;
		movePower    = pMovePower;
		propulsion   = pPropulsion;
	}
}
