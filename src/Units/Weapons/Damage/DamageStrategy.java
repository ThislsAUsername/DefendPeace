package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public interface DamageStrategy
{
  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(WeaponModel attack, UnitModel defender);
  
  public String getDescription();
}
