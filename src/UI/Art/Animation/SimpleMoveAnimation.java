package UI.Art.Animation;

import java.awt.Graphics;
import Engine.Path;
import Engine.XYCoord;
import Units.Unit;

public class SimpleMoveAnimation implements GameAnimation
{
  long startTime = 0;

  private final long maxTime = 250;
  private long endTime;

  private final Unit actor;
  private final Path path;
  private final double tilesPerMs;

  public SimpleMoveAnimation(Unit actor, Path path)
  {
    this.actor = actor;
    this.path = path;
    startTime = System.currentTimeMillis();
    tilesPerMs = actor.model.movePower / (double) maxTime;
    endTime = (long) ((path.getPathLength() - 1) / tilesPerMs);
  }

  @Override
  public boolean animate(Graphics g)
  {
    long animTime = System.currentTimeMillis() - startTime;
    return animTime > endTime;
  }

  @Override
  public void cancel()
  {
    endTime = 0;
  }

  @Override
  public Unit getActor()
  {
    return actor;
  }

  @Override
  public XYCoord getActorDrawCoord(int tileSize)
  {
    final long animTime = System.currentTimeMillis() - startTime;
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

    return new XYCoord( (int) (currX * tileSize), (int) (currY * tileSize) );
  }

  @Override
  public AnimState getAnimState()
  {
    final long animTime = System.currentTimeMillis() - startTime;
    final double tilesTraveled = animTime * tilesPerMs;
    final int prevTileIndex = Math.min((int) Math.floor(tilesTraveled), path.getPathLength() - 1);
    final int nextTileIndex = Math.min((int) Math.ceil (tilesTraveled), path.getPathLength() - 1);

    final XYCoord coord1 = path.getWaypoint( prevTileIndex ).GetCoordinates();
    final XYCoord coord2 = path.getWaypoint( nextTileIndex ).GetCoordinates();

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
