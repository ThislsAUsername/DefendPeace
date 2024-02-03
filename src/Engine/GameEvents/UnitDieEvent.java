package Engine.GameEvents;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnitDieEvent implements GameEvent
{
  private Unit unit;
  private XYCoord where;
  private Integer healthBeforeDeath;

  public UnitDieEvent(Unit unit)
  {
    this(unit, new XYCoord(unit));
  }
  public UnitDieEvent(Unit unit, XYCoord where)
  {
    this.unit = unit;
    this.where = where;
    this.healthBeforeDeath = unit.getHealth();
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildUnitDieAnimation();
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveUnitDieEvent( unit, where, healthBeforeDeath );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    // Set HP to 0. One could make a UnitDieEvent on a healthy
    // unit, and we don't want any ambiguity after the fact.
    unit.damageHealth(unit.getHealth());

    // Remove the Unit from the map and from the CO list.
    gameMap.removeUnit(unit);
    unit.CO.units.remove(unit);

    // We need to take vision away immediately if your unit dies off-turn (specifically for DoR fog)
    Army activeArmy = gameMap.game.activeArmy;
    if( activeArmy != unit.CO.army )
    {
      for( Army army : gameMap.game.armies )
      {
        if( army == activeArmy )
          continue;
        army.myView.resetFog();
      }
    }
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
