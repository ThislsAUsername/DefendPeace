package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class OlafFloatLight extends MoveTypeSea
{
  public OlafFloatLight()
  {
    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(TerrainType.REEF, 2);

    setMoveCost(Weathers.RAIN, TerrainType.SEA, 2);
  }
}
