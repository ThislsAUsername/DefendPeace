package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class FloatLight extends MoveTypeSea
{
  private static final long serialVersionUID = 1L;

  public FloatLight()
  {
    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(TerrainType.REEF, 2);

    setMoveCost(Weathers.SNOW, TerrainType.SEA, 2);
  }
}
