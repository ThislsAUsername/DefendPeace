package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class FootStandard extends MoveTypeLand
{
  private static final long serialVersionUID = 1L;

  public FootStandard()
  {
    // Make adjustments to the base-class values.
    setMoveCost(TerrainType.MOUNTAIN, 2);
    setMoveCost(TerrainType.RIVER, 2);

    setMoveCost(Weathers.SNOW, TerrainType.FOREST, 2);
    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);
    setMoveCost(Weathers.SNOW, TerrainType.MOUNTAIN, 4);

    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
  }
}
