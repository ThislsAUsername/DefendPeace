package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class OlafFlight extends MoveTypeAir
{
  public OlafFlight()
  {
    // Override specific move cost values.
    moveCosts.get(Weathers.RAIN).setAllMovementCosts(2);
    setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
  }
}
