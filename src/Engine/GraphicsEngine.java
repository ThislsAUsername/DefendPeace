package Engine;

import java.awt.Dimension;

import UI.InfoController;
import UI.InputHandler;
import UI.MainUIController;
import UI.MapView;

public interface GraphicsEngine
{
  public IView getMainUIView(MainUIController control);
  public MapView createMapView(GameInstance game);
  public IView createInfoView(InfoController control);

  /**
   * Handles user input when configuring graphics options made available by
   * this GraphicsEngine.
   * @param action The command input by the user
   * @return true if the input triggers the menu to exit.
   */
  public boolean handleOptionsInput(InputHandler.InputAction action);

  /**
   * @return The current dimensions of the game's drawing area.
   */
  public Dimension getScreenDimensions();
}
