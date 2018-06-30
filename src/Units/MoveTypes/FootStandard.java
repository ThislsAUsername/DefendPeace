package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class FootStandard extends MoveTypeLand
{
  public FootStandard()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();

    // Make adjustments to the base-class values.
    setMoveCost(Weathers.CLEAR, TerrainType.MOUNTAIN, 2);

    setMoveCost(Weathers.RAIN, TerrainType.DUNES, 3);
    setMoveCost(Weathers.RAIN, TerrainType.MOUNTAIN, 3);

    setMoveCost(Weathers.SNOW, TerrainType.FOREST, 2);
    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);
    setMoveCost(Weathers.SNOW, TerrainType.MOUNTAIN, 4);

    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
    setMoveCost(Weathers.SANDSTORM, TerrainType.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.SHOAL, 2);
  }
}
