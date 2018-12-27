package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class PerfectFlight extends MoveTypeAir
{
  public PerfectFlight()
  {
    // Override specific move cost values.
    moveCosts.get(Weathers.SNOW).setAllMovementCosts(2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
  }
}
