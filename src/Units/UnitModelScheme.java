package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import Terrain.TerrainType;

/**
 * Provides the primary interface for the game to get info about game-specific units.
 */
public abstract class UnitModelScheme implements Serializable
{
  private static final long serialVersionUID = 1L;

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
}
