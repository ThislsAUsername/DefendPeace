package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class PerfectFootMech extends MoveTypeLand
{
  public PerfectFootMech()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();
    setMoveCost(Weathers.SNOW, TerrainType.MOUNTAIN, 2);
  }
}
