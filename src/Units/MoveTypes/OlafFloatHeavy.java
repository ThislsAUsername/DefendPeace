package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class OlafFloatHeavy extends MoveTypeSea
{
  public OlafFloatHeavy()
  {
    // Heavier boats can't travel through shoals.
    setMoveCost(TerrainType.SHOAL, 99);

    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(TerrainType.REEF, 2);

    setMoveCost(Weathers.RAIN, TerrainType.SEA, 2);
  }
}
