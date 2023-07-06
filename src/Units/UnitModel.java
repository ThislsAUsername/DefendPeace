package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Terrain.MapLocation;
import Engine.UnitMods.UnitModList;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.MoveTypes.MoveType;

/**
 * Defines the invariant characteristics of a unit. One UnitModel can be shared across many instances of that Unit type.
 */
public abstract class UnitModel implements Serializable, ITargetable, UnitModList
{
  private static final long serialVersionUID = 1L;

  /**
   * These are the high-level traits on which to filter unit types.
   * Unit sets should order models such that the most "sensible" type
   *   (generally cheapest, land, assault) is the first type fulfilling a given role.
   */
  // Morphology
  public static final long TROOP           = 1 <<  0; // Not in vehicle
  public static final long TANK            = 1 <<  1; // Ground-traveling vehicle
  public static final long HOVER           = 1 <<  2; // e.g. helicopter
  public static final long JET             = 1 <<  3;
  public static final long SHIP            = 1 <<  4;
  // Domain
  public static final long LAND            = 1 <<  8;
  public static final long AIR_LOW         = 1 <<  9;
  public static final long AIR_HIGH        = 1 << 10;
  public static final long SEA             = 1 << 11;
  public static final long SUBSURFACE      = 1 << 12;
  // Role; Unit sets are expected to have at least one of each
  public static final long MECH            = 1 << 16; // Footsoldier equipped against hardened targets
  public static final long RECON           = 1 << 17; // Scout
  public static final long ASSAULT         = 1 << 18; // Fast unit that can deal with hardened ground targets
  public static final long SIEGE           = 1 << 19; // Typically has range, but is primarily effective when stationary
  public static final long SURFACE_TO_AIR  = 1 << 20;
  public static final long AIR_TO_SURFACE  = 1 << 21;
  public static final long AIR_TO_AIR      = 1 << 22;
  public static final long TRANSPORT       = 1 << 23;

  public static String standardizeID(String input)
  {
    return input.toLowerCase().replaceAll(" ", "_").replaceAll("-", "_");
  }

  public int costBase;
  public int baseMovePower;
  public MoveType baseMoveType;
  public ArrayList<UnitActionFactory> baseActions = new ArrayList<UnitActionFactory>();
  public int baseCargoCapacity = 0;
  public static final int DEFAULT_STAT_RATIO = 100; // Accounts for firepower, defense, and cost

  // Dynamic modifications to any property below this line will require new additions to UnitContext and UnitModifier
  public static final int MAXIMUM_HP = 10;
  public String name;
  public long role;
  public double abilityPowerValue;
  public int maxAmmo;
  public int maxFuel;
  public int fuelBurnIdle;
  public int fuelBurnPerTile = 1;
  public int maxMaterials = 0;
  public boolean needsMaterials = true;
  public int visionRange;
  public int visionRangePiercing = 1;
  public boolean hidden = false;
  public Set<TerrainType> healableHabs = new HashSet<TerrainType>();
  public ArrayList<WeaponModel> weapons = new ArrayList<WeaponModel>();
  public long carryableMask;
  public long carryableExclusionMask;
  public Set<TerrainType> unloadExclusionTerrain = new HashSet<TerrainType>();

  public UnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision, int pMovePower,
      MoveType pPropulsion, UnitActionFactory[] actions, WeaponModel[] pWeapons, double powerValue)
  {
    this(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, powerValue);

    for( UnitActionFactory action : actions )
    {
      baseActions.add(action);
    }
    for( WeaponModel wm : pWeapons )
    {
      weapons.add(wm.clone());
    }
  }

  public UnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision, int pMovePower,
      MoveType pPropulsion, ArrayList<UnitActionFactory> actions, ArrayList<WeaponModel> pWeapons, double powerValue)
  {
    this(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, powerValue);
    baseActions.addAll(actions);
    weapons = pWeapons;
  }

  private UnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision, int pMovePower,
      MoveType pPropulsion, double powerValue)
  {
    name = pName;
    role = pRole;
    costBase = cost;
    maxAmmo = pAmmoMax;
    abilityPowerValue = powerValue;
    maxFuel = pFuelMax;
    fuelBurnIdle = pIdleFuelBurn;
    visionRange = pVision;
    baseMovePower = pMovePower;
    baseMoveType = pPropulsion.clone();

    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      if( (isAny(AIR_LOW | AIR_HIGH) && terrain.healsAir())  ||
          (isAny(LAND)               && terrain.healsLand()) ||
          (isAny(SEA)                && terrain.healsSea())  )
        healableHabs.add(terrain);
    }
  }

  /**
   * Copy-constructor. Does a deep-copy to allow easy creation of
   * unit types that are similar to existing types.
   * @param other The UnitModel to clone.
   * @return The UnitModel clone.
   */
  @Override
  public abstract UnitModel clone();

  /** Copies stuff that isn't directly handled by the constructor. */
  protected void copyValues(UnitModel other)
  {
    // Duplicate the other model's transporting abilities.
    baseCargoCapacity = other.baseCargoCapacity;
    carryableMask = other.carryableMask;
    carryableExclusionMask = other.carryableExclusionMask;

    // Duplicate other assorted values
    maxMaterials = other.maxMaterials;
    needsMaterials = other.needsMaterials;
    fuelBurnPerTile = other.fuelBurnPerTile;
    for( UnitModifier mod : other.unitMods )
      unitMods.add(mod);
  }

  public boolean canRepairOn(MapLocation locus)
  {
    return healableHabs.contains(locus.getEnvironment().terrainType);
  }

  /** Provides a hook for inheritors to supply turn-initialization actions to a unit.
   * Overriders should collect the output of super.getTurnInitEvents() before returning.
   * @param self Assumed to be a Unit of the model's type.
   * @param map The current true state of the game map.
   */
  public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
  {
    GameEventQueue queue = new GameEventQueue();

    XYCoord xyc = new XYCoord(self.x, self.y);
    MapLocation loc = map.getLocation(xyc);

    // No actions for units in transports. Should also be checked in Unit.
    if( null == loc ) return queue;

    boolean resupplying = false;

    // If the unit is not at max health, and is on a repair tile, heal it.
    if( canRepairOn(loc) && !self.CO.isEnemy(loc.getOwner()) )
    {
      queue.add(new HealUnitEvent(self, self.CO.getRepairPower(), self.CO.army)); // Event handles cost logic
      // Resupply is free; whether or not we can repair, go ahead and add the resupply event.
      if( !self.isFullySupplied() )
      {
        resupplying = true;
        queue.add(new ResupplyEvent(self, self));
      }
    }

    // If there is an adjacent APC or similar, resupply.
    if( !resupplying && !self.isFullySupplied() )
    {
      List<XYCoord> adjacents = Utils.findLocationsInRange(map, xyc, 1);
      for( XYCoord adj : adjacents )
      {
        Unit res = map.getLocation(adj).getResident();
        if( (null != res) && !res.CO.isEnemy(self.CO) && res.hasActionType(UnitActionFactory.RESUPPLY) )
        {
          queue.add(new ResupplyEvent(res, self));
          resupplying = true;
          break;
        }
      }
    }

    if( !resupplying && (0 == self.fuel) & (isAirUnit() || isSeaUnit()) )
    {
      // Uh oh. It's crashy crashy time.
      Utils.enqueueDeathEvent(self, queue);
    }

    return queue;
  }

  public boolean needsFuel()
  {
    boolean output = false;
    output |= fuelBurnIdle > 0;
    output |= fuelBurnPerTile > 0;
    return output;
  }

  /**
   * @return True if this UnitModel has at least one weapon that normally has a minimum range of 1.
   */
  public boolean hasDirectFireWeapon()
  {
    boolean hasDirect = false;
    if(weapons != null && weapons.size() > 0)
    {
      for( WeaponModel wm : weapons )
      {
        if( wm.rangeMin == 1 )
        {
          hasDirect = true;
          break;
        }
      }
    }
    return hasDirect;
  }

  /**
   * @return True if this UnitModel has at least one weapon that cannot fire after moving.
   */
  public boolean hasImmobileWeapon()
  {
    boolean hasSiege = false;
    for( WeaponModel wm : weapons )
    {
      if( !wm.canFireAfterMoving )
      {
        hasSiege = true;
        break;
      }
    }
    return hasSiege;
  }
  public boolean hasMobileWeapon()
  {
    boolean hasStrike = false;
    for( WeaponModel wm : weapons )
    {
      if( wm.canFireAfterMoving )
      {
        hasStrike = true;
        break;
      }
    }
    return hasStrike;
  }

  @Override
  public String toString()
  {
    return name;
  }

  public boolean isAny(long input)
  {
    return (role & input) > 0;
  }
  public boolean isNone(long input)
  {
    return (role & input) == 0;
  }
  public boolean isAll(long input)
  {
    return (role & input) == input;
  }

  public boolean isSurfaceUnit()
  {
    return isAny(LAND | SEA) && isNone(SUBSURFACE);
  }

  public boolean isAirUnit()
  {
    return isAny(AIR_LOW | AIR_HIGH);
  }

  public boolean isLandUnit()
  {
    return isAll(LAND);
  }

  public boolean isSeaUnit()
  {
    return isAll(SEA);
  }

  public boolean isTroop()
  {
    return isAll(TROOP);
  }

  private final ArrayList<UnitModifier> unitMods = new ArrayList<>();
  @Override
  public List<UnitModifier> getModifiers()
  {
    ArrayList<UnitModifier> output = new ArrayList<>();
    output.addAll(unitMods);
    return output;
  }

  @Override
  public void addUnitModifier(UnitModifier unitModifier)
  {
    unitMods.add(unitModifier);
  }
  @Override
  public void removeUnitModifier(UnitModifier unitModifier)
  {
    unitMods.remove(unitModifier);
  }
}
