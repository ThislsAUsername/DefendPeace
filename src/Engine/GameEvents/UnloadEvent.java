package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnloadEvent implements GameEvent
{
  private final Unit transport;
  private final Unit cargo;
  private final XYCoord dropLoc;

  public UnloadEvent(Unit transport, Unit cargo, XYCoord dropLoc)
  {
    this.transport = transport;
    this.cargo = cargo;
    this.dropLoc = dropLoc;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildUnloadAnimation();
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    if( transport.heldUnits != null && transport.heldUnits.contains(cargo) && gameMap.getLocation(dropLoc).getResident() == null )
    {
      transport.heldUnits.remove(cargo);
      gameMap.moveUnit(cargo, dropLoc.xCoord, dropLoc.yCoord);
      cargo.isTurnOver = true;
    }
  }

}
