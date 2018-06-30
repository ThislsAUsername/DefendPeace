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
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
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

  /**
   * Returns a GameEventQueue with the events that make up this action. If the action
   * was constructed incorrectly, this should return an empty GameEventQueue.
   */
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
    private XYCoord moveLocation = null;
    private XYCoord attackLocation = null;
    private GameEventQueue attackEvents = null;

    public AttackAction(GameMap gameMap, Unit actor, Path path, int targetX, int targetY)
    {
      this(gameMap, actor, path, new XYCoord(targetX, targetY));
    }

    public AttackAction(GameMap gameMap, Unit attacker, Path movePath, XYCoord atkLoc)
    {
      // ATTACK actions consist of
      //   MOVE
      //   BATTLE
      //   [DEATH]
      //   [DEFEAT]
      attackEvents = new GameEventQueue();
      attackLocation = atkLoc;
      Unit unitTarget = null;
      int attackRange = -1;

      // Validate input.
      boolean isValid = true;
      isValid &= attacker != null;
      isValid &= (null != gameMap) && (null != attackLocation) && (gameMap.isLocationValid(attackLocation));
      isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      if( isValid )
      {
        moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        unitTarget = gameMap.getLocation(attackLocation).getResident();
        attackRange = Math.abs(moveLocation.xCoord - attackLocation.xCoord)
            + Math.abs(moveLocation.yCoord - attackLocation.yCoord);

        boolean moved = attacker.x != moveLocation.xCoord || attacker.y != moveLocation.yCoord;
        isValid &= (null != unitTarget) && attacker.canAttack(unitTarget.model, attackRange, moved);
      }

      // Generate GameEvents.
      if( isValid )
      {
        attackEvents.add(new MoveEvent(attacker, movePath));
        BattleEvent event = new BattleEvent(attacker, unitTarget, moveLocation.xCoord, moveLocation.yCoord, gameMap);
        attackEvents.add(event);

        if( event.attackerDies() )
        {
          attackEvents.add(new UnitDieEvent(attacker));

          // Since the attacker died, see if he has any friends left.
          if( attacker.CO.units.size() == 1 )
          {
            // CO is out of units. Too bad.
            attackEvents.add(new CommanderDefeatEvent(attacker.CO));
          }
        }
        if( event.defenderDies() )
        {
          attackEvents.add(new UnitDieEvent(unitTarget));

          // The defender died; check if the Commander is defeated.
          if( unitTarget.CO.units.size() == 1 )
          {
            // CO is out of units. Too bad.
            attackEvents.add(new CommanderDefeatEvent(unitTarget.CO));
          }
        }
      }
      else
      {
        // We can't create this action. Leave the event queue empty.
        System.out.println("WARNING! AttackAction created with invalid arguments.");
      }
    }

    @Override
    public GameEventQueue getEvents(GameMap gameMap)
    {
      return attackEvents;
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
    private XYCoord movePathEnd = null;
    private GameEventQueue captureEvents = null;

    public CaptureAction(GameMap map, Unit actor, Path movePath)
    {
      // CAPTURE actions consist of
      //   MOVE
      //   CAPTURE
      //   [DEFEAT]
      captureEvents = new GameEventQueue();
      Location captureLocation = null;

      // Validate input
      boolean isValid = true;
      isValid &= null != actor; // Valid unit
      isValid &= null != map; // Valid map
      isValid &= (null != movePath) && (movePath.getPathLength() > 0); // Valid path
      if( isValid )
      {
        movePathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        captureLocation = map.getLocation(movePathEnd);
        isValid &= captureLocation.isCaptureable(); // Valid location
        isValid &= captureLocation.getOwner() != actor.CO; // Valid CO
      }

      // Generate events
      if( isValid )
      {
        // Move to the target location.
        captureEvents.add(new MoveEvent(actor, movePath));

        // Attempt to capture.
        CaptureEvent capture = new CaptureEvent(actor, map.getLocation(movePathEnd));
        captureEvents.add(capture);

        if( capture.willCapture() ) // If this will succeed, check if the CO will lose as a result.
        {
          // Check if capturing this property will cause someone's defeat.
          if( (captureLocation.getEnvironment().terrainType == TerrainType.HEADQUARTERS) && (null != captureLocation.getOwner()) )
          {
            // Someone is losing their big, comfy chair.
            captureEvents.add(new CommanderDefeatEvent(captureLocation.getOwner()));
          }
        }
      }
      else
      {
        // We can't create this action. Leave the event queue empty.
        System.out.println("WARNING! Capture action was created incorrectly.");
      }
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      return captureEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return movePathEnd;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return movePathEnd;
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
    private XYCoord waitLoc = null;
    private GameEventQueue waitEvents = null;

    public WaitAction(GameMap gameMap, Unit actor, Path movePath)
    {
      // WAIT actions consist of
      //   MOVE
      waitEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
      int goX = -1, goY = -1;
      if( isValid )
      {
        goX = movePath.getEnd().x;
        goY = movePath.getEnd().y;
        isValid &= gameMap.isLocationEmpty(actor, goX, goY);
      }

      // Generate events.
      if( isValid )
      {
        // Move to the target location.
        waitEvents.add(new MoveEvent(actor, movePath));

        // Store the destination for later.
        waitLoc = new XYCoord(goX, goY);
      }
      else
      {
        // We can't create this action. Leave the event queue empty.
        System.out.println("WARNING! WaitAction was initialized incorrectly.");
      }
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      return waitEvents;
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
    private XYCoord pathEnd = null;
    private GameEventQueue loadEvents = null;

    public LoadAction(GameMap gameMap, Unit passenger, Path movePath)
    {
      // LOAD actions consist of
      //   MOVE
      //   LOAD
      loadEvents = new GameEventQueue();

      // Validate input
      boolean isValid = true;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
      Unit transport = null;
      if( isValid )
      {
        pathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        isValid &= gameMap.isLocationValid(pathEnd);

        if( isValid )
        {
          // Find the transport unit.
          transport = gameMap.getLocation(pathEnd).getResident();
          isValid &= (null != transport) && transport.hasCargoSpace(passenger.model.type);
        }
      }

      if( isValid )
      {
        // Move to the transport.
        loadEvents.add(new MoveEvent(passenger, movePath));

        // Get in the transport.
        loadEvents.add(new LoadEvent(passenger, transport));
      }
      else
      {
        // We can't create this action. Leave the event queue empty.
        System.out.println("WARNING! Failed to create a valid LOAD event.");
      }
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      return loadEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return pathEnd;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return pathEnd;
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
    private XYCoord moveLoc = null;
    private XYCoord dropLoc = null;
    private GameEventQueue unloadEvents = null;

    public UnloadAction(GameMap gameMap, Unit actor, Path path, Unit passenger, int dropX, int dropY)
    {
      this(gameMap, actor, path, passenger, new XYCoord(dropX, dropY));
    }

    public UnloadAction(GameMap gameMap, Unit transport, Path movePath, Unit passenger, XYCoord dropLocation)
    {
      // UNLOAD actions consist of
      //   MOVE (transport)
      //   UNLOAD
      unloadEvents = new GameEventQueue();
      dropLoc = dropLocation;

      // Validate input.
      boolean isValid = true;
      isValid &= null != transport;
      isValid &= null != passenger;
      isValid &= null != dropLoc;
      isValid &= movePath.getPathLength() > 0;
      isValid &= null != gameMap;
      if( isValid )
      {
        isValid &= !transport.heldUnits.isEmpty();
        moveLoc = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        isValid &= gameMap.isLocationEmpty(transport, moveLoc); // Move location is unoccupied.
        isValid &= gameMap.isLocationEmpty(transport, dropLoc); // Drop location is unoccupied.
      }

      // Generate events.
      if( isValid )
      {
        // Move transport to the target location.
        unloadEvents.add(new MoveEvent(transport, movePath));

        // Debark the passenger.
        unloadEvents.add(new UnloadEvent(transport, passenger, dropLoc));
      }
      else
      {
        // We can't create this action. Leave the event queue empty.
        System.out.println("WARNING! UNLOAD event initialized incorrectly.");
      }
    }

    @Override
    public GameEventQueue getEvents(GameMap map)
    {
      return unloadEvents;
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

      // Resupply action is a bit different from other actions. It can be used as
      // a unit's turn, but it can also be triggered by an APC during the turn-
      // initialization phase (and re-executed each turn). Since its precise
      // effects depend on its circumstances, we wait until the call to
      // getEvents() to generate the events.
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
      XYCoord supplyLocation = null;

      // Validate action.
      boolean isValid = true;
      isValid &= unitActor != null;
      // Unit can move between executions of this action, so verify it's still on the map.
      isValid &= (null != map) && map.isLocationValid(unitActor.x, unitActor.y);
      if( isValid )
      {
        // Figure out where we are acting.
        supplyLocation = myLocation();

        // Add a move event if we need to move.
        // Note that movePath being null is OK for ResupplyAction when it is being re-used.
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
          if( other != null && other.CO == unitActor.CO && !other.isFullySupplied() )
          {

            // Add a re-supply event for this unit.
            eventSequence.add(new ResupplyEvent(other));
          }
        }
      }
      else
      {
        // We can't create any events. Leave the event queue empty.
        System.out.println("WARNING! Attempting to get resupply events for invalid unit.");
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