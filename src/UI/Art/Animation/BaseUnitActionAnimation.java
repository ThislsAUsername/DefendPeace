package UI.Art.Animation;

import java.awt.Graphics;
import java.util.ArrayList;

import Engine.XYCoord;
import UI.Art.SpriteArtist.*;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;
import Units.Unit;

public class BaseUnitActionAnimation extends GameAnimation
{
  protected long startTime = 0;
  protected long duration = -42;

  protected int tileSize;

  protected Unit actor;
  protected XYCoord actorCoord;

  public BaseUnitActionAnimation(int tileSize, Unit actor, XYCoord actorCoord)
  {
    super(true);
    update(tileSize, actor, actorCoord);
  }
  public BaseUnitActionAnimation update(int tileSize, Unit actor, XYCoord actorCoord)
  {
    startTime = System.currentTimeMillis();
    this.tileSize = tileSize;
    this.actor = actor;
    this.actorCoord = actorCoord;
    return this;
  }

  @Override
  public boolean animate(Graphics g)
  {
    long animTime = System.currentTimeMillis() - startTime;

    if( null != actor && null != actorCoord )
    {
      AnimState state = AnimState.MOVEEAST; // Draw the unit running in place
      if( SpriteMapView.shouldFlip(actor) )
        state = AnimState.MOVEWEST;
      drawUnit(g, actor, state, actorCoord.xCoord, actorCoord.yCoord);
    }

    return animTime > duration;
  }

  /**
   * Draws the unit at the given domain-level map coordinates using the fast animIndex
   */
  public void drawUnit(Graphics g, Unit actor, AnimState state, double realX, double realY)
  {
    int spriteIndex = SpriteMapView.getFastAnimIndex();
    drawUnit(g, actor, state, spriteIndex, realX, realY );
  }

  /**
   * Draws the unit at the given domain-level map coordinates
   */
  public void drawUnit(Graphics g, Unit actor, AnimState state, int spriteIndex, double realX, double realY)
  {
    if( null != actor )
      SpriteLibrary.getMapUnitSpriteSet(actor).drawUnit(g, actor, state, spriteIndex, (int) (realX * tileSize), (int) (realY * tileSize));
  }

  @Override
  public void cancel()
  {
    duration = 0;
  }

  @Override
  public ArrayList<Unit> getActors()
  {
    ArrayList<Unit> out = new ArrayList<Unit>();
    out.add(actor);
    return out;
  }
}
