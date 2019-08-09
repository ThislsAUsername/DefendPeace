package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class FootMech extends MoveTypeLand
{
  private static final long serialVersionUID = 1L;

  public FootMech()
  {
    // Initialize the default land-based movement costs, then override specific values.
    super();
    setMoveCost(Weathers.SNOW, TerrainType.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 2);
  }
}
