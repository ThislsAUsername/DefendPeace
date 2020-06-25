package UI.Art.SpriteArtist;

import java.awt.Graphics;

import Engine.GameInstance;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;
import Units.Unit;

public class UnitArtist
{
  private GameInstance myGame;
  private SpriteMapView myView;

  public UnitArtist(GameInstance game, SpriteMapView view)
  {
    myGame = game;
    myView = view;
  }

  /**
   * Draws any applicable unit icons for the unit, at the specified real location.
   * "Real" means that the specified x and y are that of the game's
   * underlying data model, not of the draw-space.
   */
  public void drawUnitIcons(Graphics g, Unit unit, int x, int y, int animIndex)
  {
    // Convert "real" location into a draw-space location, then draw icons.
    int drawX = (int) (myView.getTileSize() * x);
    int drawY = (int) (myView.getTileSize() * y);

    SpriteLibrary.getMapUnitSpriteSet(unit).drawUnitIcons(g, myGame.commanders, unit, animIndex, drawX, drawY);
  }

  /**
   * Allows drawing of a single unit, at a specified real location.
   * "Real" means that the specified x and y are that of the game's
   * underlying data model, not of the draw-space.
   */
  public void drawUnit(Graphics g, Unit unit, int x, int y, int animIndex)
  {
    int drawX = (int) (myView.getTileSize() * x);
    int drawY = (int) (myView.getTileSize() * y);
    boolean tired = unit.isStunned || (unit.isTurnOver && unit.CO == myGame.activeCO);
    AnimState state = (tired) ? AnimState.TIRED : AnimState.IDLE;

    // Draw the unit at the specified location.
    SpriteLibrary.getMapUnitSpriteSet(unit).drawUnit(g, unit, state,
    animIndex, drawX, drawY);
  }
}
