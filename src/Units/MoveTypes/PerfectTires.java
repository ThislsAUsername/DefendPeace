package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class PerfectTires extends MoveTypeLand
{
  public PerfectTires()
  {
    // Wheels are no good for mountains or rivers; disable movement for all weather types.
    setMoveCost(TerrainType.MOUNTAIN, 99);
    setMoveCost(TerrainType.RIVER, 99);

    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 3);
    setMoveCost(Weathers.SNOW, TerrainType.FOREST, 3);
    setMoveCost(Weathers.SNOW, TerrainType.DUNES, 2);

    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 4);
  }
}
