package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.Types.Dunes;
import Terrain.Types.Forest;
import Terrain.Types.Grass;
import Terrain.Types.Mountain;

public class Tread extends MoveTypeLand
{
  public Tread()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();

    // Treads are no good for mountains; disable movement for all weather types.
    setMoveCost(Mountain.getInstance(), 99);

    setMoveCost(Weathers.CLEAR, Forest.getInstance(), 2);
    setMoveCost(Weathers.CLEAR, Dunes.getInstance(), 2);

    setMoveCost(Weathers.RAIN, Grass.getInstance(), 2);
    setMoveCost(Weathers.RAIN, Forest.getInstance(), 3);

    setMoveCost(Weathers.SNOW, Grass.getInstance(), 2);
    setMoveCost(Weathers.SNOW, Forest.getInstance(), 2);
    setMoveCost(Weathers.SNOW, Dunes.getInstance(), 2);

    setMoveCost(Weathers.SANDSTORM, Forest.getInstance(), 2);
    setMoveCost(Weathers.SANDSTORM, Dunes.getInstance(), 3);
  }
}
