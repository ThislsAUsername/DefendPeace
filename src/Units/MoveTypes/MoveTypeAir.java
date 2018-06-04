package Units.MoveTypes;

import Terrain.Environment.Weathers;

/** MoveTypeAir has a move cost of 1 for all terrain types. */
public class MoveTypeAir extends MoveType
{
  public MoveTypeAir()
  {
    // Initialize the superclass. This creates the weather-to-TerrainCost map.
    super();

    // Air things aren't really affected by terrain default to 1 everywhere!
    moveCosts.get(Weathers.CLEAR).setAllMovementCosts(1);
    moveCosts.get(Weathers.RAIN).setAllMovementCosts(1);
    moveCosts.get(Weathers.SNOW).setAllMovementCosts(1);
    moveCosts.get(Weathers.SANDSTORM).setAllMovementCosts(1);
  }
}
