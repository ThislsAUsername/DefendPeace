package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class Flight extends MoveTypeAir
{
  private static final long serialVersionUID = 1L;

  public Flight()
  {
    // Override specific move cost values.
    moveCosts.get(Weathers.SNOW).setAllMovementCosts(2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
  }
}
