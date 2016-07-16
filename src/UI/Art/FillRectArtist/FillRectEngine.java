package UI.Art.FillRectArtist;

import UI.InputHandler.InputAction;
import UI.MainUIController;
import UI.MapView;
import UI.Art.SpriteArtist.SpriteMainUIView;
import Engine.GameInstance;
import Engine.GraphicsEngine;
import Engine.IView;

/**
 * This is more or less a dummy class that just returns the Sprite version
 * of the main UI, to demonstrate that the interchangeable-graphics mechanism works.
 */
public class FillRectEngine implements GraphicsEngine
{

  @Override
  public IView getMainUIView(MainUIController control)
  {
    return new SpriteMainUIView(control);
  }

  @Override
  public MapView createMapView(GameInstance game)
  {
    return new FillRectMapView(game);
  }

  @Override
  public boolean handleOptionsInput(InputAction action)
  {
    // FillRectEngine does not support any options.
    return true;
  }
}
