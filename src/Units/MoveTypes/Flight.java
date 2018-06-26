package Units.MoveTypes;

import Terrain.Environment;
import Terrain.Environment.Weathers;

public class Flight extends MoveTypeAir
{
  public Flight()
  {
    // Initialize the default superclass movement costs, then override specific values.
    super();
    moveCosts.get(Weathers.SNOW).setAllMovementCosts(2);
    setMoveCost(Weathers.SANDSTORM, Environment.DUNES, 3);
  }
}
