package Units.Weapons;

import Units.UnitModel;
import Units.Weapons.Damage.ChassisDamage;
import Units.Weapons.Damage.DSDamage;
import Units.Weapons.Damage.DamageStrategy;
import Units.Weapons.Damage.DoRDamage;
import Units.Weapons.Damage.StandardDamage;

public class Weapon
{

  public WeaponModel model;
  public int ammo;
  public static DamageStrategy[] strategies = {new StandardDamage(), new DSDamage(), new DoRDamage(), new ChassisDamage()};
  public static int currentStrategy = 0;

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
    return strategies[currentStrategy].getDamage(model, defender);
  }

  /**
   * @return returns its base damage against defender if the unit is in range,
   */
  public double getDamage( UnitModel defender, int range )
  {
    if( defender != null )
    {
      if( (range >= model.minRange) && (range <= model.maxRange) )
        return getDamage(defender);
    }
    return 0;
  }

  public void fire()
  {
    if( ammo > 0 )
      ammo--;
    else if( ammo == 0 )
      System.out.println("WARNING: trying to fire an empty gun!");
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
