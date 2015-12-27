package Units;

import CommandingOfficers.Commander;
import Engine.CombatParameters;
import Engine.DamageChart.UnitEnum;

public class Unit {
	public UnitModel model;
	public int x, y, fuel;
	public Commander CO;
	public boolean isTurnOver;
	public double HP;

	public Unit(Commander co, UnitModel um)
	{
		System.out.println("Creating a " + um.type);
		CO = co;
		model = um;
		fuel = model.fuelMax;
		isTurnOver = false;
		HP = model.maxHP;
	}
	
	// allows the unit to choose its weapon
	public UnitEnum getWeapon(UnitEnum target) {
		return model.type;
	}
	// for the purpose of letting the unit know it has attacked.
	public void fire(final CombatParameters params) {}

	// Removed for the forseeable future; may be back
/*	public static double getAttackPower(final CombatParameters params) {
//		double output = model
//		return [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100] ;
		return 0;
	}
	public static double getDefensePower(Unit unit, boolean isCounter) {
		return 0;
	}*/
}
