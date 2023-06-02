package Units.MoveTypes;

import Terrain.Environment;
import Units.Unit;
import Engine.XYCoord;
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
  public int getTransitionCost(GameMap map, XYCoord from, XYCoord to,
                               Unit mover, boolean canTravelThroughEnemies)
  {
    // Mandatory sanity check
    if( !map.isLocationValid(to) )
      return IMPASSABLE;

    Environment endEnv = map.getEnvironment(to);
    // Fey units cannot enter enemy-controlled spaces.
    if( null != mover && mover.model.healableHabs.contains(endEnv.terrainType)
        && endEnv.terrainType.isCapturable()
        && mover.CO.isEnemy(map.getLocation(to).getOwner()) )
      return IMPASSABLE;

    return super.getTransitionCost(map, from, to, mover, canTravelThroughEnemies);
  }

}
