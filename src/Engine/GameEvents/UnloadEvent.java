package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnloadEvent implements GameEvent
{
  private final Unit transport;
  private final Unit cargo;
  private final XYCoord dropLoc;

  public UnloadEvent(Unit transport, Unit cargo, int dropX, int dropY)
  {
    this(transport, cargo, new XYCoord(dropX, dropY));
  }

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
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveUnloadEvent( this );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( transport.heldUnits != null && transport.heldUnits.contains(cargo) && gameMap.getLocation(dropLoc).getResident(gameMap) == null )
    {
      transport.heldUnits.remove(cargo);
      gameMap.moveUnit(cargo, dropLoc.xCoord, dropLoc.yCoord);
      cargo.isTurnOver = true;
    }
    else
    {
      System.out.println("WARNING! Failed to unload unit due to preconditions not being met:");
      if( transport.heldUnits == null ) System.out.println("          Transport unit is empty.");
      if( !transport.heldUnits.contains(cargo) ) System.out.println("          Unit to debark is not on transport.");
      if( gameMap.getLocation(dropLoc).getResident(gameMap) != null ) System.out.println("          Unload location is not empty.");
    }
  }
  
  @Override
  public boolean shouldPreempt(MapMaster gameMap )
  {
    boolean result = false;
    Unit obstacle = gameMap.getLocation(dropLoc).getResident(gameMap);
    if( null != obstacle && transport.CO.isEnemy(obstacle.CO) )
    {
      result = true;
    }
    return result;
  }
}
