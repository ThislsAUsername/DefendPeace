package Engine;

import java.util.ArrayList;

import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.LoadEvent;
import Engine.GameEvents.MoveEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.GameEvents.UnloadEvent;
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

  public abstract GameEventQueue getEvents(GameMap map);
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
    public GameEventQueue getEvents(GameMap gameMap)
    {
      // ATTACK actions consist of
      //   MOVE
      //   BATTLE
      //   [DEATH]
      //   [DEFEAT]
      GameEventQueue eventSequence = new GameEventQueue();

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
      return eventSequence;
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
    public GameEventQueue getEvents(GameMap map)
    {
      // CAPTURE actions consist of
      //   MOVE
      //   CAPTURE
      //   [DEFEAT]
      GameEventQueue eventSequence = new GameEventQueue();

      // Move to the target location.
      eventSequence.add(new MoveEvent(conquistador, movePath));

      // Attempt to capture.
      Location loc = map.getLocation(propertyLoc);
      if( loc.isCaptureable() && loc.getOwner() != conquistador.CO )
      {
        CaptureEvent capture = new CaptureEvent(conquistador, map.getLocation(propertyLoc));
        eventSequence.add(capture);

        if( capture.willCapture() ) // If this will succeed, check if the CO will lose as a result.
        {
          // Check if capturing this property will cause someone's defeat.
          if( loc.getEnvironment().terrainType == Terrains.HQ )
          {
            // Someone is losing their big, comfy chair.
            eventSequence.add(new CommanderDefeatEvent(loc.getOwner()));
          }
        }
      }
      else
      {
        System.out.println("ERROR! Attempting to capture invalid location!");
        eventSequence.clear();
      }

      return eventSequence;
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
    public GameEventQueue getEvents(GameMap map)
    {
      // WAIT actions consist of
      //   MOVE
      GameEventQueue eventSequence = new GameEventQueue();

      // Move to the target location.
      eventSequence.add(new MoveEvent(waiter, movePath));

      return eventSequence;
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

  // ===========  LoadAction  =================================
  public static class LoadAction implements GameAction
  {
    private Unit passenger = null;
    private Path movePath = null;
    private XYCoord moveLoc = null;

    public LoadAction(Unit actor, Path path)
    {
      passenger = actor;
      movePath = path;
      moveLoc = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      // LOAD actions consist of
      //   MOVE
      //   LOAD
      GameEventQueue eventSequence = new GameEventQueue();

      // Find the transport unit.
      Unit transport = map.getLocation(moveLoc).getResident();

      if( null != transport )
      {
        // Move to the transport.
        eventSequence.add(new MoveEvent(passenger, movePath));

        // Get in the transport.
        eventSequence.add(new LoadEvent(passenger, transport));
      }
      else
      {
        System.out.println("Failed to find transport to create LOAD event. Aborting.");
        eventSequence.clear();
      }

      return eventSequence;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveLoc;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return moveLoc;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.LOAD;
    }
  } // ~LoadAction

  // ===========  UnloadAction  =================================
  public static class UnloadAction implements GameAction
  {
    private Unit transport = null;
    private Unit cargo = null;
    private Path movePath = null;
    private XYCoord moveLoc = null;
    private XYCoord dropLoc = null;

    public UnloadAction(Unit actor, Path path, Unit passenger, XYCoord dropLocation)
    {
      transport = actor;
      cargo = passenger;
      movePath = path;
      moveLoc = new XYCoord(path.getEnd().x, path.getEnd().y);
      dropLoc = dropLocation;
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      // UNLOAD actions consist of
      //   MOVE (transport)
      //   UNLOAD
      GameEventQueue eventSequence = new GameEventQueue();

      // Move transport to the target location.
      eventSequence.add(new MoveEvent(transport, movePath));

      // If we have cargo and the landing zone is empty, we drop the cargo.
      if( !transport.heldUnits.isEmpty() && map.isLocationEmpty(transport, dropLoc) )
      {
        eventSequence.add(new UnloadEvent(transport, cargo, dropLoc));
      }

      return eventSequence;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveLoc;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return dropLoc;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.UNLOAD;
    }
  } // ~UnloadAction

  // ===========  ResupplyAction  =================================
  // A resupply action will refill fuel and ammunition for any adjacent friendly units.
  public static class ResupplyAction implements GameAction
  {
    private Unit unitActor = null;
    private Path movePath = null;

    /**
     * Creates a resupply action to be executed from the unit's location.
     * The location will update if the unit moves.
     */
    public ResupplyAction(Unit actor)
    {
      this(actor, null);
    }

    /**
     * Creates a resupply action to be executed from the end of path.
     * The location will not update if the unit moves.
     * @param actor
     * @param path
     */
    public ResupplyAction(Unit actor, Path path)
    {
      unitActor = actor;
      movePath = path;
    }

    private XYCoord myLocation()
    {
      XYCoord loc;
      if( movePath != null )
      {
        loc = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      }
      else
      {
        loc = new XYCoord(unitActor.x, unitActor.y);
      }
      return loc;
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      // RESUPPLY actions consist of
      //   [MOVE]
      //   RESUPPLY
      GameEventQueue eventSequence = new GameEventQueue();

      // Figure out where we are acting.
      XYCoord supplyLocation = myLocation();

      // Add a move event if we need to move.
      if( movePath != null )
      {
        eventSequence.add(new MoveEvent(unitActor, movePath));
      }

      // Get the adjacent map locations.
      ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, supplyLocation, 1);

      // For each location, see if there is a friendly unit to re-supply.
      for( XYCoord loc : locations )
      {
        Unit other = map.getLocation(loc).getResident();
        if( other != null && other.CO == unitActor.CO )
        {
          // Add a resupply event for this unit.
          eventSequence.add(new ResupplyEvent(other));
        }
      }

      return eventSequence;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return myLocation();
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return myLocation();
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.RESUPPLY;
    }
  } // ~ResupplyAction
}