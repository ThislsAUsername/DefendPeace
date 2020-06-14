package UI.Art.Animation;

import java.awt.Graphics;

import Engine.Path;
import Engine.XYCoord;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;
import Units.Unit;

public class MoveAnimation extends BaseUnitActionAnimation
{
  private final long maxTime = 250;

  private final Path path;
  private final double tilesPerMs;

  public MoveAnimation(int tileSize, Unit actor, Path path)
  {
    super(tileSize, actor, null);
    this.path = path;
    tilesPerMs = actor.model.movePower / (double) maxTime;
    duration = (long) ((path.getPathLength() - 1) / tilesPerMs);
  }

  @Override
  public boolean animate(Graphics g)
  {
    long animTime = System.currentTimeMillis() - startTime;

    final double tilesTraveled = animTime * tilesPerMs;
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
