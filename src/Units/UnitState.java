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
   * Health determines the current durability of a unit.<p>
   * Health is typically in range [1,100]. A unit at 0 health is dead.<p>
   * Health is used to scale unit effectiveness, and normally rounds up to the next 10 for this purpose.<p>
   * The final digit is truncated for display (to a nominal [1,10] scale), and values in this scale are known as "HP"<p>
   * Because the GUI does not show the final digit, it is considered hidden information.<p>
   * As such, direct access of this value should only be done when you have a specific reason to choose it over getHealth()/getHP()<p>
   */
  public int health;

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
    health = UnitModel.MAXIMUM_HP;
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
    return getHealth() < UnitModel.MAXIMUM_HP;
  }
  public int getHealth()
  {
    return roundHealth(health);
  }
  public int getHP()
  {
    return roundHealth(health) / 10;
  }
  /** Rounds the input up to the nearest 10 (accounting for negatives) */
  public static int roundHealth(int health)
  {
    if( health >= 0 )
      //     "round up", then kill the last digit
      return (health + 9) / 10 * 10;
    // Truncation rounds toward zero, so we need to round down for negative values
    return (health - 9) / 10 * 10;
  }

  /**
   * Reduces health by the specified amount.
   * <p>Enforces a minimum (optional) of 0.
   * <p>Use this for lethal damage, especially unit-on-unit violence. Do not use for healing.
   * @return the change in *rounded* health
   */
  public int damageHealth(int damage)
  {
    return damageHealth(damage, false);
  }
  public int damageHealth(int damage, boolean allowOverkill)
  {
    if( damage < 0 )
      throw new ArithmeticException("Cannot inflict negative damage!");
    int before = getHealth();
    health = health - damage;
    if( !allowOverkill )
      health = Math.max(0, health);
    return getHealth() - before;
  }

  /**
   * Increases health by the specified amount.
   * <p>Enforces a minimum of 1, and a maximum (optional) of MAXIMUM_HP.
   * <p>When healing, rounds up (optional) to the next 10 (e.g. 25 + 20 = 45 -> 50)
   * <p>Use this for most non-combat HP changes (mass damage/silos/healing).
   * @return the change in *rounded* health value (may be more or less than the actual change)
   */
  public int alterHealth(int change)
  {
    return alterHealth(change, true, false);
  }
  public int alterHealth(int change, boolean allowOver)
  {
    return alterHealth(change, true, allowOver);
  }
  public int alterHealth(int change, boolean roundUp, boolean allowOver)
  {
    final int oldHP = getHealth();
    int realChange = change;

    // Only enforce the maximum HP if we're healing
    if( !allowOver && change > 0 )
    {
      // If we already have overhealing, treat current HP as the max to avoid e.g. heals reducing HP
      final int capHP = Math.max(oldHP, UnitModel.MAXIMUM_HP);
      // Apply the cap as needed
      final int newHP = Math.min(capHP, oldHP + change);
      // Figure out whether that reduces our healing
      realChange = Math.min(change, newHP - health);
    }

    health = Math.max(1, health + realChange);
    // Round HP up, if healing
    if( roundUp && change >= 0 )
      health = getHealth();

    return getHealth() - oldHP;
  }

  public int alterHealthNoRound(int percentChange)
  {
    return alterHealth(percentChange, false, false);
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
