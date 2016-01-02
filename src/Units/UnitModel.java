package Units;

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
	private int COstr;
	private int COdef;
	
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
		COstr        = 100;
		COdef        = 100;
		holdingCapacity = 0;
	}

	/**
	 * Takes a percent change and adds it to the current damage multiplier for this UnitModel.
	 * @param change The percent damage to add; e.g. if the multiplier is 100 and this function is
	 * called with 10, the new one will be 110. If it is called with 10 again, it will go to 120.
	 */
	public void modifyDamageRatio(int change)
	{
		COstr += change;
	}
	public int getDamageRatio()
	{
		return COstr;
	}
	
	/**
	 * Takes a percent change and adds it to the current defense modifier for this UnitModel.
	 * @param change The percent defense to add; e.g. if the defense modifier is 100 and this function
	 * is called with 10, the new one will be 110. If it is called with 10 again, it will go to 120.
	 */
	public void modifyDefenseRatio(int change)
	{
		COdef += change;
	}
	public int getDefenseRatio()
	{
		return COdef;
	}
}
