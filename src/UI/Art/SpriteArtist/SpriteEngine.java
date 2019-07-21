package UI.Art.SpriteArtist;

import java.awt.Dimension;

import UI.InfoController;
import UI.InputHandler.InputAction;
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
    return new MainUIView(control);
  }

  @Override
  public MapView createMapView(GameInstance game)
  {
    return new SpriteMapView(game);
  }

  @Override
  public boolean handleOptionsInput(InputAction action)
  {
    return SpriteOptions.handleOptionsInput(action);
  }

  @Override
  public Dimension getScreenDimensions()
  {
    return SpriteOptions.getScreenDimensions();
  }

  @Override
  public IView createInfoView(InfoController control)
  {
    return new InfoView(control);
  }
}
