package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Terrain.Location;
import Terrain.Environment.Terrains;
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
  public Terrains[] healableHabs;
  public WeaponModel[] weaponModels;

  public int maxHP;
  public int holdingCapacity;
  public Vector<UnitEnum> holdables;
  private int COstr;
  private int COdef;

  public UnitModel(String pName, UnitEnum pType, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower, MoveType pPropulsion,
      ActionType[] actions, Terrains[] healableTerrains, WeaponModel[] weapons)
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
    healableHabs = healableTerrains;
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
    Terrains environs = locus.getEnvironment().terrainType;
    boolean compatible = false;
    for( Terrains terrain : healableHabs )
    {
      compatible |= environs == terrain;
    }
    return compatible;
  }
}
