package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import CommandingOfficers.Commander;
import Engine.FloodFillFunctor;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;

public class Unit implements Serializable
{
  private static final long serialVersionUID = 1L;
  public Vector<Unit> heldUnits;
  public UnitModel model;
  public int x = -1;
  public int y = -1;
  public int ammo;
  public int fuel;
  public int materials;
  private int captureProgress;
  private Location captureTarget;
  public Commander CO;
  public boolean isTurnOver;
  public boolean isStunned;
  private int health; // HP value as a percentage, thus 10x displayed HP value

  public Unit(Commander co, UnitModel um)
  {
    CO = co;
    model = um;
    ammo = model.maxAmmo;
    fuel = model.maxFuel;
    materials = model.maxMaterials;
    isTurnOver = true;
    health = model.maxHP * 10;
    captureProgress = 0;
    captureTarget = null;
    if( model.holdingCapacity > 0 )
      heldUnits = new Vector<Unit>(model.holdingCapacity);
  }

  /**
   * Ready this unit for the next turn. Any actions it performs as part of
   * initialization will be returned in a GameEventQueue.
   * @param map
   * @param events
   */
  public GameEventQueue initTurn(MapMaster map)
  {
    // Make a queue to return any init events.
    GameEventQueue events = new GameEventQueue();

    Location locus = map.getLocation(x, y);

    // Only perform turn initialization for the unit if it is on the map.
    //   Units that are e.g. in a transport don't burn fuel, etc.
    if( isStunned )
    {
      isTurnOver = true;
      isStunned = false;
    }
    else
      isTurnOver = false;
    if( captureTarget != null && captureTarget.getResident() != this )
    {
      captureTarget = null;
      captureProgress = 0;
    }

    if( null != heldUnits )
      for( Unit cargo : heldUnits )
        events.addAll(cargo.initTurn(map));

    if( null != locus )
    {
      fuel -= model.idleFuelBurn;
      // If the unit is not at max health, and is on a repair tile, heal it.
      if( model.canRepairOn(locus) && !CO.isEnemy(locus.getOwner()) )
      {
        events.add(new HealUnitEvent(this, CO.getRepairPower(), CO)); // Event handles cost logic
        // Resupply is free; whether or not we can repair, go ahead and add the resupply event.
        if( !isFullySupplied() )
        {
          events.add(new ResupplyEvent(this));
        }
      }

      // Collect any turn-initialization events for this unit.
      events.addAll(model.getTurnInitEvents(this, map));
    } // ~If location is valid.

    return events;
  }

  public FloodFillFunctor getMoveFunctor(boolean includeOccupied)
  {
    // Units cannot normally pass through enemies
    return getMoveFunctor(includeOccupied, false);
  }
  public FloodFillFunctor getMoveFunctor(boolean includeOccupied, boolean canTravelThroughEnemies)
  {
    return model.propulsion.getUnitMoveFunctor(this, includeOccupied, canTravelThroughEnemies);
  }

  /**
   * @return whether or not this unit can attack the given unit type at the
   * specified range, accounting for the possibility of moving first.
   */
  public boolean canAttack(UnitModel targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( model.weapons == null )
      return false;

    boolean canHit = false;
    for( WeaponModel weapon : model.weapons )
    {
      if( afterMoving && !weapon.canFireAfterMoving )
      {
        // If we are planning to move first, and the weapon
        // can't shoot after moving, then move along.
        continue;
      }
      if( weapon.getDamage(targetType, range) > 0 )
      {
        canHit = true;
        break;
      }
    }
    return canHit;
  }

  /**
   * Select the weapon owned by this unit that can inflict the
   * most damage against the chosen target
   * @param target
   * @param range
   * @param afterMoving
   * @return The best weapon for that target, or null if no usable weapon exists.
   */
  public WeaponModel chooseWeapon(UnitModel targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( model.weapons == null )
      return null;

    WeaponModel chosenWeapon = null;
    double maxDamage = 0;
    for( WeaponModel weapon : model.weapons )
    {
      // If the weapon isn't mobile, we cannot fire if we moved.
      if( afterMoving && !weapon.canFireAfterMoving )
      {
        continue;
      }
      double currentDamage = weapon.getDamage(targetType, range);
      if( weapon.getDamage(targetType, range) > maxDamage )
      {
        chosenWeapon = weapon;
        maxDamage = currentDamage;
      }
    }
    return chosenWeapon;
  }

  /** Expend ammo, if the weapon uses ammo */
  public void fire(WeaponModel weapon)
  {
    if( !weapon.hasInfiniteAmmo )
    {
      if( ammo > 0 )
        ammo--;
      else
        System.out.println("WARNING: fired with no available ammo!");
    }
  }

  public int getHP()
  {
    return (int) Math.ceil(healthToHP(health));
  }
  public double getPreciseHP()
  {
    return healthToHP(health);
  }
  private static double healthToHP(int input)
  {
    return ((double)input)/10;
  }

  /**
   * Reduces HP by the specified amount.
   * Enforces a minimum of 0.
   */
  public void damageHP(double damage)
  {
    health -= damage*10;
    if( health < 0 )
    {
      health = 0;
    }
  }

  /**
   * Increases HP by the specified amount.
   * Enforces a minimum of 0.1.
   * Enforces model.maxHP.
   * @return the change in HP
   */
  public double alterHP(int change)
  {
    int before = health;
    health = Math.max(1, Math.min(model.maxHP, getHP() + change) * 10);
    return healthToHP(health - before);
  }

  public boolean capture(Location target)
  {
    boolean success = false;

    if( target != captureTarget )
    {
      captureTarget = target;
      captureProgress = 0;
    }
    captureProgress += getHP();
    if( captureProgress >= 20 )
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

  /** Compiles and returns a list of all actions this unit could perform on map after moving along movePath. */
  public ArrayList<GameActionSet> getPossibleActions(GameMap map, Path movePath)
  {
    return getPossibleActions(map, movePath, false);
  }
  public ArrayList<GameActionSet> getPossibleActions(GameMap map, Path movePath, boolean ignoreResident)
  {
    ArrayList<GameActionSet> actionSet = new ArrayList<GameActionSet>();
    for( UnitActionFactory at : model.possibleActions )
    {
      GameActionSet actions = at.getPossibleActions(map, movePath, this, ignoreResident);
      if( null != actions )
        actionSet.add(actions);
    }

    return actionSet;
  }

  public boolean hasCargoSpace(long type)
  {
    return (model.holdingCapacity > 0 && 
            heldUnits.size() < model.holdingCapacity &&
            ((model.carryableMask & type) > 0) &&
            ((model.carryableExclusionMask & type) == 0));
  }

  /** Grant this unit full fuel and ammunition */
  public void resupply()
  {
    fuel = model.maxFuel;
    ammo = model.maxAmmo;
  }

  /** Returns true if resupply would have zero effect on this unit. */
  public boolean isFullySupplied()
  {
    boolean isFull = (model.maxFuel == fuel);
    isFull &= (model.maxAmmo == ammo);
    return isFull;
  }

  @Override
  public String toString()
  {
    return model.toString();
  }

  public String toStringWithLocation()
  {
    return String.format("%s at %s", model, new XYCoord(x, y));
  }

}
