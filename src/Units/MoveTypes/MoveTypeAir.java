package Units.MoveTypes;

import Terrain.TerrainType;
import Terrain.Environment.Weathers;

/** MoveTypeAir has a move cost of 1 for all terrain types. */
public class MoveTypeAir extends MoveType
{
  private static final long serialVersionUID = 1L;

  public MoveTypeAir()
  {
    // Air things aren't really affected by terrain. Default to 1 everywhere!
    moveCosts.get(Weathers.CLEAR).setAllMovementCosts(1);
    moveCosts.get(Weathers.RAIN).setAllMovementCosts(1);
    moveCosts.get(Weathers.SNOW).setAllMovementCosts(1);
    moveCosts.get(Weathers.SANDSTORM).setAllMovementCosts(1);
    moveCosts.get(Weathers.CHILL  ).setAllMovementCosts(1);
    moveCosts.get(Weathers.SLEET  ).setAllMovementCosts(1);
    moveCosts.get(Weathers.SMOKE  ).setAllMovementCosts(1);
    moveCosts.get(Weathers.SIROCCO).setAllMovementCosts(1);
    setMoveCost(TerrainType.TELETILE, 0);
    setMoveCost(TerrainType.PILLAR, 99);
    setMoveCost(TerrainType.METEOR, 99);
  }
}
