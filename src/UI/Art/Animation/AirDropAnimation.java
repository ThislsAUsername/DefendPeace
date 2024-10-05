package UI.Art.Animation;

import java.awt.Color;
import java.awt.Graphics;

import Engine.XYCoord;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;
import Units.Unit;

public class AirDropAnimation extends BaseUnitActionAnimation
{
  XYCoord dropOrigin;
  XYCoord dropDestination;
  int dropHeight = 15;

  int phase = 0;

  // Phase 0 variables.
  double xDrop;
  double yCurrent;
  double vel;
  double deltaV;

  // Effects variables.
  Color fxColor = new Color(255, 255, 255, 190);
  double xLeft;
  double xRight;

  boolean done;

  public AirDropAnimation(int tileSize, Unit unit, XYCoord start, XYCoord end)
  {
    super(tileSize, unit, end);

    dropOrigin = start;
    dropDestination = end;

    // Phase 0
    xDrop = dropDestination.x;
    yCurrent = dropDestination.y - dropHeight; // So we drop in from the sky.
    vel = 0.5;
    deltaV = 0.03;

    // Phase 1
    xLeft = xDrop-1.5;
    xRight = xDrop+2.5;

    done = false;
  }

  @Override
  public boolean animate(Graphics g)
  {
    if(0==phase) // Guide lights
    {
      g.setColor(fxColor);
      g.fillRect((int)(xLeft*tileSize), (int)(yCurrent*tileSize), 2, dropHeight*tileSize+tileSize);
      g.fillRect((int)(xRight*tileSize), (int)(yCurrent*tileSize), 2, dropHeight*tileSize+tileSize);
      xLeft += 0.125;
      xRight -= 0.125;

      if( xLeft >= xRight )
      {
        phase++;
      }
    }
    else if(1==phase) // Drop
    {
      yCurrent += vel;
      vel += deltaV;

      if( yCurrent > dropDestination.y )
      {
        yCurrent = dropDestination.y;
        phase++;

        // Set up the next phase.
        xLeft = xDrop+0.25;
        xRight = xDrop+0.75;
      }

      drawUnit(g, actor, AnimState.IDLE, xDrop, yCurrent);
    }
    else if(2==phase)
    {
      // Draw dust clouds from landing.
      final int diam_px = 10; // Draw-space pixels.
      final double map_y = dropDestination.y+0.75;
      int xlDraw = (int)(xLeft*tileSize)-diam_px/2;
      int xrDraw = (int)(xRight*tileSize)-diam_px/2;
      int yDraw = (int)(map_y*tileSize)-diam_px/2;
      g.setColor(fxColor);

      drawUnit(g, actor, AnimState.IDLE, dropDestination.x, dropDestination.y);
      g.fillOval(xlDraw, yDraw, diam_px, diam_px);
      g.fillOval(xrDraw, yDraw, diam_px, diam_px);

      xLeft -= 0.07;
      xRight += 0.07;
      if( xLeft < xDrop-.3 ) done = true;
    }

    return done;
  }

  @Override
  public void cancel()
  {
    done = true;
  }
}
