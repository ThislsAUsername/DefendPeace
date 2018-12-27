package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class PerfectFloatHeavy extends MoveTypeSea
{
  public PerfectFloatHeavy()
  {
    // Heavier boats can't travel through shoals.
    setMoveCost(TerrainType.SHOAL, 99);

    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(Weathers.SNOW, TerrainType.REEF, 2);

    setMoveCost(Weathers.SNOW, TerrainType.SEA, 2);
  }
}
