package Units;

import java.io.Serializable;

import Terrain.TerrainType;
import Units.AWBWUnits.AWBWUnitModel;
import Units.DoRUnits.DoRUnitModel;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;

public abstract class WeaponModel implements Serializable
{
  private static final long serialVersionUID = 1L;

  public boolean canFireAfterMoving;
  public boolean hasInfiniteAmmo;
  public int rangeMin;
  public int rangeMax;

  protected WeaponModel(boolean infiniteAmmo, int minRange, int maxRange)
  {
    hasInfiniteAmmo = infiniteAmmo;
    if( maxRange > 1 )
    {
      canFireAfterMoving = false;
    }
    else
    {
      canFireAfterMoving = true;
    }
    this.rangeMin = minRange;
    this.rangeMax = maxRange;
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

  public boolean loaded(UnitState user)
  {
    if( hasInfiniteAmmo )
      return true;
    else
      return user.ammo > 0;
  }

  /**
   * @return returns its base damage against that target type
   */
  public double getDamage(ITargetable defender)
  {
    if( defender == null )
      throw new IllegalArgumentException("Stare not into the void. It just may stare back.");
    return defender.getDamageRedirect(this);
  }
  public double getDamage(KaijuWarsUnitModel defender)
  {
    throw new UnsupportedOperationException("Called base WeaponModel.getDamage() with input type " + defender.getClass());
  }
  public double getDamage(AWBWUnitModel defender)
  {
    throw new UnsupportedOperationException("Called base WeaponModel.getDamage() with input type " + defender.getClass());
  }
  public double getDamage(DoRUnitModel defender)
  {
    throw new UnsupportedOperationException("Called base WeaponModel.getDamage() with input type " + defender.getClass());
  }
  public abstract double getDamage(TerrainType target);
}
