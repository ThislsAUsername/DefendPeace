package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.Types.Reef;
import Terrain.Types.Sea;
import Terrain.Types.Shoal;

public class FloatHeavy extends MoveTypeSea
{
  public FloatHeavy()
  {
    // Initialize the default sea-based movement costs, then override specific values.
    super();

    // Heavier boats can't travel through shoals.
    setMoveCost(Shoal.getInstance(), 99);

    // Reefs are just a bit more difficult to move through in general.
    setMoveCost(Reef.getInstance(), 2);

    setMoveCost(Weathers.SNOW, Sea.getInstance(), 2);
  }
}
