package UI.Art.Animation;

import java.awt.Graphics;
import Engine.Path;
import Engine.XYCoord;
import Units.Unit;

public class SimpleMoveAnimation implements GameAnimation
{
  long startTime = 0;

  private long endTime = 600;

  private final Unit actor;
  private final Path path;

  public SimpleMoveAnimation(Unit actor, Path path)
  {
    this.actor = actor;
    this.path = path;
    path.start();
    startTime = System.currentTimeMillis();
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
    XYCoord coord = path.getPosition();

    return new XYCoord(coord.xCoord * tileSize, coord.yCoord * tileSize);
  }
}
