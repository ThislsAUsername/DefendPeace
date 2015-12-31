package Units.Weapons;

import Units.Unit;
import Units.UnitModel;

public abstract class WeaponModel {
	public int type;
	public int maxAmmo;
	public int minRange;
	public int maxRange;
	
	public WeaponModel(int type, int ammo, int minRange, int maxRange) {
		this.type = type;
		maxAmmo = ammo;
		this.minRange = minRange;
		this.maxRange = maxRange;
	}
	public WeaponModel(int type, int ammo) {
		this(type, ammo, 1, 1);
	}
	public WeaponModel(int type) {
		this(type, -1, 1, 1);
	}
}
