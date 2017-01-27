package Engine.GameEvents;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnitDieEvent implements GameEvent
{
  private Unit unit;

  public UnitDieEvent( Unit unit )
  {
    this.unit = unit;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildUnitDieAnimation();
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    gameMap.removeUnit(unit);
    unit.CO.units.remove(unit);
  }
}
