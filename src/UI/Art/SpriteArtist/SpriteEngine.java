package UI.Art.SpriteArtist;

import UI.MainUIController;
import UI.MapView;
import Engine.GameInstance;
import Engine.GraphicsEngine;
import Engine.IView;

public class SpriteEngine implements GraphicsEngine
{
  @Override
  public IView getMainUIView(MainUIController control)
  {
    return new SpriteMainUIView(control);
  }

  @Override
  public MapView createMapView(GameInstance game)
  {
    return new SpriteMapView(game);
  }
}
