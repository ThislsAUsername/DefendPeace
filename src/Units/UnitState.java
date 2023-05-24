package Units;

import java.io.Serializable;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapLocation;

public abstract class UnitState implements Serializable
{
  private static final long serialVersionUID = 1L;

  public final ArrayList<Unit> heldUnits;
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
    health = healthFromHP(UnitModel.MAXIMUM_HP);
    captureProgress = 0;
    captureTarget = null;

    heldUnits = new ArrayList<>(model.baseCargoCapacity);
  }
  public UnitState(UnitState other)
  {
    CO = other.CO;
    model = other.model;
    isTurnOver = other.isTurnOver;
    copyUnitState(other);
    captureProgress = other.captureProgress;
    captureTarget = other.captureTarget;

    heldUnits = new ArrayList<>(model.baseCargoCapacity);
    heldUnits.addAll(other.heldUnits);
  }
  public void copyUnitState(UnitState other)
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

  public boolean hasMaterials()
  {
    return materials > 0 || !model.needsMaterials;
  }

  public boolean isHurt()
  {
    return health < healthFromHP(UnitModel.MAXIMUM_HP);
  }
  public int getHP()
  {
    return (int) Math.ceil(healthToHP(health));
  }
  /** @return value in range [0-1.0]; represents the unit's current effectiveness */
  public double getHPFactor()
  {
    return getHP() / (double) UnitModel.MAXIMUM_HP;
  }
  /** @return un-rounded HP */
  public double getPreciseHP()
  {
    return healthToHP(health);
  }
  public static double healthToHP(int input)
  {
    return ((double) input) / 10;
  }
  public static int healthFromHP(double input)
  {
    return (int) (input * 10);
  }

  /**
   * Reduces HP by the specified amount.
   * <p>Enforces a minimum (optional) of 0.
   * <p>Use this for lethal damage, especially unit-on-unit violence. Do not use for healing.
   * @return the change in HP
   */
  public int damageHP(double damage)
  {
    return damageHP(damage, false);
  }
  public int damageHP(double damage, boolean allowOverkill)
  {
    if( damage < 0 )
      throw new ArithmeticException("Cannot inflict negative damage!");
    int before = getHP();
    health = health - healthFromHP(damage);
    if( !allowOverkill )
      health = Math.max(0, health);
    return getHP() - before;
  }

  /**
   * Increases HP by the specified amount.
   * <p>Enforces a minimum of 0.1, and a maximum (optional) of MAXIMUM_HP.
   * <p>When healing, rounds health up to a whole HP (e.g. 2.5 + 2 = 4.5 -> 5.0)
   * <p>Use this for most non-combat HP changes (mass damage/silos/healing).
   * @return the change in HP
   */
  public int alterHP(int change)
  {
    return alterHP(change, false);
  }
  public int alterHP(int change, boolean allowOver)
  {
    final int oldHP = getHP();
    int realChange = change;

    // Only enforce the maximum HP if we're healing
    if( !allowOver && change > 0 )
    {
      // If we already have overhealing, treat current HP as the max to avoid e.g. heals reducing HP
      final int capHP = Math.max(oldHP, UnitModel.MAXIMUM_HP);
      // Apply the cap as needed
      final int newHP = Math.min(capHP, oldHP + change);
      // Figure out whether that reduces our healing
      realChange = Math.min(change, newHP - oldHP);
    }

    health = Math.max(1, health + healthFromHP(realChange));
    // Round HP up, if healing
    if( change >= 0 )
      health = healthFromHP(getHP());

    return getHP() - oldHP;
  }

  /**
   * Increases *fractional health* by the specified amount.
   * <p>Enforces a minimum of 0.1, and a maximum (optional) of MAXIMUM_HP.
   * <p>Does not round.
   * <p>Use this when you want precise non-combat health changes, or want to heal without rounding up.
   * @return the change in HP
   */
  public int alterHealthPercent(int percentChange)
  {
    return alterHealthPercent(percentChange, false);
  }
  public int alterHealthPercent(int percentChange, boolean allowOver)
  {
    final int oldHP = getHP();
    final int changeHP = (int) Math.ceil(healthToHP(percentChange));;
    int realPercentChange = percentChange;

    // Only enforce the maximum HP if we're healing
    if( !allowOver && percentChange > 0 )
    {
      // If we already have overhealing, treat current HP as the max to avoid e.g. heals reducing HP
      final int capHP = Math.max(oldHP, UnitModel.MAXIMUM_HP);
      // Apply the cap as needed
      final int newHP = Math.min(capHP, oldHP + changeHP);
      // Figure out whether that reduces our healing
      realPercentChange = Math.min(realPercentChange, healthFromHP(newHP) - health);
    }

    health = Math.max(1, health + realPercentChange);

    return getHP() - oldHP;
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
