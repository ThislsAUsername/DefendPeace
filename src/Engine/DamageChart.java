package Engine;

import Units.Unit;

public class DamageChart {

	// format is [defender][attacker]
	// non-AW units in this version... UnitEnum{INFANTRY, SNIPER, MORTAR, MECH, MECHZOOKA, COMMANDO, COMMANDOMISSILE, APC};
//	public static double[][] chart = {{55, 50, 60, 65, 00, 75, 0, 0},
//									  {50, 45, 50, 60, 00, 70, 0, 0},
//									  {60, 50, 70, 75, 00, 80, 0, 0},
//									  {45, 50, 55, 55, 00, 60, 0, 0},
//									  {45, 50, 55, 55, 00, 60, 0, 0},
//									  {42, 55, 42, 42, 00, 55, 0, 0},
//									  {42, 55, 42, 42, 00, 55, 0, 0},
//									  { 7, 13, 42, 10, 55, 13, 0, 0}};
	// format is [defender][attacker]
	public static double[][] chart = {{55, 65, 00, 0},
									  {45, 55, 00, 0},
									  {45, 55, 00, 0},
									  { 7, 10, 55, 0}};
	public DamageChart() {
		// chart = new double[Units.values().length][Units.values().length];
	}
	
	public static double chartDamage(Unit attacker, Unit defender) {
		return chart[defender.model.type.ordinal()][attacker.getWeapon(defender.model.type).ordinal()];
	}
}
