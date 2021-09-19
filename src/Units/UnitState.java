package Units;

import java.io.Serializable;
import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapLocation;
import Units.Unit.CargoList;

public abstract class UnitState implements Serializable
{
  private static final long serialVersionUID = 1L;

  public final CargoList heldUnits;
  public int ammo;
  public int fuel;
  public int materials;
  public boolean isTurnOver;
  public boolean isStunned;

  public UnitModel model;
  public Commander CO;

  /**
   * HP determines the current actual durability of a unit.
   * It's typically in range [1-10]. A unit at 0 HP is dead.
   * Health is HP value as a percentage, thus ~10x the HP value.
   * When determining HP, health must always be rounded up.
   */
  protected int health;

  protected int captureProgress;
  protected MapLocation captureTarget;


  public UnitState(Commander co, UnitModel um)
  {
    CO = co;
    model = um;
    ammo = model.maxAmmo;
    fuel = model.maxFuel;
    materials = model.maxMaterials;
    isTurnOver = true;
    health = healthFromHP(model.maxHP);
    captureProgress = 0;
    captureTarget = null;

    heldUnits = new CargoList(model);
  }
  public UnitState(UnitState other)
  {
    CO = other.CO;
    model = other.model;
    isTurnOver = other.isTurnOver;
    setResourceState(other);
    captureProgress = other.captureProgress;
    captureTarget = other.captureTarget;

    heldUnits = new CargoList(model);
    heldUnits.addAll(other.heldUnits);
  }
  public void setResourceState(UnitState other)
  {
    ammo = other.ammo;
    fuel = other.fuel;
    materials = other.materials;
    health = other.health;
  }


  /** Expend ammo, if the weapon uses ammo */
  public void fire(WeaponModel weapon)
  {
    if( !weapon.hasInfiniteAmmo )
    {
      if( ammo > 0 )
        ammo--;
      else
        System.out.println("WARNING: " + toString() + " fired with no available ammo!");
    }
  }

  public boolean isHurt()
  {
    return health < healthFromHP(model.maxHP);
  }
  public int getHP()
  {
    return (int) Math.ceil(healthToHP(health));
  }
  /** @return value in range [0-1.0]; represents the unit's current effectiveness */
  public double getHPFactor()
  {
    return getHP() / (double) model.maxHP;
  }
  /** @return un-rounded HP */
  public double getPreciseHP()
  {
    return healthToHP(health);
  }
  protected static double healthToHP(int input)
  {
    return ((double) input) / 10;
  }
  protected static int healthFromHP(double input)
  {
    return (int) (input * 10);
  }

  /**
   * Reduces HP by the specified amount.
   * Enforces a minimum of 0.
   * @return the change in HP
   */
  public int damageHP(double damage)
  {
    return damageHP(damage, false);
  }
  public int damageHP(double damage, boolean allowNegative)
  {
    if( damage < 0 )
      throw new ArithmeticException("Cannot inflict negative damage!");
    int before = getHP();
    health = health - healthFromHP(damage);
    if( !allowNegative )
      health = Math.max(0, health);
    return getHP() - before;
  }

  /**
   * Increases HP by the specified amount.
   * Enforces a minimum of 0.1.
   * When healing, sets health to the maximum value for its HP
   * @return the change in HP
   */
  public int alterHP(int change)
  {
    return alterHP(change, change < 0);
  }
  public int alterHP(int change, boolean allowOver)
  {
    int before = getHP();
    int newHP = allowOver ? getHP() + change : Math.min(model.maxHP, getHP() + change);
    health = Math.max(1, healthFromHP(newHP));
    if( change > 0 )
      health = healthFromHP(getHP());
    return getHP() - before;
  }


  public boolean capture(MapLocation target)
  {
    boolean success = false;

    if( target != captureTarget )
    {
      captureTarget = target;
      captureProgress = 0;
    }
    captureProgress += getHP();
    if( captureProgress >= target.getEnvironment().terrainType.getCaptureThreshold() )
    {
      target.setOwner(CO);
      captureProgress = 0;
      target = null;
      success = true;
    }

    return success;
  }

  public void stopCapturing()
  {
    captureTarget = null;
    captureProgress = 0;
  }

  public int getCaptureProgress()
  {
    return captureProgress;
  }
  public XYCoord getCaptureTargetCoords()
  {
    XYCoord target = null;
    if( null != captureTarget )
    {
      target = captureTarget.getCoordinates();
    }
    return target;
  }
}
