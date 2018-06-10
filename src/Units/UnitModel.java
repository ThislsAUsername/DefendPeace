package Units;

import java.util.ArrayList;
import java.util.Vector;

import Engine.GameAction;
import Engine.GameAction.ActionType;
import Terrain.Environment;
import Terrain.Location;
import Terrain.Types.BaseTerrain;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

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
  public int moneyCost = 9001;
  public int maxFuel;
  public int idleFuelBurn;
  public int movePower;
  public MoveType propulsion;
  public ActionType[] possibleActions;
  public BaseTerrain[] healableHabs;
  public WeaponModel[] weaponModels;

  public int maxHP;
  public int holdingCapacity;
  public Vector<UnitEnum> holdables;
  private int COstr;
  private int COdef;

  public UnitModel(String pName, UnitEnum pType, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion,
      ActionType[] actions, boolean isLand, boolean isAir, boolean isSea, WeaponModel[] weapons)
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
    ArrayList<BaseTerrain> healableTerrains = new ArrayList<BaseTerrain>();
    for (BaseTerrain terrain : Environment.getTerrainTypes())
    {
      if( isAir  && terrain.healsAir() ||
          isLand && terrain.healsLand() || 
          isSea  && terrain.healsSea() )
        healableTerrains.add(terrain);
    }
    healableHabs = new BaseTerrain[healableTerrains.size()];
    healableHabs = healableTerrains.toArray(healableHabs);
    weaponModels = weapons;

    maxHP = 10;
    COstr = 100;
    COdef = 100;
    holdingCapacity = 0;
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
    BaseTerrain environs = locus.getEnvironment().terrainType;
    boolean compatible = false;
    for( BaseTerrain terrain : healableHabs )
    {
      compatible |= environs == terrain;
    }
    return compatible;
  }

  /** Provides a hook for inheritors to supply turn-initialization actions to a unit.
   * @param self Assumed to be a Unit of the model's type.
   */
  public ArrayList<GameAction> getTurnInitActions(Unit self)
  {
    // Most Units don't have any; specific UnitModel types can override.
    return new ArrayList<GameAction>();
  }
}
