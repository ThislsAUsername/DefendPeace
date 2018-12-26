package Units;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import Engine.GameAction;
import Engine.GameAction.ActionType;
import Terrain.Location;
import Terrain.TerrainType;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

/**
 * Defines the invariant characteristics of a unit. One UnitModel can be shared across many instances of that Unit type.
 */
public class UnitModel
{
  public enum UnitEnum
  {
    INFANTRY, MECH, RECON, TANK, MD_TANK, NEOTANK, APC, ARTILLERY, ROCKETS, ANTI_AIR, MOBILESAM, FIGHTER, BOMBER, B_COPTER, T_COPTER, BATTLESHIP, CRUISER, LANDER, SUB
  };

  // Subs are ships unless they're submerged.
  public enum ChassisEnum
  {
    TROOP, TANK, AIR_LOW, AIR_HIGH, SHIP, SUBMERGED
  };

  public String name;
  public UnitEnum type;
  public ChassisEnum chassis;
  private int moneyCost = 9001;
  public int maxFuel;
  public int idleFuelBurn;
  public int movePower;
  public MoveType propulsion;
  public ActionType[] possibleActions;
  public Set<TerrainType> healableHabs;
  public WeaponModel[] weaponModels;

  public int maxHP;
  public int holdingCapacity;
  public Vector<UnitEnum> holdables;
  private int COstr;
  private int COdef;
  public double COcost = 1.0;

  public UnitModel(String pName, UnitEnum pType, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower,
      MoveType pPropulsion, ActionType[] actions, WeaponModel[] weapons)
  {
    name = pName;
    type = pType;
    chassis = pChassis;
    moneyCost = cost;
    maxFuel = pFuelMax;
    idleFuelBurn = pIdleFuelBurn;
    movePower = pMovePower;
    propulsion = pPropulsion;
    possibleActions = actions;
    healableHabs = new HashSet<TerrainType>();
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      if( ((chassis == ChassisEnum.AIR_HIGH) || (chassis == ChassisEnum.AIR_LOW) && terrain.healsAir())
          || ((chassis == ChassisEnum.TANK) || (chassis == ChassisEnum.TROOP) && terrain.healsLand())
          || ((chassis == ChassisEnum.SHIP) || (chassis == ChassisEnum.SUBMERGED) && terrain.healsSea()) )
        healableHabs.add(terrain);
    }
    if( weapons != null )
    {
      weaponModels = new WeaponModel[weapons.length];
      for( int i = 0; i < weapons.length; i++ )
      {
        weaponModels[i] = new WeaponModel(weapons[i]);
      }
    }

    maxHP = 10;
    COstr = 100;
    COdef = 100;
    holdingCapacity = 0;
    holdables = new Vector<UnitEnum>();
  }

  /**
   * Copy-constructor. Does a deep-copy on 'other' to allow easy creation of
   * unit types that are similar to exiting types.
   * @param other The UnitModel to clone.
   * @return The UnitModel clone.
   */
  public static UnitModel clone(UnitModel other)
  {
    // Make a copy of the weapons used by the other model.
    WeaponModel[] weaponModels = null;
    if( other.weaponModels != null )
    {
      weaponModels = new WeaponModel[other.weaponModels.length];
      for( int i = 0; i < weaponModels.length; ++i )
      {
        weaponModels[i] = new WeaponModel(other.weaponModels[i]);
      }
    }

    // Create a new model with the given attributes.
    UnitModel newModel = new UnitModel(other.name, other.type, other.chassis, other.getCost(), other.maxFuel, other.idleFuelBurn,
        other.movePower, new MoveType(other.propulsion), other.possibleActions, weaponModels);

    // Duplicate the other model's transporting abilities.
    newModel.holdingCapacity = other.holdingCapacity;
    newModel.holdables.addAll(other.holdables);

    return newModel;
  }

  public int getCost()
  {
    return (int) (moneyCost * COcost);
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
  public ArrayList<GameAction> getTurnInitActions(Unit self)
  {
    // Most Units don't have any; specific UnitModel types can override.
    return new ArrayList<GameAction>();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
