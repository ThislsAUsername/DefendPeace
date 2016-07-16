package Engine;

import UI.InputHandler;
import UI.MainUIController;
import UI.MapView;

public interface GraphicsEngine
{
  public IView getMainUIView(MainUIController control);
  public MapView createMapView(GameInstance game);

  /**
   * Handles user input when configuring graphics options made available by
   * this GraphicsEngine.
   * @param action The command input by the user
   * @return true if the input triggers the menu to exit.
   */
  public boolean handleOptionsInput(InputHandler.InputAction action);
}
