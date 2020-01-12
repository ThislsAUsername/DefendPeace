package Units;

import java.io.Serializable;

import Units.AWBWUnits.AWBWUnitModel;
import Units.DoRUnits.DoRUnitModel;

public abstract class WeaponModel implements Serializable
{
  private static final long serialVersionUID = 1L;

  public boolean canFireAfterMoving;
  public boolean hasInfiniteAmmo;
  public int maxAmmo;
  public int minRange;
  public int maxRange;

  protected WeaponModel(int ammo, int minRange, int maxRange)
  {
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
  protected WeaponModel(int ammo)
  {
    this(ammo, 1, 1);
  }
  protected WeaponModel()
  {
    this(-1, 1, 1);
  }
  public abstract WeaponModel clone();

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(UnitModel defender)
  {
    return defender.getDamageRedirect(this);
  }
  public double getDamage(AWBWUnitModel defender)
  {
    throw new UnsupportedOperationException("Called base WeaponModel.getDamage() with input type " + defender.getClass());
  }
  public double getDamage(DoRUnitModel defender)
  {
    throw new UnsupportedOperationException("Called base WeaponModel.getDamage() with input type " + defender.getClass());
  }
}
