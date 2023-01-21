package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import Engine.GameInstance;
import Engine.UnitMods.UnitModifier;
import Terrain.TerrainType;

/**
 * Provides the primary interface for the game to get info about game-specific units.
 */
public abstract class UnitModelScheme implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  /** Indicates whether, for the given map/game, all the units can be instantiated */
  public boolean schemeValid = true;
  @Override
  public String toString()
  {
    return (schemeValid ? "" : "!");
  }

  /**
   * When the UI wants to show a unit type, it should use this string.
   * Typically the name of whatever passes for infantry, but can be anything.
   */
  public abstract String getIconicUnitName();
  
  // Fetches a shopping list and a full unit list for a generic army.
  private GameReadyModels grms = null;
  public final GameReadyModels getGameReadyModels()
  {
    if(grms != null)
      return grms;
    grms = buildGameReadyModels();
    return grms;
  }
  protected abstract GameReadyModels buildGameReadyModels();

  public void registerStateTrackers(GameInstance gi)
  {
    for( UnitModel um : getGameReadyModels().unitModels )
      for( UnitModifier mod : um.getModifiers() )
        mod.registerTrackers(gi);
  }

  // Holds the data for a single Commander's unit selection.
  public static class GameReadyModels implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public HashMap<TerrainType, ArrayList<UnitModel>> shoppingList = new HashMap<TerrainType, ArrayList<UnitModel>>();
    public UnitModelList unitModels = new UnitModelList();
  }
  
  public static UnitModel getModelFromString(String pName, Collection<UnitModel> models)
  {
    String name = UnitModel.standardizeID(pName);
    UnitModel model = null;
    for( UnitModel um : models )
      if( UnitModel.standardizeID(um.name).equals(name) )
      {
        model = um;
        break;
      }
    return model;
  }
}
