package Units;

import CommandingOfficers.Commander;

public class Unit {
	public UnitModel model;
	public int x, y, fuel;
	public Commander CO;
	public boolean isTurnOver;
	public double HP;

	public double getAtkStr(Unit target, boolean isCounter) {
//		double output = model
//		return [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100] ;
		return 0;
	}
	public double getDefenseStr(Unit unit, boolean isCounter) {
		return 0;
	}
}
