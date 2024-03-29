package UI.Art.SpriteArtist;

import java.awt.Dimension;

import UI.DamageChartController;
import UI.InfoController;
import UI.InputHandler.InputAction;
import UI.MainUIController;
import UI.MapView;
import Engine.GameInstance;
import Engine.GraphicsEngine;
import Engine.IView;

public class SpriteEngine implements GraphicsEngine
{
  static
  {
    SpriteOptions.initialize();
  }

  @Override
  public IView getMainUIView(MainUIController control)
  {
    PlayerSetupColorFactionArtist.startSpritePreloader("Infantry");
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

  @Override
  public IView createDamageChartView(DamageChartController control)
  {
    return new DamageChartView(control);
  }
}
