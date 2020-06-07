package UI.Art.Animation;

import java.awt.Graphics;
import Engine.Path;
import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.UnitSpriteSet;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;
import Units.Unit;

public class SimpleMoveAnimation implements GameAnimation
{
  long startTime = 0;

  private final long maxTime = 250;
  private long endTime;

  private final int tileSize;
  private final Unit actor;
  UnitSpriteSet actorSpriteSet;
  private final Path path;
  private final double tilesPerMs;
  private static int timePerFrame = 125;

  public SimpleMoveAnimation(int tileSize, Unit actor, Path path)
  {
    this.tileSize = tileSize;
    this.actor = actor;
    actorSpriteSet = SpriteLibrary.getMapUnitSpriteSet(actor);
    this.path = path;
    startTime = System.currentTimeMillis();
    tilesPerMs = actor.model.movePower / (double) maxTime;
    endTime = (long) ((path.getPathLength() - 1) / tilesPerMs);
  }

  @Override
  public boolean animate(Graphics g)
  {
    long animTime = System.currentTimeMillis() - startTime;

    // Figure out which way the actor is going and where he is.
    AnimState actorAnimState = getAnimState();
    XYCoord actorDrawCoord = getActorDrawCoord(animTime, tileSize);
    boolean flip = actorAnimState == AnimState.MOVEWEST;

    // Choose the sprite index and draw it.
    int spriteIndex = (int)Math.floor(animTime / timePerFrame);
    actorSpriteSet.drawUnit(g, actorAnimState, spriteIndex, actorDrawCoord.xCoord, actorDrawCoord.yCoord, flip );

    return animTime > endTime;
  }

  @Override
  public void cancel()
  {
    endTime = 0;
  }

  public Unit getActor()
  {
    return actor;
  }

  public XYCoord getActorDrawCoord(long animTime, int tileSize)
  {
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
