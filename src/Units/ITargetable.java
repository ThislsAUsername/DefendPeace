package Units;

public interface ITargetable
{
  /** Calls the appropriate type-specific override of getDamage() on the input WeaponModel */
  public abstract int getDamageRedirect(WeaponModel wm);
}
