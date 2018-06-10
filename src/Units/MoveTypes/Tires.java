package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.Types.Dunes;
import Terrain.Types.Forest;
import Terrain.Types.Grass;
import Terrain.Types.Mountain;

public class Tires extends MoveTypeLand
{
  public Tires()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();

    // Wheels are no good for mountains; disable movement for all weather types.
    setMoveCost(Mountain.getInstance(), 99);

    setMoveCost(Weathers.CLEAR, Grass.getInstance(), 2);
    setMoveCost(Weathers.CLEAR, Forest.getInstance(), 3);
    setMoveCost(Weathers.CLEAR, Dunes.getInstance(), 3);

    setMoveCost(Weathers.RAIN, Grass.getInstance(), 3);
    setMoveCost(Weathers.RAIN, Forest.getInstance(), 4);
    setMoveCost(Weathers.RAIN, Dunes.getInstance(), 2);

    setMoveCost(Weathers.SNOW, Grass.getInstance(), 3);
    setMoveCost(Weathers.SNOW, Forest.getInstance(), 3);
    setMoveCost(Weathers.SNOW, Dunes.getInstance(), 2);

    setMoveCost(Weathers.SANDSTORM, Grass.getInstance(), 2);
    setMoveCost(Weathers.SANDSTORM, Forest.getInstance(), 3);
    setMoveCost(Weathers.SANDSTORM, Dunes.getInstance(), 4);
  }
}
