package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FootStandard extends MoveTypeLand
{
  public FootStandard()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();

    // Make adjustments to the base-class values.
    moveCosts.get(Weathers.CLEAR).put(Terrains.MOUNTAIN, 2);

    moveCosts.get(Weathers.RAIN).put(Terrains.DUNES, 3);
    moveCosts.get(Weathers.RAIN).put(Terrains.MOUNTAIN, 3);

    moveCosts.get(Weathers.SNOW).put(Terrains.FOREST, 2);
    moveCosts.get(Weathers.SNOW).put(Terrains.GRASS, 2);
    moveCosts.get(Weathers.SNOW).put(Terrains.MOUNTAIN, 4);

    moveCosts.get(Weathers.SANDSTORM).put(Terrains.DUNES, 3);
    moveCosts.get(Weathers.SANDSTORM).put(Terrains.MOUNTAIN, 2);
    moveCosts.get(Weathers.SANDSTORM).put(Terrains.SHOAL, 2);
  }
}
