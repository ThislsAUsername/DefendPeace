package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import Engine.UnitActionType;
import Engine.GameEvents.GameEventQueue;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.MoveTypes.MoveType;

/**
 * Defines the invariant characteristics of a unit. One UnitModel can be shared across many instances of that Unit type.
 */
public abstract class UnitModel implements Serializable
{
  private static final long serialVersionUID = 1L;

  /**
   * Defines the core types of units we expect to be in any given unit set.
   * Unit sets should have no less than one of each.
   * Unit sets should order models such that the most "sensible" type (generally cheapest, land) is the first type of a given role.
   */
  public enum UnitRoleEnum
  {
    INFANTRY, MECH,
    RECON, ASSAULT,
    SIEGE, ANTI_AIR,
    AIR_ASSAULT, AIR_SUPERIORITY,
    TRANSPORT,
  };

  private static ArrayList<String> unitRoleIDs = null;
  public static ArrayList<String> getUnitRoleIDs()
  {
    if( null == unitRoleIDs )
    {
      unitRoleIDs = new ArrayList<String>();
      for( UnitRoleEnum role : UnitRoleEnum.values() )
        unitRoleIDs.add(standardizeID(role.toString()));
    }
    return unitRoleIDs;
  }

  public static String standardizeID(String input)
  {
    return input.toLowerCase().replaceAll(" ", "_").replaceAll("-", "_");
  }

  // Subs are ships unless they're submerged.
  public enum ChassisEnum
  {
    TROOP, TANK, AIR_LOW, AIR_HIGH, SHIP, SUBMERGED
  };

  public String name;
  public UnitRoleEnum role;
  public ChassisEnum chassis;
  protected int moneyCost;
  public double abilityPowerValue;
  public int maxAmmo;
  public int maxFuel;
  public int idleFuelBurn;
  public int maxMaterials = 0;
  public int movePower;
  public int visionRange;
  public int visionRangePiercing = 1;
  public boolean hidden = false;
  public MoveType propulsion;
  public ArrayList<UnitActionType> possibleActions = new ArrayList<UnitActionType>();
  public Set<TerrainType> healableHabs = new HashSet<TerrainType>();
  public ArrayList<WeaponModel> weapons = new ArrayList<WeaponModel>();

  public int maxHP = 10;
  public int holdingCapacity = 0;
  public Vector<ChassisEnum> holdables = new Vector<ChassisEnum>();
  private int COstr = 100;
  private int COdef = 100;
  public double COcost = 1.0;
  public int moneyCostAdjustment = 0;

  public UnitModel(String pName, UnitRoleEnum pRole, ChassisEnum pChassis, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision, int pMovePower,
      MoveType pPropulsion, UnitActionType[] actions, WeaponModel[] pWeapons, double powerValue)
  {
    this(pName, pRole, pChassis, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, powerValue);

    for( UnitActionType action : actions )
    {
      possibleActions.add(action);
    }
    for( WeaponModel wm : pWeapons )
    {
      weapons.add(wm.clone());
    }
  }

  public UnitModel(String pName, UnitRoleEnum pRole, ChassisEnum pChassis, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision, int pMovePower,
      MoveType pPropulsion, ArrayList<UnitActionType> actions, ArrayList<WeaponModel> pWeapons, double powerValue)
  {
    this(pName, pRole, pChassis, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, powerValue);
    possibleActions.addAll(actions);
    weapons = pWeapons;
  }

  private UnitModel(String pName, UnitRoleEnum pRole, ChassisEnum pChassis, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision, int pMovePower,
      MoveType pPropulsion, double powerValue)
  {
    name = pName;
    role = pRole;
    chassis = pChassis;
    moneyCost = cost;
    maxAmmo = pAmmoMax;
    abilityPowerValue = powerValue;
    maxFuel = pFuelMax;
    idleFuelBurn = pIdleFuelBurn;
    visionRange = pVision;
    movePower = pMovePower;
    propulsion = new MoveType(pPropulsion);

    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      if( (((chassis == ChassisEnum.AIR_HIGH) || (chassis == ChassisEnum.AIR_LOW)) && terrain.healsAir()) ||
          (((chassis == ChassisEnum.TANK) || (chassis == ChassisEnum.TROOP)) && terrain.healsLand()) ||
          (((chassis == ChassisEnum.SHIP) || (chassis == ChassisEnum.SUBMERGED)) && terrain.healsSea()) )
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
    holdingCapacity = other.holdingCapacity;
    holdables.addAll(other.holdables);

    // Duplicate other assorted values
    maxHP = other.maxHP;
    maxMaterials = other.maxMaterials;

    COstr = other.COstr;
    COdef = other.COdef;
    COcost = other.COcost;
    moneyCostAdjustment = other.moneyCostAdjustment;
  }

  public int getCost()
  {
    return (int) ((moneyCost+moneyCostAdjustment)*COcost);
  }

  /**
   * Takes a percent change and adds it to the current damage multiplier for this UnitModel.
   * @param change The percent damage to add; e.g. if the multiplier is 100 and this function is
   * called with 10, the new one will be 110. If it is called with 10 again, it will go to 120.
   */
  public void modifyDamageRatio(int change)
  {
    COstr += change;
  }
  public int getDamageRatio()
  {
    return COstr;
  }

  /**
   * Takes a percent change and adds it to the current defense modifier for this UnitModel.
   * @param change The percent defense to add; e.g. if the defense modifier is 100 and this function
   * is called with 10, the new one will be 110. If it is called with 10 again, it will go to 120.
   */
  public void modifyDefenseRatio(int change)
  {
    COdef += change;
  }
  public int getDefenseRatio()
  {
    return COdef;
  }

  public boolean canRepairOn(Location locus)
  {
    return healableHabs.contains(locus.getEnvironment().terrainType);
  }

  /** Provides a hook for inheritors to supply turn-initialization actions to a unit.
   * @param self Assumed to be a Unit of the model's type.
   */
  public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
  {
    // Most Units don't have any; specific UnitModel types can override.
    return new GameEventQueue();
  }

  /** Calls the appropriate type-specific override of getDamage() on the input WeaponModel */
  public abstract double getDamageRedirect(WeaponModel wm);

  /**
   * @return True if this UnitModel has at least one weapon with a minimum range of 1.
   */
  public boolean hasDirectFireWeapon()
  {
    boolean hasDirect = false;
    if(weapons != null && weapons.size() > 0)
    {
      for( WeaponModel wm : weapons )
      {
        if( wm.minRange == 1 )
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

  @Override
  public String toString()
  {
    return name;
  }

  public boolean hasActionType(UnitActionType UnitActionType)
  {
    boolean hasAction = false;
    for( UnitActionType at : possibleActions )
    {
      if( at == UnitActionType )
      {
        hasAction = true;
        break;
      }
    }
    return hasAction;
  }

  public boolean isSurfaceUnit()
  {
    return (ChassisEnum.SHIP == chassis) || (ChassisEnum.TANK == chassis) || (ChassisEnum.TROOP == chassis);
  }

  public boolean isAirUnit()
  {
    return (ChassisEnum.AIR_HIGH == chassis) || (ChassisEnum.AIR_LOW == chassis);
  }

  public boolean isLandUnit()
  {
    return (ChassisEnum.TANK == chassis) || (ChassisEnum.TROOP == chassis);
  }

  public boolean isSeaUnit()
  {
    return (ChassisEnum.SHIP == chassis) || (ChassisEnum.SUBMERGED == chassis);
  }
}
