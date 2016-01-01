package Units.Weapons;

public abstract class WeaponModel {
	protected enum WeaponType{INFANTRYMGUN, MECHMGUN, MECHROCKET};
	public WeaponType type;
	public int maxAmmo;
	public int minRange;
	public int maxRange;
	public double dmgPercent;
	
	protected WeaponModel(WeaponType type, int ammo, int minRange, int maxRange) {
		this.type = type;
		maxAmmo = ammo;
		this.minRange = minRange;
		this.maxRange = maxRange;
		dmgPercent = 100;
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

	/**
	 * Takes a percent change and adds it to the current damage ratio for this WeaponModel.
	 * @param change The percent damage to add; e.g. if dmgRatio is 100 and this function is
	 * called with 10, the new one will be 110. If it is called with 10 again, it will go to 120.
	 */
	public void modifyDamageRatio(int change)
	{
		dmgPercent += change;
	}
}
