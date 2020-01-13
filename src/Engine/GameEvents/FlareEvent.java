package Engine.GameEvents;

import java.util.ArrayList;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class FlareEvent implements GameEvent
{
  private final Unit launcher;
  private final XYCoord target;
  private final int radius;

  public FlareEvent(Unit unit, XYCoord pTarget, int pRadius)
  {
    launcher = unit;
    target = pTarget;
    radius = pRadius;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    // TODO
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    launcher.ammo -= 1;
    ArrayList<XYCoord> tiles = Utils.findLocationsInRange(gameMap, target, 0, radius);
    for( XYCoord xyc : tiles )
      launcher.CO.myView.revealFog(xyc, true);
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(launcher.x, launcher.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return target;
  }
}
