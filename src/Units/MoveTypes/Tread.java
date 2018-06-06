package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class Tread extends MoveTypeLand
{
  public Tread()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();

    // Treads are no good for mountains; disable movement for all weather types.
    setMoveCost(Terrains.MOUNTAIN, 99);

    setMoveCost(Weathers.CLEAR, Terrains.FOREST, 2);
    setMoveCost(Weathers.CLEAR, Terrains.DUNES, 2);

    setMoveCost(Weathers.RAIN, Terrains.GRASS, 2);
    setMoveCost(Weathers.RAIN, Terrains.FOREST, 3);

    setMoveCost(Weathers.SNOW, Terrains.GRASS, 2);
    setMoveCost(Weathers.SNOW, Terrains.FOREST, 2);
    setMoveCost(Weathers.SNOW, Terrains.DUNES, 2);

    setMoveCost(Weathers.SANDSTORM, Terrains.FOREST, 2);
    setMoveCost(Weathers.SANDSTORM, Terrains.DUNES, 3);
  }
}
