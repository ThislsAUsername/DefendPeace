package Units.Weapons;

import Units.Unit;
import Units.UnitModel;

public abstract class Weapon {
	// format is [attacker][defender]
	private static double[][] chart = {{55, 45, 45,  7},
									  {65, 55, 55, 10},
									  {00, 00, 00, 55},
									  { 0,  0,  0,  0}};
	public WeaponModel model;
	public int ammo;
	
	public Weapon(WeaponModel model) {
		this.model = model;
		ammo = model.maxAmmo;
	}

	public double getDamage(UnitModel.UnitEnum defender) {
		if (ammo == 0 || defender == null)
			return 0;
		return chart[model.type][defender.ordinal()];
	}
//	public double getDamage(Unit defender) {
//		return getDamage(defender.model.type);
//	}
	public double getDamage(int x, int y, Unit defender) {
		int dist = Math.abs(defender.y-y) + Math.abs(defender.x-x);
		if ((dist >= model.minRange) && (dist <= model.maxRange))
			return getDamage(defender.model.type);
		return 0;
	}
	
	public void fire()
	{
	if (ammo > 0)
		ammo--;
	}
	
	/**
	 * @return the amount of ammo reloaded
	 */
	public int reload()
	{
		int difference = model.maxAmmo-ammo;
		ammo = model.maxAmmo;
		return difference;
	}
}
