package Units;

import CommandingOfficers.Commander;

public class Unit {
	public UnitModel model;
	public int x, y, fuel;
	public Commander CO;
	public boolean isTurnOver;
	public double HP;

	public double getAtkStr(Unit target, boolean isCounter){
		return 0;
	}
	public void applyDamage(Unit unit, boolean isCounter){}
}
