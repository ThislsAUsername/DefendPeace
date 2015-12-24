package Engine;

public class DamageChart {
	public enum UnitEnum{INFANTRY, SNIPER, MORTAR, MECH, MECHZOOKA, COMMANDO, COMMANDOMISSILE};
	
	// format is [attacker][defender]
	public static double[][] chart = {{55, 55, 60, 50, 50, 42, 42},
									  {55, 60, 55, 60, 60, 55, 55},
									  {60, 50, 70, 55, 55, 42, 42},
									  {60, 55, 75, 55, 55, 42, 42},
									  {00, 00, 00, 00, 00, 00, 00},
									  {65, 60, 80, 60, 60, 55, 55},
									  {00, 00, 00, 00, 00, 00, 00}};
	public DamageChart() {
		// chart = new double[Units.values().length][Units.values().length];
	}
	
//	public double 
}
