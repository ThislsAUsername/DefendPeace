package Units.Weapons;

import java.io.Serializable;
import Units.UnitModel;

public class Weapon implements Serializable
{
  private static final long serialVersionUID = 1L;
  public WeaponModel model;
  public int ammo;

  public Weapon(WeaponModel model)
  {
    this.model = model;
    ammo = model.maxAmmo;
  }

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(UnitModel defender)
  {
    if( ammo == 0 || defender == null )
      return 0;
    return WeaponModel.getDamage(model, defender);
  }

  /**
   * @return returns its base damage against defender if the unit is in range,
   */
  public double getDamage( UnitModel defender, int range )
  {
    if( defender != null )
    {
      if( (ammo > 0) && (range >= model.minRange) && (range <= model.maxRange) )
        return getDamage(defender);
    }
    return 0;
  }

  public void fire()
  {
    if( !model.hasInfiniteAmmo )
    {
      if( ammo > 0 )
        ammo--;
      else if( ammo == 0 )
        System.out.println("WARNING: trying to fire an empty gun!");
    }
  }

  /**
   * @return the amount of ammo reloaded
   */
  public int reload()
  {
    int difference = model.maxAmmo - ammo;
    ammo = model.maxAmmo;
    return difference;
  }
}
