package Engine.GameEvents;

import Engine.Path;
import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class MoveEvent implements GameEvent
{
  private Unit unit = null;
  private Path unitPath = null;

  public MoveEvent(Unit u, Path path)
  {
    unit = u;
    unitPath = path;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildMoveAnimation();
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    gameMap.moveUnit(unit, unitPath.getEnd().x, unitPath.getEnd().y);

    // Every unit action is preceded by a move (possibly to the same location),
    // so we'll just set the "has moved" flag here. 
    unit.isTurnOver = true;
  }
}
