package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FootMech extends MoveTypeLand
{
  public FootMech()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();
    moveCosts.get(Weathers.SNOW).put(Terrains.MOUNTAIN, 2);
    moveCosts.get(Weathers.SANDSTORM).put(Terrains.DUNES, 2);
  }
}
