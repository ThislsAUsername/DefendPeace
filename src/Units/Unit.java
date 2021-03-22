package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import CommandingOfficers.Commander;
import Engine.FloodFillFunctor;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitModList;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;

public class Unit implements Serializable, UnitModList
{
  private static final long serialVersionUID = 1L;
  public CargoList heldUnits;
  public UnitModel model;
  public int x = -1;
  public int y = -1;
  public int ammo;
  public int fuel;
  public int materials;
  private int captureProgress;
  private MapLocation captureTarget;
  public Commander CO;
  public boolean isTurnOver;
  public boolean isStunned;

  /**
   * HP is a value, typically in range [1-10], that determines the current actual strength of a unit.
   * A unit at 0 HP is dead.
   * Health is HP value as a percentage, thus ~10x the HP value.
   * When determining HP, health must always be rounded up.
   */
  private int health;

  public Unit(Commander co, UnitModel um)
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

    MapLocation locus = map.getLocation(x, y);

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
      fuel = Math.max(0, fuel - model.idleFuelBurn);

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
  public boolean canAttack(ITargetable targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( model.weapons == null )
      return false;

    boolean canHit = false;
    for( WeaponModel weapon : model.weapons )
    {
      if( !weapon.loaded(this) ) continue; // Can't shoot with no bullets.

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
   * @return Whether this unit has a weapon with ammo that can hit `targetType`
   * under any combination of ranged/direct, move-first/static.
   */
  public boolean canTarget(UnitModel targetType)
  {
    // if we have no weapons, we can't hurt things
    if( model.weapons == null )
      return false;

    boolean canHit = false;
    for( WeaponModel weapon : model.weapons )
    {
      if( !weapon.loaded(this) ) continue; // Can't shoot with no bullets.

      if( weapon.getDamage(targetType) > 0 )
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
  public WeaponModel chooseWeapon(ITargetable targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( model.weapons == null )
      return null;

    WeaponModel chosenWeapon = null;
    double maxDamage = 0;
    for( WeaponModel weapon : model.weapons )
    {
      if( !weapon.loaded(this) ) continue; // Can't shoot with no bullets.

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
        System.out.println("WARNING: " + toStringWithLocation() + " fired with no available ammo!");
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
    return getHP() / (double)model.maxHP;
  }
  /** @return un-rounded HP */
  public double getPreciseHP()
  {
    return healthToHP(health);
  }
  private static double healthToHP(int input)
  {
    return ((double)input)/10;
  }
  private static int healthFromHP(double input)
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
    if( damage < 0 ) throw new ArithmeticException("Cannot inflict negative damage!");
    int before = getHP();
    health = Math.max(0, health - healthFromHP(damage));
    return getHP() - before;
  }

  /**
   * Increases HP by the specified amount.
   * Enforces a minimum of 0.1.
   * When healing, sets health to the maximum value for its HP
   * @return the change in HP
   */
  public int alterHP(int change) { return alterHP(change, false); }
  public int alterHP(int change, boolean allowOver)
  {
    int before = getHP();
    int newHP = allowOver ? getHP()+change : Math.min(model.maxHP, getHP() + change);
    health = Math.max(1, healthFromHP(newHP));
    if (change > 0)
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

  /** Compiles and returns a list of all actions this unit could perform on map after moving along movePath. */
  public ArrayList<GameActionSet> getPossibleActions(GameMap map, GamePath movePath)
  {
    return getPossibleActions(map, movePath, false);
  }
  public ArrayList<GameActionSet> getPossibleActions(GameMap map, GamePath movePath, boolean ignoreResident)
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

  public static class CargoList extends ArrayList<Unit>
  {
    private static final long serialVersionUID = 1L;
    UnitModel model;
    public CargoList(UnitModel model)
    {
      super(model.holdingCapacity);
      this.model = model;
    }

    @Override
    public boolean add(Unit u)
    {
      if( size() >= model.holdingCapacity )
        throw new IllegalStateException("Cannot put a unit into a transport that is already full!");
      return super.add(u);
    }
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

  private final ArrayList<UnitModifier> unitMods = new ArrayList<UnitModifier>();
  @Override
  public List<UnitModifier> getModifiers()
  {
    ArrayList<UnitModifier> output = new ArrayList<UnitModifier>();
    output.addAll(model.getModifiers());
    output.addAll(unitMods);
    return output;
  }

  @Override
  public void apply(UnitModifier unitModifier)
  {
    UnitModList.super.apply(unitModifier);
    unitMods.add(unitModifier);
  }
  @Override
  public void remove(UnitModifier unitModifier)
  {
    unitMods.remove(unitModifier);
  }
}
