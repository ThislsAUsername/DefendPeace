package Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;
import Engine.GameEvents.CommanderAbilityEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.LoadEvent;
import Engine.GameEvents.MoveEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.GameEvents.UnloadEvent;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 * Provides an interface for all in-game actions.
 */
public interface GameAction
{
  public enum ActionType
  {
    ATTACK, CAPTURE, LOAD, RESUPPLY, UNLOAD, WAIT, UNITPRODUCTION, OTHER
  }

  /**
   * Returns a GameEventQueue with the events that make up this action. If the action
   * was constructed incorrectly, this should return an empty GameEventQueue.
   */
  public abstract GameEventQueue getEvents(MapMaster map);
  public abstract XYCoord getMoveLocation();
  public abstract XYCoord getTargetLocation();
  public abstract ActionType getType();

  // ==========================================================
  //   Concrete Action type classes.
  // ==========================================================

  // ===========  AttackAction  ===============================
  public static class AttackAction implements GameAction
  {
    private Path movePath;
    private XYCoord moveCoord = null;
    private XYCoord attackLocation = null;
    private Unit attacker;
    private Unit defender;

    public AttackAction(GameMap gameMap, Unit actor, Path path, int targetX, int targetY)
    {
      this(gameMap, actor, path, new XYCoord(targetX, targetY));
    }

    public AttackAction(GameMap gameMap, Unit actor, Path path, XYCoord atkLoc)
    {
      movePath = path;
      attacker = actor;
      attackLocation = atkLoc;
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        if((null != atkLoc) && (null != gameMap) && gameMap.isLocationValid(atkLoc))
        {
          defender = gameMap.getLocation(atkLoc).getResident();
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // ATTACK actions consist of
      //   MOVE
      //   BATTLE
      //   [DEATH]
      //   [DEFEAT]
      GameEventQueue attackEvents = new GameEventQueue();

      // Validate input.
      int attackRange = -1;
      boolean isValid = true;
      isValid &= attacker != null && !attacker.isTurnOver;
      isValid &= (null != gameMap) && (gameMap.isLocationValid(attackLocation));
      isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      if( isValid )
      {
        XYCoord moveCoord = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        Location moveLocation = gameMap.getLocation(moveCoord);
        defender = gameMap.getLocation(attackLocation).getResident();
        attackRange = Math.abs(moveCoord.xCoord - attackLocation.xCoord)
            + Math.abs(moveCoord.yCoord - attackLocation.yCoord);

        boolean moved = attacker.x != moveCoord.xCoord || attacker.y != moveCoord.yCoord;
        isValid &= (null != defender) && attacker.canAttack(defender.model, attackRange, moved);
        isValid &= attacker.CO.isEnemy(defender.CO);
        isValid &= (null == moveLocation.getResident()) || (attacker == moveLocation.getResident());
      }

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, attacker, movePath, attackEvents) )
        {
          // No surprises in the fog. Resolve combat.
          BattleEvent event = new BattleEvent(attacker, defender, moveCoord.xCoord, moveCoord.yCoord, gameMap);
          attackEvents.add(event);

          if( event.attackerDies() )
          {
            attackEvents.add(new UnitDieEvent(event.getAttacker()));

            // Since the attacker died, see if he has any friends left.
            if( attacker.CO.units.size() == 1 )
            {
              // CO is out of units. Too bad.
              attackEvents.add(new CommanderDefeatEvent(event.getAttacker().CO));
            }
          }
          if( event.defenderDies() )
          {
            attackEvents.add(new UnitDieEvent(event.getDefender()));

            // The defender died; check if the Commander is defeated.
            if( defender.CO.units.size() == 1 )
            {
              // CO is out of units. Too bad.
              attackEvents.add(new CommanderDefeatEvent(event.getDefender().CO));
            }
          }
        }
      }
      return attackEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveCoord;
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

    @Override
    public String toString()
    {
      return String.format("[Attack %s with %s after moving to %s]",
          defender.toStringWithLocation(), attacker.toStringWithLocation(), moveCoord );
    }
  } // ~AttackAction

  // ===========  UnitProductionAction  ==============================
  public static class UnitProductionAction implements GameAction
  {
    private final XYCoord where;
    private final Commander who;
    private final UnitModel what;

    public UnitProductionAction(Commander who, UnitModel what, XYCoord where)
    {
      this.where = where;
      this.who = who;
      this.what = what;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // BUILDUNIT actions consist of
      //   TODO: Consider introducing TRANSFERFUNDS for the fiscal part.
      //   CREATEUNIT
      GameEventQueue buildEvents = new GameEventQueue();

      // Validate events.
      boolean isValid = true;
      isValid &= (null != gameMap) && (null != who) && (null != what) && (null != where);
      if( isValid )
      {
        Location site = gameMap.getLocation(where);
        isValid &= (null == site.getResident());
        isValid &= site.getOwner() == who;
        isValid &= who.getShoppingList(site).contains(what);
        isValid &= (who.money >= what.getCost());
      }

      if( isValid )
      {
        //buildEvents.add(new TransferFundsEvent(who, what.moneyCost));
        buildEvents.add(new CreateUnitEvent(who, what, where));
      }
      else
      {
        // We can't create this action. Leave the event queue empty.
        System.out.println("WARNING! BuildUnitAction created with invalid arguments.");
      }
      return buildEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return where;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return where;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.UNITPRODUCTION;
    }

    @Override
    public String toString()
    {
      return String.format("[Produce %s at %s]", what, where);
    }
  } // ~UnitProductionAction

  // ===========  CaptureAction  ==============================
  public static class CaptureAction implements GameAction
  {
    private Unit actor = null;
    private Path movePath;
    private XYCoord movePathEnd;
    private TerrainType propertyType;

    public CaptureAction(GameMap gameMap, Unit unit, Path path)
    {
      actor = unit;
      movePath = path;
      if( (null != path) && path.getPathLength() > 0 )
      {
        movePathEnd = new XYCoord(path.getEnd().x, path.getEnd().y);
      }
      if( (null != gameMap) && gameMap.isLocationValid(movePathEnd))
      {
        propertyType = gameMap.getLocation(movePathEnd).getEnvironment().terrainType;
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      // CAPTURE actions consist of
      //   MOVE
      //   CAPTURE
      //   [DEFEAT]
      GameEventQueue captureEvents = new GameEventQueue();

      // Validate input
      Location captureLocation = null;
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver; // Valid unit
      isValid &= null != map; // Valid map
      isValid &= (null != movePath) && (movePath.getPathLength() > 0); // Valid path
      if( isValid )
      {
        movePathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        captureLocation = map.getLocation(movePathEnd);
        isValid &= captureLocation.isCaptureable(); // Valid location
        isValid &= actor.CO.isEnemy(captureLocation.getOwner()); // Valid CO
        isValid &= (null == captureLocation.getResident()) || (actor == captureLocation.getResident());
      }

      // Generate events
      if( isValid )
      {
        // Move to the target location.
        if( Utils.enqueueMoveEvent(map, actor, movePath, captureEvents))
        {
          // Attempt to capture.
          CaptureEvent capture = new CaptureEvent(actor, map.getLocation(movePathEnd));
          captureEvents.add(capture);

          if( capture.willCapture() ) // If this will succeed, check if the CO will lose as a result.
          {
            // Check if capturing this property will cause someone's defeat.
            if( (propertyType == TerrainType.HEADQUARTERS) && (null != captureLocation.getOwner()) )
            {
              // Someone is losing their big, comfy chair.
              CommanderDefeatEvent defeat = new CommanderDefeatEvent(captureLocation.getOwner());
              defeat.setPropertyBeneficiary(actor.CO);
              captureEvents.add(defeat);
            }
          }
        }
      }
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

    @Override
    public String toString()
    {
      return String.format("[Capture %s at %s with %s]", propertyType, movePathEnd, actor.toStringWithLocation());
    }
  } // ~CaptureAction

  // ===========  WaitAction  =================================
  public static class WaitAction implements GameAction
  {
    private Path movePath;
    private XYCoord waitLoc = null;
    private Unit actor = null;

    public WaitAction(Unit unit, Path path)
    {
      actor = unit;
      movePath = path;
      if( (null != path) && (path.getPathLength() > 0) )
      {
        // Store the destination for later.
        waitLoc = new XYCoord(path.getEnd().x, path.getEnd().y);
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // WAIT actions consist of
      //   MOVE
      GameEventQueue waitEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
      if( isValid )
      {
        XYCoord movePathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        Location moveLocation = gameMap.getLocation(movePathEnd);
        isValid &= (null == moveLocation.getResident()) || (actor == moveLocation.getResident());
      }

      // Generate events.
      if( isValid )
      {
        Utils.enqueueMoveEvent(gameMap, actor, movePath, waitEvents);
      }
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

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s]", actor.toStringWithLocation(), waitLoc);
    }
  } // ~WaitAction

  // ===========  LoadAction  =================================
  public static class LoadAction implements GameAction
  {
    private Unit passenger;
    Path movePath;
    private XYCoord pathEnd = null;
    private Unit transport;

    public LoadAction(GameMap gameMap, Unit actor, Path path)
    {
      passenger = actor;
      movePath = path;
      if( (null != movePath) && (movePath.getPathLength() > 0 ))
      {
        pathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        if( (null != gameMap) && gameMap.isLocationValid(pathEnd) )
        {
          transport = gameMap.getLocation(pathEnd).getResident();
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // LOAD actions consist of
      //   MOVE
      //   LOAD
      GameEventQueue loadEvents = new GameEventQueue();

      // Validate input
      boolean isValid = true;
      isValid &= (null != passenger) && !passenger.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
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

      // Create events.
      if( isValid )
      {
        // Move to the transport, if we don't get blocked.
        if( Utils.enqueueMoveEvent(gameMap, passenger, movePath, loadEvents) )
        {
          // Get in the transport.
          loadEvents.add(new LoadEvent(passenger, transport));
        }
      }
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

    @Override
    public String toString()
    {
      return String.format("[Load %s into %s]", passenger.toStringWithLocation(), transport.toStringWithLocation());
    }
  } // ~LoadAction

  // ===========  UnloadAction  =================================
  public static class UnloadAction implements GameAction
  {
    private Unit actor;
    private Path movePath;
    private XYCoord moveLoc;
    private Map<Unit, XYCoord> myDropoffs;
    private XYCoord firstDropLoc;

    public UnloadAction(GameMap gameMap, Unit actor, Path path, Unit passenger, int dropX, int dropY)
    {
      this(actor, path, passenger, new XYCoord(dropX, dropY));
    }

    public UnloadAction(Unit transport, Path movePath, final Unit passenger, final XYCoord dropLocation)
    {
      this(transport, movePath, new HashMap<Unit, XYCoord>(){
          private static final long serialVersionUID = 1L;
          {
            this.put(passenger, dropLocation);
          }
        });
    }

    public UnloadAction(Unit transport, Path path, Map<Unit, XYCoord> dropoffs)
    {
      actor = transport;
      movePath = path;
      myDropoffs = dropoffs;

      // Grab the move location and the first drop location to support getMoveLocation and getTargetLocation.
      if( (null != movePath) && (movePath.getPathLength() > 0 ))
      {
        moveLoc = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      }
      if( !myDropoffs.isEmpty() )
      {
        for( XYCoord coord : myDropoffs.values() )
        {
          firstDropLoc = coord;
          break;
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // UNLOAD actions consist of
      //   MOVE (transport)
      //   UNLOAD
      //   [UNLOAD]*
      GameEventQueue unloadEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver;
      isValid &= null != myDropoffs && !myDropoffs.isEmpty();
      isValid &= movePath.getPathLength() > 0;
      isValid &= null != gameMap;
      if( isValid )
      {
        isValid &= !actor.heldUnits.isEmpty();
        for( Unit cargo : myDropoffs.keySet() ) // Make sure the cargo can go where we want to put it.
        {
          isValid &= cargo.model.propulsion.canTraverse(gameMap.getEnvironment(myDropoffs.get(cargo)));
        }
        for( XYCoord coord : myDropoffs.values() ) // Make sure nobody's there already.
        {
          Unit res = gameMap.getLocation(coord).getResident();
          isValid &= (null == res) || (res == actor); // Except the transport, because it must have moved anyway.
        }
      }

      // Generate events.
      if( isValid )
      {
        // Attempt to move the transport to the target location.
        if( Utils.enqueueMoveEvent(gameMap, actor, movePath, unloadEvents) )
        {
          // Debark the passengers. Unload all passengers you can, regardless of order.
          for( Unit unit : myDropoffs.keySet() )
          {
            XYCoord dropXY = myDropoffs.get(unit);
            if( gameMap.isLocationEmpty(actor, dropXY) )
            {
              unloadEvents.add(new UnloadEvent(actor, unit, myDropoffs.get(unit)));
            }
          }
        }
      }
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
      return firstDropLoc;
    }

    @Override
    public ActionType getType()
    {
      return GameAction.ActionType.UNLOAD;
    }

    @Override
    public String toString()
    {
      return String.format("[Unload from %s]", actor.toStringWithLocation());
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
      // initialization phase (and re-executed each turn).
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
    public GameEventQueue getEvents(MapMaster map)
    {
      // RESUPPLY actions consist of
      //   [MOVE]
      //   RESUPPLY
      GameEventQueue eventSequence = new GameEventQueue();
      XYCoord supplyLocation = null;

      // Validate action.
      boolean isValid = true;
      isValid &= unitActor != null && !unitActor.isTurnOver;
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

    @Override
    public String toString()
    {
      return String.format("[Resupply units adjacent to %s with %s]", myLocation(), unitActor.toStringWithLocation());
    }
  } // ~ResupplyAction

  // ===========  AbilityAction  =================================
  public static class AbilityAction implements GameAction
  {
    private GameEventQueue abilityEvents = null;
    private CommanderAbility myAbility;

    public AbilityAction(CommanderAbility ability)
    {
      // ABILITY actions consist of
      //   ABILITY
      myAbility = ability;
      boolean isValid = null != myAbility;
      isValid &= myAbility.myCommander.getReadyAbilities().contains(myAbility);
      if( isValid )
      {
        abilityEvents = new GameEventQueue();
        abilityEvents.add(new CommanderAbilityEvent(myAbility));
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      return abilityEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return null;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return null;
    }

    @Override
    public ActionType getType()
    {
      // Use OTHER, just because it doesn't correspond to a normal unit-based
      // action with an actor, target location, etc.
      return GameAction.ActionType.OTHER;
    }

    @Override
    public String toString()
    {
      return String.format("[Perform CO Ability %s]", myAbility);
    }
  } // ~AbilityAction
}