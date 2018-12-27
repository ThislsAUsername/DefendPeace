package CommandingOfficers.Modifiers;

import java.util.Map;

import CommandingOfficers.Commander;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.UnitModel;

public class PerfectMoveModifier extends UnitRemodelModifier implements COModifier
{

  public PerfectMoveModifier()
  {
    super();
  }

  /** Add a new unit transform assignment. */
  public Map<UnitModel, UnitModel> init(Commander commander)
  {
    modelSwaps.clear();
    for( UnitModel um : commander.unitModels )
    {
      UnitModel newModel = UnitModel.clone(um);

      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        if( newModel.propulsion.getMoveCost(Weathers.CLEAR, terrain) < 99 )
        {
          newModel.propulsion.setMoveCost(Weathers.CLEAR, terrain, 1);
          newModel.propulsion.setMoveCost(Weathers.RAIN, terrain, 1);
        }
      }

      modelSwaps.put(um, newModel);
    }
    return modelSwaps;
  }
}
