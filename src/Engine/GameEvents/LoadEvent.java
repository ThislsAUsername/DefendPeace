package Engine.GameEvents;

import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class LoadEvent implements GameEvent
{
  private Unit unitCargo = null;
  private Unit unitTransport = null;

  public LoadEvent( Unit cargo, Unit transport )
  {
    unitCargo = cargo;
    unitTransport = transport;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildLoadAnimation();
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveLoadEvent( this );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( null != unitTransport && unitTransport.hasCargoSpace(unitCargo.model.type) )
    {
      gameMap.removeUnit(unitCargo);
      unitTransport.heldUnits.add(unitCargo);
    }
    else
    {
      System.out.println("WARNING! Cannot load " + unitCargo.model.type + " onto " + unitTransport.model.type );
    }
  }
}
