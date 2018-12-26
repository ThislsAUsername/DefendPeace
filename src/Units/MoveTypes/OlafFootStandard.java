package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class OlafFootStandard extends MoveTypeLand
{
  public OlafFootStandard()
  {
    // Make adjustments to the base-class values.
    setMoveCost(Weathers.CLEAR, TerrainType.MOUNTAIN, 2);
    setMoveCost(Weathers.CLEAR, TerrainType.RIVER, 2);

    setMoveCost(Weathers.SNOW, TerrainType.MOUNTAIN, 2);
    setMoveCost(Weathers.SNOW, TerrainType.RIVER, 2);

    setMoveCost(Weathers.RAIN, TerrainType.FOREST, 2);
    setMoveCost(Weathers.RAIN, TerrainType.GRASS, 2);
    setMoveCost(Weathers.RAIN, TerrainType.MOUNTAIN, 4);
    setMoveCost(Weathers.RAIN, TerrainType.RIVER, 3);

    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
    setMoveCost(Weathers.SANDSTORM, TerrainType.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.SHOAL, 2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.RIVER, 2);
  }
}
