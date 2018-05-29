package Units.Weapons;

public abstract class WeaponModel
{
  public enum WeaponType
  {
    INFANTRYMGUN, MECHZOOKA, MECHMGUN, RECONMGUN, TANKCANNON, TANKMGUN, MD_TANKCANNON, MD_TANKMGUN, NEOCANNON, NEOMGUN, ARTILLERYCANNON, ROCKETS, ANTI_AIRMGUN, MOBILESAM, FIGHTERMISSILES, BOMBERBOMBS, B_COPTERROCKETS, B_COPTERMGUN, BATTLESHIPCANNON, CRUISERTORPEDOES, CRUISERMGUN, SUBTORPEDOES
  };

  public WeaponType type;
  public boolean canFireAfterMoving;
  public boolean hasInfiniteAmmo;
  public int maxAmmo;
  public int minRange;
  public int maxRange;

  protected WeaponModel(WeaponType type, int ammo, int minRange, int maxRange)
  {
    this.type = type;
    hasInfiniteAmmo = (ammo < 0) ? true : false;
    maxAmmo = hasInfiniteAmmo ? Integer.MAX_VALUE : ammo;
    if( minRange > 1 )
    {
      canFireAfterMoving = false;
    }
    else
    {
      canFireAfterMoving = true;
    }
    this.minRange = minRange;
    this.maxRange = maxRange;
  }
  protected WeaponModel(WeaponType type, int ammo)
  {
    this(type, ammo, 1, 1);
  }
  protected WeaponModel(WeaponType type)
  {
    this(type, -1, 1, 1);
  }

  public int getIndex()
  {
    return type.ordinal();
  }
}
