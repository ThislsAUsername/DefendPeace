package Units.MoveTypes;

import Terrain.Environment;
import Units.Unit;
import Engine.FloodFillFunctor;
import Engine.XYCoord;
import Engine.FloodFillFunctor.BasicMoveFillFunctor;
import Terrain.GameMap;

/**
 * A variant MoveType that is barred from entering non-friendly ownable healing terrain
 */
public class MoveTypeFey extends MoveType
{
  private static final long serialVersionUID = 1L;

  public MoveTypeFey(MoveType other)
  {
    super(other);
  }

  @Override
  public MoveType clone()
  {
    return new MoveTypeFey(this);
  }

  @Override
  public FloodFillFunctor getUnitMoveFunctor(Unit mover, boolean includeOccupied, boolean canTravelThroughEnemies)
  {
    return new FeyMoveFillFunctor(mover, this, includeOccupied, canTravelThroughEnemies);
  }

  public static class FeyMoveFillFunctor extends BasicMoveFillFunctor
  {
    public FeyMoveFillFunctor(Unit mover, MoveType propulsion, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      super(mover, propulsion, includeOccupied, canTravelThroughEnemies);
    }

    @Override
    public int findMoveCost(XYCoord from, XYCoord to, GameMap map)
    {
      Environment endEnv = map.getEnvironment(to);
      // Fey units cannot enter enemy-controlled spaces.
      if (null != unit && unit.model.healableHabs.contains(endEnv.terrainType)
          && endEnv.terrainType.isCapturable()
          && unit.CO.isEnemy(map.getLocation(to).getOwner()))
        return IMPASSABLE;

      return propulsion.getMoveCost(endEnv);
    }
  } // ~FeyMoveFillFunctor
}
