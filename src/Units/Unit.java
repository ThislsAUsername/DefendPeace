package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionType;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;
import Units.Weapons.WeaponModel;

public class Unit implements Serializable
{
  private static final long serialVersionUID = 1L;
  public Vector<Unit> heldUnits;
  public UnitModel model;
  public int x;
  public int y;
  public int fuel;
  private int captureProgress;
  private Location captureTarget;
  public Commander CO;
  public boolean isTurnOver;
  public boolean isStunned;
  private double HP;
  public ArrayList<Weapon> weapons;

  public Unit(Commander co, UnitModel um)
  {
    CO = co;
    model = um;
    fuel = model.maxFuel;
    isTurnOver = true;
    HP = model.maxHP;
    captureProgress = 0;
    captureTarget = null;
    if( model.weaponModels != null )
    {
      weapons = new ArrayList<Weapon>();
      for( WeaponModel weapType : model.weaponModels )
      {
        weapons.add(new Weapon(weapType));
      }
    }
    else
    {
      // Just make sure we don't crash if we try to iterate on this.
      weapons = new ArrayList<Weapon>();
    }
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
    if( null != locus )
    {
      if( isStunned )
      {
        isTurnOver = true;
        isStunned = false;
      }
      else
        isTurnOver = false;
      fuel -= model.idleFuelBurn;
      if( captureTarget != null && captureTarget.getResident() != this )
      {
        captureTarget = null;
        captureProgress = 0;
      }

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

      // Collect any turn-initialization actions for this unit.
      for( GameAction ga : model.getTurnInitActions(this) )
      {
        events.addAll(ga.getEvents(map));
      }
    } // ~If location is valid.

    return events;
  }

  /**
   * @return whether or not this unit can attack the given unit type at the
   * specified range, accounting for the possibility of moving first.
   */
  public boolean canAttack(UnitModel targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( weapons == null )
      return false;

    boolean canHit = false;
    for( Weapon weapon : weapons )
    {
      if( afterMoving && !weapon.model.canFireAfterMoving )
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
  public Weapon chooseWeapon(UnitModel targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( weapons == null )
      return null;

    Weapon chosenWeapon = null;
    double maxDamage = 0;
    for( Weapon weapon : weapons )
    {
      // If the weapon isn't mobile, we cannot fire if we moved.
      if( afterMoving && !weapon.model.canFireAfterMoving )
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

  public int getHP()
  {
    return (int) Math.ceil(HP);
  }
  public double getPreciseHP()
  {
    return HP;
  }

  public void damageHP(double damage)
  {
    HP -= damage;
    if( HP < 0 )
    {
      HP = 0;
    }
  }
  public double alterHP(int change)
  {
    double before = HP;
    // Change the unit's health, but don't grant more
    // than 10 HP, and don't drop HP to zero.
    HP = Math.max(0.1, Math.min(model.maxHP, getHP() + change));
    return HP - before;
  }

  public void capture(Location target)
  {
    if( !target.isCaptureable() )
    {
      System.out.println("ERROR! Attempting to capture an uncapturable Location!");
      return;
    }
    if( !CO.isEnemy(target.getOwner()) )
    {
      System.out.println("WARNING! Attempting to capture an allied property!");
      return;
    }

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
    }
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
    for( UnitActionType at : model.possibleActions )
    {
      GameActionSet actions = at.getPossibleActions(map, movePath, this, ignoreResident);
      if( null != actions )
        actionSet.add(actions);
    }

    return actionSet;
  }

  public boolean hasCargoSpace(UnitEnum type)
  {
    return (model.holdingCapacity > 0 && heldUnits.size() < model.holdingCapacity && model.holdables.contains(type));
  }

  /** Grant this unit full fuel and ammunition */
  public void resupply()
  {
    fuel = model.maxFuel;
    if( null != weapons )
    {
      for( Weapon wpn : weapons )
      {
        wpn.reload();
      }
    }
  }

  /** Returns true if resupply would have zero effect on this unit. */
  public boolean isFullySupplied()
  {
    boolean isFull = (model.maxFuel == fuel);
    if( isFull )
    {
      // Check weapon ammo.
      for( Weapon w : weapons )
      {
        isFull &= (w.model.maxAmmo == w.ammo);
      }
    }
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
