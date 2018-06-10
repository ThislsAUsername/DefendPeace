package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.Types.Dunes;
import Terrain.Types.Mountain;

public class FootMech extends MoveTypeLand
{
  public FootMech()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();
    setMoveCost(Weathers.SNOW, Mountain.getInstance(), 2);
    setMoveCost(Weathers.SANDSTORM, Dunes.getInstance(), 2);
  }
}
