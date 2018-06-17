package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FootStandard extends MoveTypeLand
{
  public FootStandard()
  {
    // Make adjustments to the base-class values.
    setMoveCost(Weathers.CLEAR, Terrains.MOUNTAIN, 2);

    setMoveCost(Weathers.RAIN, Terrains.DUNES, 3);
    setMoveCost(Weathers.RAIN, Terrains.MOUNTAIN, 3);

    setMoveCost(Weathers.SNOW, Terrains.FOREST, 2);
    setMoveCost(Weathers.SNOW, Terrains.GRASS, 2);
    setMoveCost(Weathers.SNOW, Terrains.MOUNTAIN, 4);

    setMoveCost(Weathers.SANDSTORM, Terrains.DUNES, 3);
    setMoveCost(Weathers.SANDSTORM, Terrains.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, Terrains.SHOAL, 2);
  }
}
