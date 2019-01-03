package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.Path;
import Terrain.Location;
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
  private Path unitPath = null;

  public MoveEvent(Unit u, Path path)
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
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveMoveEvent(this);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( unitPath.getPathLength() > 0 ) // Make sure we have a destination.
    {
      Path.PathNode endpoint = unitPath.getEnd();
      Location loc = gameMap.getLocation(endpoint.x, endpoint.y);
      int fuelBurn = unitPath.getFuelCost(unit.model, gameMap);

      // If the unit is already at the destination, we don't need to move it.
      if( 0 == fuelBurn )
      {
        unit.isTurnOver = true;
      }
      // Make sure it is valid to move this unit to its destination.
      else if( loc.getResident() == null && unit.model.propulsion.getMoveCost(loc.getEnvironment()) < 99 )
      {
        gameMap.moveUnit(unit, endpoint.x, endpoint.y);

        unit.fuel -= fuelBurn;

        // Every unit action begins with a (potentially 0-distance) move,
        // so we'll just set the "has moved" flag here.
        unit.isTurnOver = true;

        // reveal fog as applicable
        for( Commander co : gameMap.commanders )
        {
          co.myView.revealFog(unit, unitPath);
        }
      }
      else
      {
        System.out.println("WARNING! Unable to move " + unit.model.type + " to (" + endpoint.x + ", " + endpoint.y + ")");
      }
    }
  }
}
