package Engine;

import UI.MainUIController;
import UI.MapView;

public interface GraphicsEngine
{
  public IView getMainUIView(MainUIController control);
  public MapView createMapView(GameInstance game);
}
