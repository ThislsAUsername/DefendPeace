package Units;

import java.io.Serializable;

import Units.AWBWUnits.AWBWUnitModel;
import Units.DoRUnits.DoRUnitModel;

public abstract class WeaponModel implements Serializable
{
  private static final long serialVersionUID = 1L;

  public boolean canFireAfterMoving;
  public boolean hasInfiniteAmmo;
  public int minRange;
  public int maxRange;

  protected WeaponModel(boolean infiniteAmmo, int minRange, int maxRange)
  {
    hasInfiniteAmmo = infiniteAmmo;
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
  protected WeaponModel(boolean infiniteAmmo)
  {
    this(infiniteAmmo, 1, 1);
  }
  protected WeaponModel()
  {
    this(true, 1, 1);
  }
  public abstract WeaponModel clone();

  public boolean loaded(Unit user)
  {
    if( hasInfiniteAmmo )
      return true;
    else
      return user.ammo > 0;
  }

  /** Expend ammo, if the weapon uses ammo */
  public void fire(Unit user)
  {
    if( !hasInfiniteAmmo )
    {
      if( user.ammo > 0 )
        user.ammo--;
      else if( user.ammo == 0 )
        System.out.println("WARNING: fired an empty gun!");
    }
  }

  /**
   * @return returns its base damage against defender if the unit is in range
   */
  public double getDamage( UnitModel defender, int range )
  {
    if( defender != null )
    {
      if( (range >= minRange) && (range <= maxRange) )
        return getDamage(defender);
    }
    return 0;
  }
  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(UnitModel defender)
  {
    if( defender == null )
      return 0;
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
