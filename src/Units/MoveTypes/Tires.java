package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class Tires extends MoveTypeLand
{
  public Tires()
  {
    // Wheels are no good for mountains; disable movement for all weather types.
    setMoveCost(Terrains.MOUNTAIN, 99);

    setMoveCost(Weathers.CLEAR, Terrains.GRASS, 2);
    setMoveCost(Weathers.CLEAR, Terrains.FOREST, 3);
    setMoveCost(Weathers.CLEAR, Terrains.DUNES, 3);

    setMoveCost(Weathers.RAIN, Terrains.GRASS, 3);
    setMoveCost(Weathers.RAIN, Terrains.FOREST, 4);
    setMoveCost(Weathers.RAIN, Terrains.DUNES, 2);

    setMoveCost(Weathers.SNOW, Terrains.GRASS, 3);
    setMoveCost(Weathers.SNOW, Terrains.FOREST, 3);
    setMoveCost(Weathers.SNOW, Terrains.DUNES, 2);

    setMoveCost(Weathers.SANDSTORM, Terrains.GRASS, 2);
    setMoveCost(Weathers.SANDSTORM, Terrains.FOREST, 3);
    setMoveCost(Weathers.SANDSTORM, Terrains.DUNES, 4);
  }
}
