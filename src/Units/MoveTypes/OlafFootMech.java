package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class OlafFootMech extends MoveTypeLand
{
  public OlafFootMech()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();
    setMoveCost(Weathers.RAIN, TerrainType.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 2);
  }
}
