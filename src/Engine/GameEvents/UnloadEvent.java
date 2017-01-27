package Engine.GameEvents;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnloadEvent implements GameEvent
{
  private final Unit transport;
  private final Unit cargo;
  private final int dropX;
  private final int dropY;

  public UnloadEvent( Unit transport, Unit cargo, int dropX, int dropY )
  {
    this.transport = transport;
    this.cargo = cargo;
    this.dropX = dropX;
    this.dropY = dropY;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildUnloadAnimation();
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    transport.isTurnOver = true;
    transport.heldUnits.remove(cargo);
    gameMap.moveUnit(cargo, dropX, dropY);
    cargo.isTurnOver = true;
  }

}
