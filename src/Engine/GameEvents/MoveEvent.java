package Engine.GameEvents;

import Engine.Army;
import Engine.GamePath;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * Moves a unit to the end of the provided path. Only the final path location
 * is validated, so any path-validation checks (e.g. for collisions) should
 * be done before the creation of the MoveEvent. Having the whole path is
 * still useful for the animator.
 */
public class MoveEvent implements GameEvent
{
  private Unit unit = null;
  private GamePath unitPath = null;

  public MoveEvent(Unit u, GamePath path)
  {
    unit = u;
    unitPath = path;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildMoveAnimation(unit, unitPath);
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveMoveEvent(unit, unitPath);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( unitPath.getPathLength() > 0 ) // Make sure we have a destination.
    {
      GamePath.PathNode endpoint = unitPath.getEnd();
      int fuelBurn = unitPath.getFuelCost(unit, gameMap);

      gameMap.moveUnit(unit, endpoint.x, endpoint.y);
      unit.isTurnOver = true;

      unit.fuel = Math.max(0, unit.fuel - fuelBurn);

      // reveal fog as applicable
      for( Army co : gameMap.game.armies )
      {
        co.myView.revealFog(unit, unitPath);
      }
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(unitPath.getWaypoint(0).x, unitPath.getWaypoint(0).y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return unitPath.getEndCoord();
  }
}
