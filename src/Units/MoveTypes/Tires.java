package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class Tires extends MoveTypeLand
{
  private static final long serialVersionUID = 1L;

  public Tires()
  {
    // Wheels are no good for mountains or rivers; disable movement for all weather types.
    setMoveCost(TerrainType.MOUNTAIN, 99);
    setMoveCost(TerrainType.RIVER, 99);

    setMoveCost(Weathers.CLEAR, TerrainType.GRASS, 2);
    setMoveCost(Weathers.CLEAR, TerrainType.FOREST, 3);
    setMoveCost(Weathers.CLEAR, TerrainType.DUNES, 3);

    setMoveCost(Weathers.RAIN, TerrainType.GRASS, 3);
    setMoveCost(Weathers.RAIN, TerrainType.FOREST, 4);
    setMoveCost(Weathers.RAIN, TerrainType.DUNES, 2);

    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 3);
    setMoveCost(Weathers.SNOW, TerrainType.FOREST, 3);
    setMoveCost(Weathers.SNOW, TerrainType.DUNES, 2);

    setMoveCost(Weathers.SANDSTORM, TerrainType.GRASS, 2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.FOREST, 3);
    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 4);
  }
}
