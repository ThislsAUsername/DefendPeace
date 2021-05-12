package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnitDieEvent implements GameEvent
{
  private Unit unit;
  private XYCoord where;
  private Integer hpBeforeDeath;

  public UnitDieEvent( Unit unit )
  {
    this.unit = unit;
    this.where = new XYCoord(unit.x, unit.y);
    this.hpBeforeDeath = unit.getHP();
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildUnitDieAnimation();
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveUnitDieEvent( unit, where, hpBeforeDeath );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    // Set HP to 0. One could make a UnitDieEvent on a healthy
    // unit, and we don't want any ambiguity after the fact.
    unit.damageHP(unit.getHP()+1);

    // Remove the Unit from the map and from the CO list.
    gameMap.removeUnit(unit);
    unit.CO.units.remove(unit);
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
