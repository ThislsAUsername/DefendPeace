package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FootMech extends MoveTypeLand
{
  public FootMech()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();
    setMoveCost(Weathers.SNOW, Terrains.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, Terrains.DUNES, 2);
  }
}
