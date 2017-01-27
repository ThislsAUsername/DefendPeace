package Engine.GameEvents;

import Terrain.GameMap;
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
  public void performEvent(GameMap gameMap)
  {
    if( null != unitTransport && unitTransport.hasCargoSpace(unitCargo.model.type) )
    {
      gameMap.removeUnit(unitCargo);
      unitTransport.heldUnits.add(unitCargo);
    }
    else
    {
      System.out.println("WARNING! Failed to load " + unitCargo.model.type + " onto " + unitTransport.model.type );
    }
  }
}
