package Engine;

import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MoveEvent;
import Engine.GameEvents.UnitDieEvent;
import Terrain.Environment.Terrains;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;

/**
 * Provides an interface for all in-game actions.
 */
public interface GameAction
{
  public enum ActionType
  {
    INVALID, ATTACK, CAPTURE, LOAD, RESUPPLY, UNLOAD, WAIT
  }

  public abstract void getEvents(GameMap map, GameEventQueue eventSequence);
  public abstract XYCoord getMoveLocation();
  public abstract XYCoord getTargetLocation();
  public abstract ActionType getType();

  // ==========================================================
  //   Concrete Action type classes.
  // ==========================================================

  // ===========  AttackAction  ===============================
  public static class AttackAction implements GameAction
  {
    private Unit attacker = null;
    private Path movePath = null;
    private XYCoord moveLocation = null;
    private XYCoord attackLocation = null;
    private int attackRange = 0;

    public AttackAction(Unit actor, Path path, XYCoord atkLoc)
    {
      attacker = actor;
      movePath = path;
      moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      attackLocation = atkLoc;
      attackRange = Math.abs(moveLocation.xCoord - attackLocation.xCoord) + Math.abs(moveLocation.yCoord - attackLocation.yCoord);
    }

    @Override
    public void getEvents(GameMap gameMap, GameEventQueue eventSequence)
    {
      // ATTACK actions consist of
      //   MOVE
      //   BATTLE
      //   [DEATH]
      //   [DEFEAT]

      Unit unitTarget = gameMap.getLocation(attackLocation).getResident();

      // Make sure this is a valid battle before creating the event.
      boolean moved = attacker.x != moveLocation.xCoord || attacker.y != moveLocation.yCoord;
      if( unitTarget != null && attacker.canAttack(unitTarget.model, attackRange, moved) )
      {
        eventSequence.add(new MoveEvent(attacker, movePath));
        BattleEvent event = new BattleEvent(attacker, unitTarget, moveLocation.xCoord, moveLocation.yCoord, gameMap);
        eventSequence.add(event);

        if( event.attackerDies() )
        {
          eventSequence.add(new UnitDieEvent(attacker));

          // Since the attacker died, see if he has any friends left.
          if( attacker.CO.units.size() == 1 )
          {
            // CO is out of units. Too bad.
            eventSequence.add(new CommanderDefeatEvent(attacker.CO));
          }
        }
        if( event.defenderDies() )
        {
          eventSequence.add(new UnitDieEvent(unitTarget));

          // The defender died; check if the Commander is defeated.
          if( unitTarget.CO.units.size() == 1 )
          {
            // CO is out of units. Too bad.
            eventSequence.add(new CommanderDefeatEvent(unitTarget.CO));
          }
        }
      }
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveLocation;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return attackLocation;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.ATTACK;
    }
  } // ~AttackAction

  // ===========  CaptureAction  ==============================
  public static class CaptureAction implements GameAction
  {
    private Unit conquistador = null;
    private Path movePath = null;
    private XYCoord propertyLoc = null;

    public CaptureAction(Unit actor, Path path)
    {
      conquistador = actor;
      movePath = path;
      propertyLoc = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
    }

    @Override
    public void getEvents(GameMap map, GameEventQueue eventSequence)
    {
      // CAPTURE actions consist of
      //   MOVE
      //   CAPTURE
      //   [DEFEAT]

      GameEventQueue myEvents = new GameEventQueue();
      // Move to the target location.
      myEvents.add(new MoveEvent(conquistador, movePath));

      // Attempt to capture.
      Location loc = map.getLocation(propertyLoc);
      if( loc.isCaptureable() && loc.getOwner() != conquistador.CO )
      {
        CaptureEvent capture = new CaptureEvent(conquistador, map.getLocation(propertyLoc));
        myEvents.add(capture);

        if( capture.willCapture() ) // If this will succeed, check if the CO will lose as a result.
        {
          // Check if capturing this property will cause someone's defeat.
          if( loc.getEnvironment().terrainType == Terrains.HQ )
          {
            // Someone is losing their big, comfy chair.
            myEvents.add(new CommanderDefeatEvent(loc.getOwner()));
          }
        }
      }
      else
      {
        System.out.println("ERROR! Attempting to capture invalid location!");
        myEvents.clear();
      }

      eventSequence.addAll(myEvents);
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return propertyLoc;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return propertyLoc;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.CAPTURE;
    }
  } // ~CaptureAction

  // ===========  WaitAction  =================================
  public static class WaitAction implements GameAction
  {
    private Unit waiter = null;
    private Path movePath = null;
    private XYCoord waitLoc = null;

    public WaitAction(Unit actor, Path path)
    {
      waiter = actor;
      movePath = path;
      waitLoc = new XYCoord(path.getEnd().x, path.getEnd().y);
    }

    @Override
    public void getEvents(GameMap map, GameEventQueue eventSequence)
    {
      // WAIT actions consist of
      //   MOVE

      // Move to the target location.
      eventSequence.add(new MoveEvent(waiter, movePath));
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return waitLoc;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return waitLoc;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.WAIT;
    }
  } // ~WaitAction
}