package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

public class UnitTransformEvent implements GameEvent
{
  private Unit unit;
  private UnitModel oldType;
  private UnitModel destinationType;

  public UnitTransformEvent(Unit unit, UnitModel destination)
  {
    this.unit = unit;
    oldType = unit.model;
    destinationType = destination;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveUnitTransformEvent(unit, oldType);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    // TODO: Consider fiddling with ammo count
    unit.model = destinationType;
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }
}
