package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class PerfectFootStandard extends MoveTypeLand
{
  public PerfectFootStandard()
  {

    setMoveCost(Weathers.SNOW, TerrainType.FOREST, 2);
    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);
    setMoveCost(Weathers.SNOW, TerrainType.MOUNTAIN, 4);
    setMoveCost(Weathers.SNOW, TerrainType.RIVER, 3);

    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
  }
}
