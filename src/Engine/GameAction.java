package Engine;

import CommandingOfficers.Commander;
import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventSequence;
import Engine.GameEvents.LoadEvent;
import Engine.GameEvents.MoveEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.GameEvents.UnloadEvent;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;

/**
 * Allows the building of an in-game action as a Unit, a location to move to, an ActionType, and an (optional) location to act on.
 * Once fully constructed (as affirmed by isReadyToExecute()), a call to execute() will cause GameAction to perform the action.
 */
public class GameAction
{
  public enum ActionType
  {
    INVALID, ATTACK, CAPTURE, LOAD, UNLOAD, WAIT
  };

  private Unit unitActor = null;
  private ActionType actionType;

  private Path movePath = null;
  private int moveX;
  private int moveY;
  private int actX;
  private int actY;

  public GameAction(Unit unit)
  {
    this(unit, -1, -1, ActionType.INVALID, -1, -1);
  }

  public GameAction(Unit unit, int mx, int my, ActionType action)
  {
    this(unit, mx, my, action, -1, -1);
  }

  // TODO: GameAction should accept a Path in the constructor, rather than mx and my.
  public GameAction(Unit unit, int mx, int my, ActionType action, int ax, int ay)
  {
    unitActor = unit;
    actionType = action;
    moveX = mx;
    moveY = my;
    actX = ax;
    actY = ay;
  }

  public void setActionType(ActionType type)
  {
    actionType = type;
  }

  public Unit getActor()
  {
    return unitActor;
  }

  public ActionType getActionType()
  {
    return actionType;
  }

  public void setMovePath(Path path)
  {
    movePath = path;
    if( null != path && path.getPathLength() > 0 )
    {
      moveX = path.getEnd().x;
      moveY = path.getEnd().y;
    }
    else
    {
      moveX = -1;
      moveY = -1;
    }
  }

  public Path getMovePath()
  {
    return movePath;
  }
  public int getMoveX()
  {
    return moveX;
  }
  public int getMoveY()
  {
    return moveY;
  }
  public int getActX()
  {
    return actX;
  }
  public int getActY()
  {
    return actY;
  }

  public void setActionLocation(int x, int y)
  {
    actX = x;
    actY = y;
  }

  /** Returns true if this GameAction has all the information it needs to execute, false else. */
  public boolean isReadyToExecute()
  {
    boolean ready = false;
    if( moveX >= 0 && moveY >= 0 && actionType != ActionType.INVALID )
    {
      if( actionType == ActionType.WAIT || actionType == ActionType.LOAD || actionType == ActionType.CAPTURE )
      {
        ready = true;
      }
      else
      // ActionType is Attack or Unload - needs a target
      {
        if( actX >= 0 && actY >= 0 )
        {
          ready = true;
        }
        else
        {
          System.out.println("Targeted ActionType cannot be executed without target location!");
        }
      }
    }
    else
    {
      if( actionType == ActionType.INVALID )
      {
        System.out.println("Invalid ActionType cannot be executed!");
      }
      else
      {
        System.out.println("GameAction with no move location cannot be executed!");
      }
    }
    return ready;
  }

  /**
   * Evaluate the action and construct the MapEvents necessary to render any changes in the game.
   * IF a GameAction is a castle, MapEvents are the bricks that compose it.
   * @return A MapEventSequence containing all MapEvents caused by this GameAction.
   */
  public GameEventSequence getGameEvents( GameMap gameMap )
  {
    GameEventSequence sequence = new GameEventSequence();

    // Make sure we have a path to our destination.
    if( movePath == null )
    {
      movePath = new Path(1.0); // TODO: No need for this parameter.
      Utils.findShortestPath(unitActor, moveX, moveY, movePath, gameMap);
    }

    // TODO: Check for ambushes in fog of war.

    switch (actionType)
    {
      case ATTACK:
      {
        // ATTACK actions consist of
        //   MOVE
        //   BATTLE
        //   [DEATH]
        //   [DEFEAT]

        Unit unitTarget = gameMap.getLocation(actX, actY).getResident();

        // Make sure this is a valid battle before creating the event.
        if( unitTarget != null && unitActor.getDamage(unitTarget, moveX, moveY) != 0 )
        {
          sequence.add( new MoveEvent(unitActor, movePath) );
          BattleEvent event = new BattleEvent(unitActor, unitTarget, moveX, moveY, gameMap);
          sequence.add(event);

          if( event.attackerDies() )
          {
            sequence.add( new UnitDieEvent( unitActor ) );

            // Since the attacker died, see if he has any friends left.
            if( unitActor.CO.units. isEmpty() )
            {
              // CO is out of units. Too bad.
              sequence.add( new CommanderDefeatEvent( unitActor.CO ) );
            }
          }
          if( event.defenderDies() )
          {
            sequence.add( new UnitDieEvent( unitTarget ) );

            // The defender died; check if the Commander is defeated.
            if( unitTarget.CO.units.isEmpty() )
            {
              // CO is out of units. Too bad.
              sequence.add( new CommanderDefeatEvent( unitTarget.CO ) );
            }
          }

          //sequence.add( new UnitEndTurnEvent( unitActor ) );
        }
        break;
      }
      case CAPTURE:
      {
        // CAPTURE actions consist of
        //   MOVE
        //   CAPTURE
        //   [DEFEAT]

        // Move to the target location.
        sequence.add( new MoveEvent(unitActor, movePath) );

        // Attempt to capture.
        Location loc = gameMap.getLocation( moveX, moveY );
        if( loc.isCaptureable() && loc.getOwner() != unitActor.CO )
        {
          CaptureEvent capture = new CaptureEvent( unitActor, gameMap.getLocation(moveX, moveY) );
          sequence.add( capture );

          if( capture.willCapture() ) // If this will succeed, check if the CO will lose as a result.
          {
            Commander targetCO = gameMap.getLocation(unitActor.x, unitActor.y).getOwner();
            if( targetCO != null && targetCO.HQLocation.getOwner() != targetCO )
            {
              // If targetCO no longer owns his HQ, too bad.
              sequence.add( new CommanderDefeatEvent( targetCO ) );
            }
          }
        }
        else
        {
          System.out.println("ERROR! Attempting to capture invalid location!");
          sequence.clear();
        }
        break;
      }
      case LOAD:
      {
        // LOAD actions consist of
        //   MOVE
        //   LOAD

        // Move to the target location.
        sequence.add( new MoveEvent(unitActor, movePath) );

        Unit transport = gameMap.getLocation(moveX, moveY).getResident();

        if( null != transport && transport.hasCargoSpace(unitActor.model.type) )
        {
          sequence.add( new LoadEvent( unitActor, transport ) );
        }
        else
        {
          System.out.println("WARNING! " + transport.model.type + " cannot carry " + unitActor.model.type + "!");
        }
        break;
      }
      case UNLOAD:
      {
        // UNLOAD actions consist of
        //   MOVE (transport)
        //   UNLOAD

        // Move transport to the target location.
        sequence.add( new MoveEvent(unitActor, movePath) );

        // If we have cargo and the landing zone is empty, we drop the cargo.
        if( !unitActor.heldUnits.isEmpty() && gameMap.isLocationEmpty(unitActor, actX, actY) )
        {
          Unit cargo = unitActor.heldUnits.remove(0); // TODO: Account for multi-Unit transports.
          sequence.add( new UnloadEvent( unitActor, cargo, actX, actY ) );
        }
        break;
      }
      case WAIT:
      {
        // WAIT actions consist of
        //   MOVE

        // Move transport to the target location.
        sequence.add( new MoveEvent(unitActor, movePath) );
        break;
      }
      case INVALID:
      default:
        System.out.println("Attempting to execute an invalid GameAction!");
    }

    return sequence;
  }
}