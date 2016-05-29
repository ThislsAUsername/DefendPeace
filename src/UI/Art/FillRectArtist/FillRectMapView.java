package UI.Art.FillRectArtist;

import java.awt.Dimension;
import java.awt.Graphics;

import Engine.GameInstance;
import UI.MapView;

public class FillRectMapView extends MapView
{
  private static final long serialVersionUID = 1L;

  private FillRectMapArtist mapArtist;
  private FillRectUnitArtist unitArtist;
  private FillRectMenuArtist menuArtist;

  private int baseTileSize = 16;
  private int mapViewWidth;
  private int mapViewHeight;

  public FillRectMapView(GameInstance game)
  {
    mapViewWidth = baseTileSize * getDrawScale() * 15;
    mapViewHeight = baseTileSize * getDrawScale() * 10;

    mapArtist = new FillRectMapArtist(game);
    unitArtist = new FillRectUnitArtist(game);
    menuArtist = new FillRectMenuArtist(game);

    mapArtist.setView(this);
    unitArtist.setView(this);
    menuArtist.setView(this);
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return new Dimension(mapViewWidth, mapViewHeight);
  }

  @Override
  public int getTileSize()
  {
    return baseTileSize * getDrawScale();
  }

  @Override
  public int getViewWidth()
  {
    return mapViewWidth;
  }

  @Override
  public int getViewHeight()
  {
    return mapViewHeight;
  }

  @Override
  public void render(Graphics g)
  {
    mapArtist.drawMap(g);
    mapArtist.drawHighlights(g);
    if( mapController.getContemplatedMove() != null )
    {
      mapArtist.drawMovePath(g, mapController.getContemplatedMove());
    }
    unitArtist.drawUnits(g);

    if( currentAnimation != null )
    {
      // Animate until it tells you it's done.
      if( currentAnimation.animate(g) )
      {
        currentAction = null;
        currentAnimation = null;
        mapController.animationEnded();
      }
    }
    else if( currentMenu == null )
    {
      mapArtist.drawCursor(g);
    }
    else
    {
      menuArtist.drawMenu(g);
    }
  }
}
