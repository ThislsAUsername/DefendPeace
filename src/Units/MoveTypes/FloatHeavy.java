package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FloatHeavy extends MoveTypeSea
{
  public FloatHeavy()
  {
    // Heavier boats can't travel through shoals.
    setMoveCost(Terrains.SHOAL, 99);

    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(Terrains.REEF, 2);

    setMoveCost(Weathers.SNOW, Terrains.SEA, 2);
  }
}
