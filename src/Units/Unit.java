package Units;

import CommandingOfficers.Commander;
import Engine.DamageChart.UnitEnum;

public class Unit {
	public UnitModel model;
	public int x, y, fuel;
	public Commander CO;
	public boolean isTurnOver;
	public double HP;
	
	// allows the unit to choose its weapon
	public UnitEnum getWeapon(UnitEnum target) {
		return model.type;
	}
	// for the purpose of letting the unit know it has attacked.
	public void fire(Unit target) {}

	public double getAtkStr(Unit target, boolean isCounter) {
//		double output = model
//		return [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100] ;
		return 0;
	}
	public double getDefenseStr(Unit unit, boolean isCounter) {
		return 0;
	}
}
