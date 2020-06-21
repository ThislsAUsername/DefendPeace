package UI.Art.Animation;

import java.awt.Color;
import java.awt.Graphics;

import Engine.XYCoord;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.UnitSpriteSet;
import Units.Unit;

public class AirDropAnimation implements GameAnimation
{
  private final int tileSize;

  Unit mover;
  UnitSpriteSet unitSpriteSet;
  boolean someoneDies;
  XYCoord dropOrigin;
  XYCoord dropDestination;
  int dropHeight = 15;

  int phase = 1;

  // Phase 0 variables.
  double xCurrent;
  double yCurrent;
  double vel;
  double deltaV;

  // Phase 1 variables.
  Color light = new Color(255, 255, 255, 190);
  double xLeftLight;
  double xRightLight;

  boolean done;

  public AirDropAnimation(int tileSize, Unit unit, XYCoord start, XYCoord end, boolean unitDies, boolean obstacleUnitDies)
  {
    this.tileSize = tileSize;

    mover = unit;
    unitSpriteSet = SpriteLibrary.getMapUnitSpriteSet(unit);
    someoneDies = unitDies | obstacleUnitDies;
    dropOrigin = start;
    dropDestination = end;

    // Phase 0
    xCurrent = dropDestination.xCoord;
    yCurrent = dropDestination.yCoord - dropHeight; // So we drop in from the sky.
    vel = 0.5;
    deltaV = 0.03;

    // Phase 1
    xLeftLight = xCurrent-1.5;
    xRightLight = xCurrent+2.5;

    done = false;
  }

  @Override
  public boolean animate(Graphics g)
  {
    if(0==phase) // Guide lights
    {
      g.setColor(light);
      g.fillRect((int)(xLeftLight*tileSize), (int)(yCurrent*tileSize), 2, dropHeight*tileSize+tileSize);
      g.fillRect((int)(xRightLight*tileSize), (int)(yCurrent*tileSize), 2, dropHeight*tileSize+tileSize);
      xLeftLight += 0.125;
      xRightLight -= 0.125;

      if( xLeftLight >= xRightLight )
      {
        phase++;
      }
    }
    else if(1==phase) // Drop
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
    }
    // TODO: Draw explosions if/when needed.

    return done;
  }

  @Override
  public void cancel()
  {
    done = true;
  }
}
