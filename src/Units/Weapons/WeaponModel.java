package Units.Weapons;

public abstract class WeaponModel {
	protected enum WeaponType{INFANTRYMGUN, MECHMGUN, MECHROCKET};
	public WeaponType type;
	public int maxAmmo;
	public int minRange;
	public int maxRange;
	
	protected WeaponModel(WeaponType type, int ammo, int minRange, int maxRange) {
		this.type = type;
		maxAmmo = ammo;
		this.minRange = minRange;
		this.maxRange = maxRange;
	}
	protected WeaponModel(WeaponType type, int ammo) {
		this(type, ammo, 1, 1);
	}
	protected WeaponModel(WeaponType type) {
		this(type, -1, 1, 1);
	}
	
	public int getIndex()
	{
		return type.ordinal();
	}
}
