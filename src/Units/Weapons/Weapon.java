package Units.Weapons;

import java.io.Serializable;

import Units.UnitModel;
import Units.Weapons.Damage.DSDamage;
import Units.Weapons.Damage.DamageStrategy;
import Units.Weapons.Damage.DoRDamage;
import Units.Weapons.Damage.BHRDamage;

public class Weapon implements Serializable
{

  public WeaponModel model;
  public int ammo;
  public static DamageStrategy[] strategies = {new BHRDamage(), new DSDamage(), new DoRDamage()};
  public static String[] stratDescriptions = {strategies[0].getDescription(), strategies[1].getDescription(), strategies[2].getDescription()};
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
