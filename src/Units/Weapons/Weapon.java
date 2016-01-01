package Units.Weapons;

import Units.Unit;
import Units.UnitModel;

public class Weapon {
	// format is [attacker][defender]
	private static double[][] damageChart = {{55, 45,  7},
									   		 {65, 55, 10},
									   		 {00, 00, 55}};
	public WeaponModel model;
	public int ammo;
	
	public Weapon(WeaponModel model) {
		this.model = model;
		ammo = model.maxAmmo;
	}

	/**
	 * @return returns its base damage against that unit type
	 */
	public double getDamage(UnitModel.UnitEnum defender) {
		if (ammo == 0 || defender == null)
			return 0;
		return damageChart[model.getIndex()][defender.ordinal()] * (model.dmgPercent/100d);
	}
	/**
	 * @return returns its base damage against defender if the unit is in range,
	 */
	public double getDamage(int x, int y, Unit defender) {
		if (defender != null)
		{
			int dist = Math.abs(defender.y-y) + Math.abs(defender.x-x);
			if ((dist >= model.minRange) && (dist <= model.maxRange))
				return getDamage(defender.model.type);
		}
		return 0;
	}
	
	public void fire()
	{
	if (ammo > 0)
		ammo--;
	else
		System.out.println("Warning, trying to fire an empty gun!");
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
