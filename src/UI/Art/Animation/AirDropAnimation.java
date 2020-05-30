package UI.Art.Animation;

import java.awt.Graphics;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.UnitSpriteSet;
import Units.Unit;

public class AirDropAnimation implements GameAnimation
{
  private final int tileSize;

  Unit mover;

  XYCoord dropOrigin;
  XYCoord dropDestination;
  double xCurrent;
  double yCurrent;

  UnitSpriteSet unitSpriteSet;
  boolean someoneDies;

  double vel;
  double deltaV;

  boolean done;

  public AirDropAnimation(int tileSize, Unit unit, XYCoord start, XYCoord end, boolean unitDies, boolean obstacleUnitDies)
  {
    this.tileSize = tileSize;

    mover = unit;
    dropOrigin = start;
    dropDestination = end;
    xCurrent = dropDestination.xCoord;
    yCurrent = dropDestination.yCoord - 15; // So we drop in from the sky.
    unitSpriteSet = SpriteLibrary.getMapUnitSpriteSet(unit);
    someoneDies = unitDies | obstacleUnitDies;

    vel = 0.5;
    deltaV = 0.03;

    done = false;
  }

  @Override
  public boolean animate(Graphics g)
  {
    yCurrent += vel;
    vel += deltaV;
    
    if( yCurrent > dropDestination.yCoord )
    {
      yCurrent = dropDestination.yCoord;
      done = true;
    }

    int xDraw = (int)(xCurrent*tileSize);
    int yDraw = (int)(yCurrent*tileSize);
    boolean flipUnitFacing = dropOrigin.xCoord >= dropDestination.xCoord;
    unitSpriteSet.drawUnit(g, mover.CO, mover, 0, xDraw, yDraw, flipUnitFacing );

    // TODO: Draw explosions if/when needed.

    return done;
  }

  @Override
  public void cancel()
  {
    done = true;
  }
}
