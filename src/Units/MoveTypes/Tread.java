package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class Tread extends MoveTypeLand
{
  private static final long serialVersionUID = 1L;

  public Tread()
  {
    // Treads are no good for mountains or rivers; disable movement for all weather types.
    setMoveCost(TerrainType.MOUNTAIN, 99);
    setMoveCost(TerrainType.RIVER, 99);

    setMoveCost(TerrainType.FOREST, 2);
    setMoveCost(TerrainType.DUNES, 2);

    setMoveCost(Weathers.RAIN, TerrainType.GRASS, 2);
    setMoveCost(Weathers.RAIN, TerrainType.FOREST, 3);

    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);
  }
}
