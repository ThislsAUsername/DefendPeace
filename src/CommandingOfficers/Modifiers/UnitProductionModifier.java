package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import CommandingOfficers.Commander;
import Terrain.TerrainType;
import Units.UnitModel;

/** Allows us to add units to a CO's production capabilities on the fly. */
public class UnitProductionModifier implements COModifier
{
  private static final long serialVersionUID = 1L;
  private Map<TerrainType, Set<UnitModel>> productionMods = null; 

  public UnitProductionModifier(TerrainType terrain, UnitModel model)
  {
    productionMods = new HashMap<TerrainType, Set<UnitModel>>();
    addProductionPair(terrain, model);
  }

  public void addProductionPair(TerrainType terrain, UnitModel model)
  {
    // If we don't have a mapping to this terrain type yet, add it.
    if( !productionMods.containsKey(terrain))
    {
      productionMods.put(terrain, new HashSet<UnitModel>());
    }

    // Assign the UnitModel to that terrain type.
    productionMods.get(terrain).add(model);
  }

  @Override
  public void applyChanges(Commander commander)
  {
    for(TerrainType tt : productionMods.keySet() )
    {
      // If the commander doesn't support production from this type, add that property type.
      if( !commander.unitProductionByTerrain.containsKey(tt) )
      {
        commander.unitProductionByTerrain.put(tt, new ArrayList<UnitModel>());
      }

      // Now make sure they have an entry for each unit we want them to be able to build.
      // Make a copy so we can modify the original set if needed.
      Set<UnitModel> setCopy = new HashSet<UnitModel>(productionMods.get(tt));
      for( UnitModel um : setCopy )
      {
        // If the commander already can build that unit type, we will remove it from our map instead,
        // so we don't accidentally prevent them from building something they should be able to.
        if( commander.unitProductionByTerrain.get(tt).contains(um) )
        {
          // Since the Commander can already build um, we don't need to add it, and we don't need to remember to remove it.
          productionMods.get(tt).remove(um);
        }
        else
        {
          // Add this UnitModel to the commander's shopping list.
          commander.unitProductionByTerrain.get(tt).add(um);
        }
      }
    }
  }

  @Override
  public void revertChanges(Commander commander)
  {
    for( TerrainType tt : productionMods.keySet() )
    {
      for( UnitModel um : productionMods.get(tt) )
      {
        commander.unitProductionByTerrain.get(tt).remove(um);
      }
    }
  }
}
