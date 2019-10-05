package Units;

import java.util.ArrayList;
import java.util.HashMap;

import Terrain.TerrainType;

public abstract class UnitModelScheme
{
  public abstract GameReadyModels getGameReadyModels();

  public static class GameReadyModels
  {
    public HashMap<TerrainType, ArrayList<UnitModel>> shoppingList = new HashMap<TerrainType, ArrayList<UnitModel>>();
    public ArrayList<UnitModel> unitModels = new ArrayList<UnitModel>();
  }
}
