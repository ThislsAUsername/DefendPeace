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
    KILL,
    SWAP
  }

  public TeleportEvent(Unit u, XYCoord dest, AnimationStyle animStyle, CollisionOutcome crashResult)
  {
    unit = u;
    unitStart = new XYCoord(unit.x, unit.y);
    unitDestination = dest;
    animationStyle = animStyle;
    collisionOutcome = crashResult;
  }

  public TeleportEvent(Unit u, XYCoord dest, AnimationStyle animStyle)
  {
    this(u, dest, animStyle, CollisionOutcome.KILL);
  }

  public TeleportEvent(Unit u, XYCoord dest, CollisionOutcome crashResult)
  {
    this(u, dest, AnimationStyle.BLINK, crashResult);
  }

  public TeleportEvent(Unit u, XYCoord dest)
  {
    this(u, dest, AnimationStyle.BLINK, CollisionOutcome.KILL);
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTeleportAnimation(unit, unitStart, unitDestination, animationStyle, collisionOutcome);
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveTeleportEvent(unit, unitStart, unitDestination);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    gameMap.removeUnit(unit); // First remove our guy. He's in the ether.

    // Figure out if something's in the way, and what to do with it.
    Unit oldResident = gameMap.getLocation(unitDestination).getResident();
    boolean killOldResident = false;
    if( null != oldResident )
    {
      switch(collisionOutcome)
      {
        case KILL:
          killOldResident = true;
          break;
        case SWAP:
          // Move him to where our guy started. If he can't live there, he dies.
          if( gameMap.isLocationValid(unitStart) )
          {
            gameMap.moveUnit(oldResident, unitStart.xCoord, unitStart.yCoord);
            if( !oldResident.model.propulsion.canTraverse(gameMap.getEnvironment(unitStart)) )
            {
              killOldResident = true;
            }
          }
          else killOldResident = true;
          break;
      }
    }

    if( killOldResident )
    {
      UnitDieEvent ude = new UnitDieEvent(oldResident);
      ude.performEvent(gameMap);

      // Poor sap died; Check if his CO lost the game.
      if( oldResident.CO.units.size() == 0 )
      {
        new CommanderDefeatEvent(oldResident.CO).performEvent(gameMap);
      }
    }

    // Put our guy where he belongs.
    gameMap.moveUnit(unit, unitDestination.xCoord, unitDestination.yCoord);

    // If our guy can't survive there, end him.
    if( !unit.model.propulsion.canTraverse(gameMap.getEnvironment(unitDestination)) )
    {
      new UnitDieEvent(unit).performEvent(gameMap);

      // Our unit died; check if we are defeated.
      if( unit.CO.units.size() == 0 )
      {
        // CO is out of units. Too bad.
        new CommanderDefeatEvent(unit.CO).performEvent(gameMap);
      }
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
