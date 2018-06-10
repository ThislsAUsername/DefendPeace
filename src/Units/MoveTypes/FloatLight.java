package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.Types.Reef;
import Terrain.Types.Sea;

public class FloatLight extends MoveTypeSea
{
  public FloatLight()
  {
    // Initialize the default sea-based movement costs, then override specific values.
    super();

    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(Reef.getInstance(), 2);

    setMoveCost(Weathers.SNOW, Sea.getInstance(), 2);
  }
}
