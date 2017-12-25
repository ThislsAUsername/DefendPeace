package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public abstract class DamageStrategy
{
  /**
   * @return returns its base damage against that unit type
   */
  public abstract double getDamage(WeaponModel attack, UnitModel.UnitEnum defender);
}
