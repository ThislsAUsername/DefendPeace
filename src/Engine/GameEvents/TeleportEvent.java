package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.GameMap;
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
  private Unit obstacle;
  private AnimationStyle animationStyle;
  private CollisionOutcome collisionOutcome;

  private boolean unitDies;
  private boolean obstacleDies;

  private GameEventQueue subEvents = new GameEventQueue();

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

  public TeleportEvent(GameMap map, Unit u, XYCoord dest, AnimationStyle animStyle, CollisionOutcome crashResult)
  {
    unit = u;
    unitStart = new XYCoord(unit.x, unit.y);
    unitDestination = dest;
    animationStyle = animStyle;
    collisionOutcome = crashResult;
    unitDies = false;
    obstacleDies = false;
  }

  public TeleportEvent(GameMap map, Unit u, XYCoord dest, AnimationStyle animStyle)
  {
    this(map, u, dest, animStyle, CollisionOutcome.KILL);
  }

  public TeleportEvent(GameMap map, Unit u, XYCoord dest, CollisionOutcome crashResult)
  {
    this(map, u, dest, AnimationStyle.BLINK, crashResult);
  }

  public TeleportEvent(GameMap map, Unit u, XYCoord dest)
  {
    this(map, u, dest, AnimationStyle.BLINK, CollisionOutcome.KILL);
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTeleportAnimation(unit, unitStart, unitDestination, obstacle, animationStyle, unitDies, obstacleDies);
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveTeleportEvent(unit, unitStart, unitDestination);
    for( GameEvent sub : subEvents )
      sub.sendToListener(listener);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    gameMap.removeUnit(unit); // First remove our guy. He's in the ether.

    // Figure out if something's in the way, and what to do with it.
    obstacle = gameMap.getLocation(unitDestination).getResident();
    obstacleDies = false;
    if( null != obstacle )
    {
      switch(collisionOutcome)
      {
        case KILL:
          obstacleDies = true;
          break;
        case SWAP:
          // Move him to where our guy started. If he can't live there, he dies.
          if( gameMap.isLocationValid(unitStart) )
          {
            gameMap.moveUnit(obstacle, unitStart.xCoord, unitStart.yCoord);
            if( !obstacle.model.propulsion.canTraverse(gameMap.getEnvironment(unitStart)) )
            {
              obstacleDies = true;
            }
          }
          else obstacleDies = true;
          break;
      }
    }

    if( obstacleDies )
    {
      UnitDieEvent ude = new UnitDieEvent(obstacle);
      ude.performEvent(gameMap);
      subEvents.add(ude);

      // Poor sap died; Check if his CO lost the game.
      if( obstacle.CO.units.size() == 0 )
      {
        CommanderDefeatEvent cde = new CommanderDefeatEvent(obstacle.CO);
        cde.performEvent(gameMap);
        subEvents.add(cde);
      }
    }

    // Put our guy where he belongs.
    gameMap.moveUnit(unit, unitDestination.xCoord, unitDestination.yCoord);

    // If our guy can't survive there, end him.
    if( !unit.model.propulsion.canTraverse(gameMap.getEnvironment(unitDestination)) )
    {
      unitDies = true;
      UnitDieEvent ude = new UnitDieEvent(unit);
      ude.performEvent(gameMap);
      subEvents.add(ude);

      // Our unit died; check if we are defeated.
      if( unit.CO.units.size() == 0 )
      {
        // CO is out of units. Too bad.
        CommanderDefeatEvent cde = new CommanderDefeatEvent(unit.CO);
        cde.performEvent(gameMap);
        subEvents.add(cde);
      }
    }
  }

  public Unit getUnit()
  {
    return unit;
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
