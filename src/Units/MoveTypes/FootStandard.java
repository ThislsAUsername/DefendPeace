package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.Types.Dunes;
import Terrain.Types.Forest;
import Terrain.Types.Grass;
import Terrain.Types.Mountain;
import Terrain.Types.Shoal;

public class FootStandard extends MoveTypeLand
{
  public FootStandard()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();

    // Make adjustments to the base-class values.
    setMoveCost(Weathers.CLEAR, Mountain.getInstance(), 2);

    setMoveCost(Weathers.RAIN, Dunes.getInstance(), 3);
    setMoveCost(Weathers.RAIN, Mountain.getInstance(), 3);

    setMoveCost(Weathers.SNOW, Forest.getInstance(), 2);
    setMoveCost(Weathers.SNOW, Grass.getInstance(), 2);
    setMoveCost(Weathers.SNOW, Mountain.getInstance(), 4);

    setMoveCost(Weathers.SANDSTORM, Dunes.getInstance(), 3);
    setMoveCost(Weathers.SANDSTORM, Mountain.getInstance(), 2);
    setMoveCost(Weathers.SANDSTORM, Shoal.getInstance(), 2);
  }
}
