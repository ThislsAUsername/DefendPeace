package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FloatLight extends MoveTypeSea
{
  public FloatLight()
  {
    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(Terrains.REEF, 2);

    setMoveCost(Weathers.SNOW, Terrains.SEA, 2);
  }
}
