package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.Path;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * Moves a unit directly to the destination without traversing intermediate steps.
 * No validation is performed on the final destination, but affordances are provided for
 * conflict resolution (e.g. existing units can swap places, die, or simply be removed).
 * If a unit is teleported into non-traversable terrain, it will die; teleport carefully.
 */
public class TeleportEvent implements GameEvent
{
  private Unit unit;
  XYCoord unitStart;
  private XYCoord unitDestination;
  private AnimationStyle animationStyle;
  private CollisionOutcome collisionOutcome;

  public enum AnimationStyle
  {
    BLINK,
    DROP_IN
  }

  public enum CollisionOutcome
  {
    REMOVE,
    KILL,
    SWAP
  }

  public TeleportEvent(Unit u, XYCoord dest, AnimationStyle animStyle, CollisionOutcome crashResult)
  {
    unit = u;
    unitDestination = dest;
    animationStyle = animStyle;
    collisionOutcome = crashResult;
  }

  public TeleportEvent(Unit u, XYCoord dest, AnimationStyle animStyle)
  {
    this(u, dest, animStyle, CollisionOutcome.REMOVE); // No muss, no fuss.
  }

  public TeleportEvent(Unit u, XYCoord dest, CollisionOutcome crashResult)
  {
    this(u, dest, AnimationStyle.BLINK, crashResult); // No muss, no fuss.
  }

  public TeleportEvent(Unit u, XYCoord dest)
  {
    this(u, dest, AnimationStyle.BLINK, CollisionOutcome.REMOVE); // No muss, no fuss.
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTeleportAnimation(unit, unitStart, unitDestination, animationStyle, collisionOutcome);
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveTeleportEvent(this);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    gameMap.removeUnit(unit); // First remove our guy. He's in the ether.

    // Figure out if something's in the way, and what to do with it.
    Unit oldResident = gameMap.getLocation(unitDestination).getResident();
    if( null != oldResident )
    {
      switch(collisionOutcome)
      {
        case REMOVE:
          gameMap.removeUnit(oldResident);
          break;
        case KILL:
          UnitDieEvent ude = new UnitDieEvent(oldResident);
          ude.performEvent(gameMap);
          break;
        case SWAP:
          // Move him to where our guy started. If he can't live there, he dies.
          gameMap.moveUnit(oldResident, unitStart.xCoord, unitStart.yCoord);
          if( !oldResident.model.propulsion.canTraverse(gameMap.getEnvironment(unitStart)) )
            new UnitDieEvent(oldResident).performEvent(gameMap);
          break;
      }
    }

    // Put our guy where he belongs.
    gameMap.moveUnit(unit, unitDestination.xCoord, unitDestination.yCoord);

    // If our guy can't survive there, end him.
    if( !unit.model.propulsion.canTraverse(gameMap.getEnvironment(unitDestination)) )
    {
      new UnitDieEvent(unit).performEvent(gameMap);
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return unitStart;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return unitDestination;
  }
}
