package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import Terrain.TerrainType;
import Units.UnitModel.UnitRoleEnum;

/**
 * Provides the primary interface for the game to get info about game-specific units.
 */
public abstract class UnitModelScheme implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  /** Indicates whether, for the given map/game, all the units can be instantiated */
  public boolean schemeValid = true;

  /**
   * When the UI wants to show a unit type, it should use this string.
   * Typically the name of whatever passes for infantry, but can be anything.
   */
  public abstract String getIconicUnitName();
  
  // Fetches a shopping list and a full unit list for a generic commander.
  public abstract GameReadyModels getGameReadyModels();

  // Holds the data for a single Commander's unit selection.
  public static class GameReadyModels
  {
    public HashMap<TerrainType, ArrayList<UnitModel>> shoppingList = new HashMap<TerrainType, ArrayList<UnitModel>>();
    public ArrayList<UnitModel> unitModels = new ArrayList<UnitModel>();
  }
  
  public static UnitModel getModelFromString(String pName, ArrayList<UnitModel> models)
  {
    String name = UnitModel.standardizeID(pName);
    UnitModel model = null;
    ArrayList<String> coreRoles = UnitModel.getUnitRoleIDs();
    if (coreRoles.contains(name))
    {
      UnitRoleEnum role = UnitModel.UnitRoleEnum.values()[coreRoles.indexOf(name)];
      for( UnitModel um : models )
        if( um.role.equals(role) )
        {
          model = um;
          break;
        }
    }
    else // If it's not a core type, see if there's a name match
    {
      for( UnitModel um : models )
        if( UnitModel.standardizeID(um.name).equals(name) )
        {
          model = um;
          break;
        }
    }
    return model;
  }
}
