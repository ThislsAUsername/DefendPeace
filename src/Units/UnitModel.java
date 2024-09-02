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
import lombok.Builder;
import lombok.experimental.SuperBuilder;

/**
 * Defines the invariant characteristics of a unit. One UnitModel can be shared across many instances of that Unit type.
 */
@SuperBuilder(toBuilder = true)
public class UnitModel implements Serializable, ITargetable, UnitModList
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
  public static final long CAPTURE         = 1 << 15; // Unit that can give you ownership of property
  public static final long MECH            = 1 << 16; // Footsoldier equipped against hardened targets
  public static final long RECON           = 1 << 17; // Scout
  public static final long ASSAULT         = 1 << 18; // Fast unit that can deal with hardened ground targets
  public static final long SIEGE           = 1 << 19; // Typically has range, but is primarily effective when stationary
  public static final long SURFACE_TO_AIR  = 1 << 20;
  public static final long AIR_TO_SURFACE  = 1 << 21;
  public static final long AIR_TO_AIR      = 1 << 22;
  public static final long TRANSPORT       = 1 << 23;

  // Calculated properties so I can be lazy
  public static final long DIRECT          = 1l << 50; // Has only weapons for 1 range
  public static final long INDIRECT        = 1l << 51; // Has weapons for 2+ range
  // A shift of 63 produces a negative, which is Uncool

  public static String standardizeID(String input)
  {
    return input.toLowerCase().replaceAll(" ", "_").replaceAll("-", "_");
  }

  public final int costBase;
  public final int baseMovePower;
  public final MoveType baseMoveType;
  public final ArrayList<UnitActionFactory> baseActions;
  @Builder.Default public final int baseCargoCapacity = 0;
  public static final int DEFAULT_STAT_RATIO = 100; // Accounts for firepower, defense, and cost

  // Dynamic modifications to any property below this line will require new additions to UnitContext and UnitModifier
  public static final int MAXIMUM_HEALTH = 100;
  public final String name;
  public long role; // Can't easily be final due to setCalculatedProps()
  public final int abilityPowerValue; // In percent of a star's value per GUI HP
  public final int maxAmmo;
  public final int maxFuel;
  public final int fuelBurnIdle;
  @Builder.Default public final int fuelBurnPerTile = 1;
  @Builder.Default public final int maxMaterials = 0;
  @Builder.Default public final boolean needsMaterials = true;
  public final int visionRange;
  @Builder.Default public final boolean visionPierces = false;
  @Builder.Default public final boolean hidden = false;
  public final Set<TerrainType> healableHabs = new HashSet<TerrainType>();
  @Builder.Default public final ArrayList<WeaponModel> weapons = new ArrayList<WeaponModel>();
  public final long carryableMask;
  public final long carryableExclusionMask;
  @Builder.Default public final Set<TerrainType> unloadExclusionTerrain = new HashSet<TerrainType>();

  @Builder.Default public final boolean supplyCargo = false;
  @Builder.Default public final boolean repairCargo = false;


  public void setCalculatedProps()
  {
    boolean isDirect = weapons.size() > 0;
    boolean isIndirect = false;
    for( WeaponModel wm : weapons )
    {
      if( wm.rangeMax == 1 )
        continue;
      isDirect = false;
      isIndirect = true;
      break;
    }
    if( isDirect )
      this.role |= DIRECT;
    if( isIndirect )
      this.role |= INDIRECT;

    if( baseActions.contains(UnitActionFactory.CAPTURE) )
      this.role |= CAPTURE;

    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      if( (isAny(AIR_LOW | AIR_HIGH) && terrain.healsAir())  ||
          (isAny(LAND)               && terrain.healsLand()) ||
          (isAny(SEA)                && terrain.healsSea())  )
        healableHabs.add(terrain);
    }
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

    if( supplyCargo )
      for( Unit cargo : self.heldUnits )
      {
        if( !cargo.isFullySupplied() )
          queue.add(new ResupplyEvent(self, cargo));
      }
    if( repairCargo )
      for( Unit cargo : self.heldUnits )
      {
        queue.add(new HealUnitEvent(cargo, self.CO.getRepairPower(), self.CO.army)); // Event handles cost logic
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

  @Override
  public int getDamageRedirect(WeaponModel wm)
  {
    throw new UnsupportedOperationException("Called base UnitModel.getDamageRedirect()");
  }
}
