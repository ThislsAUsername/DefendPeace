package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class PerfectFloatLight extends MoveTypeSea
{
  public PerfectFloatLight()
  {
    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(Weathers.SNOW, TerrainType.REEF, 2);

    setMoveCost(Weathers.SNOW, TerrainType.SEA, 2);
  }
}
