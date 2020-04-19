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
  public FloodFillFunctor getUnitMoveFunctor(Unit mover)
  {
    return new FeyMoveFillFunctor(mover);
  }

  public static class FeyMoveFillFunctor extends BasicMoveFillFunctor
  {
    public FeyMoveFillFunctor(Unit mover)
    {
      super(mover);
    }

    @Override
    public int findMoveCost(XYCoord from, XYCoord to, GameMap map)
    {
      Environment endEnv = map.getEnvironment(to);
      if (unit.model.healableHabs.contains(endEnv.terrainType)
          && endEnv.terrainType.isCapturable()
          && unit.CO.isEnemy(map.getLocation(to).getOwner()))
        return IMPASSABLE;

      return unit.model.propulsion.getMoveCost(endEnv);
    }
  } // ~FeyMoveFillFunctor
}
