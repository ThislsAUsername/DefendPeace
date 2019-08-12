package Units.MoveTypes;

import Terrain.TerrainType;
import Terrain.Environment.Weathers;

/** MoveTypeAir has a move cost of 1 for all water-based terrain types. */
public class MoveTypeSea extends MoveType
{
  private static final long serialVersionUID = 1L;

  public MoveTypeSea()
  {
    // The superclass sets all costs to 99; we just need to set the sea-based terrains to 1.
    moveCosts.get(Weathers.CLEAR).setAllSeaCosts(1);
    moveCosts.get(Weathers.RAIN).setAllSeaCosts(1);
    moveCosts.get(Weathers.SNOW).setAllSeaCosts(1);
    moveCosts.get(Weathers.SANDSTORM).setAllSeaCosts(1);
    setMoveCost(TerrainType.PILLAR, 99);
  }
}
