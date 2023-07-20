package UI.Art.Animation;

import java.awt.Graphics;

import Engine.GamePath;
import Engine.XYCoord;
import Terrain.GameMap;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;
import Units.Unit;
import Units.UnitContext;
import Units.MoveTypes.MoveType;

public class MoveAnimation extends BaseUnitActionAnimation
{
  private final long maxTime = 250;

  private final GamePath path;
  private final double[] pathCosts; // in terms of milliseconds

  public MoveAnimation(int tileSize, GameMap map, Unit actor, GamePath path)
  {
    super(tileSize, actor, null);
    this.path = path;

    final UnitContext uc = new UnitContext(map, actor);
    MoveType mt = uc.calculateMoveType();
    final int tilesTraveled = path.getPathLength()-1;
    pathCosts = new double[tilesTraveled];
    double msPerCost = maxTime / (double) uc.calculateMovePower();
    double msPerTeleport = maxTime / (double) tilesTraveled; // Teletiles should add slightly less than 1 tile's movement to the final duration

    double msSpentTotal = 0;
    for( int i = 0; i < tilesTraveled; ++i)
    {
      XYCoord destCoord  = path.getWaypoint( i+1 ).GetCoordinates();
      int moveCost       = mt.getMoveCost(map.getEnvironment(destCoord));
      double msSpent     = msPerCost * moveCost;
      if( 0 == moveCost )
        msSpent          = msPerTeleport;
      msSpentTotal      += msSpent;
      pathCosts[i]       = msSpent;
    }
    duration = (long) Math.ceil(msSpentTotal);
  }

  @Override
  public boolean animate(Graphics g)
  {
    final long animTime = System.currentTimeMillis() - startTime;

    double tilesTraveled = 0;
    double timeLeft = animTime;
    int currentTile = 0;
    while (0 < timeLeft && currentTile < pathCosts.length)
    {
      double thisTime = pathCosts[currentTile];
      if( timeLeft >= thisTime )
      {
        timeLeft -= thisTime;
        ++currentTile;
        continue;
      }
      double fractionalMove = timeLeft / thisTime;
      tilesTraveled = currentTile + fractionalMove;
      break;
    }

    final int prevTileIndex = Math.min((int) Math.floor(tilesTraveled), path.getPathLength() - 1);
    final int nextTileIndex = Math.min((int) Math.ceil (tilesTraveled), path.getPathLength() - 1);

    final XYCoord coord1 = path.getWaypoint( prevTileIndex ).GetCoordinates();
    final XYCoord coord2 = path.getWaypoint( nextTileIndex ).GetCoordinates();

    final double diffX = coord2.xCoord - coord1.xCoord;
    final double diffY = coord2.yCoord - coord1.yCoord;
    final double tileDiff = tilesTraveled - prevTileIndex;
    final double currX = coord1.xCoord + tileDiff * diffX;
    final double currY = coord1.yCoord + tileDiff * diffY;

    // Figure out which way the actor is going and where he is.
    AnimState actorAnimState = getAnimState(coord1, coord2);

    // Choose the sprite index and draw it.
    drawUnit(g, actor, actorAnimState, currX, currY );

    return animTime > duration;
  }

  public AnimState getAnimState(final XYCoord coord1, final XYCoord coord2)
  {
    final int diffX = coord2.xCoord - coord1.xCoord;
    final int diffY = coord2.yCoord - coord1.yCoord;

    // Either x or y can be different. Check x offset.
    if( 0 != diffX )
    {
      return (0 < diffX) ? AnimState.MOVEEAST : AnimState.MOVEWEST;
    }
    else if( 0 != diffY ) // If not x, maybe y is different.
    {
      return (0 < diffY) ? AnimState.MOVESOUTH : AnimState.MOVENORTH;
    }

    return AnimState.IDLE;
  }
}
