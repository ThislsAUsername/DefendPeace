package Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameEvents.BattleEvent;
import Engine.GameEvents.CaptureEvent;
import Engine.GameEvents.CommanderAbilityEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.LoadEvent;
import Engine.GameEvents.MassDamageEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.GameEvents.UnitJoinEvent;
import Engine.GameEvents.UnitTransformEvent;
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
  /**
   * Returns a GameEventQueue with the events that make up this action. If the action
   * was constructed incorrectly, this should return an empty GameEventQueue.
   */
  public abstract GameEventQueue getEvents(MapMaster map);
  public abstract XYCoord getMoveLocation();
  public abstract XYCoord getTargetLocation();
  public abstract UnitActionType getType();

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
      isValid &= (null != gameMap) && (gameMap.isLocationValid(attackLocation)) && gameMap.isLocationValid(moveCoord);
      isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      if( isValid )
      {
        attackRange = Math.abs(moveCoord.xCoord - attackLocation.xCoord)
            + Math.abs(moveCoord.yCoord - attackLocation.yCoord);

        boolean moved = attacker.x != moveCoord.xCoord || attacker.y != moveCoord.yCoord;
        isValid &= (gameMap.getLocation(attackLocation).getResident() == defender);
        isValid &= (null != defender) && attacker.canAttack(defender.model, attackRange, moved);
        isValid &= (null != defender) && attacker.CO.isEnemy(defender.CO);
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
    public String toString()
    {
      return String.format("[Attack %s with %s after moving to %s]",
          defender.toStringWithLocation(), attacker.toStringWithLocation(), moveCoord );
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.ATTACK;
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
    public String toString()
    {
      return String.format("[Produce %s at %s]", what, where);
    }

    @Override
    public UnitActionType getType()
    {
      return null;
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
    public String toString()
    {
      return String.format("[Capture %s at %s with %s]", propertyType, movePathEnd, actor.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.CAPTURE;
    }
  } // ~CaptureAction

  // ===========  WaitAction  =================================
  public static class WaitAction implements GameAction
  {
    private final Path movePath;
    private final XYCoord waitLoc;
    private final Unit actor;

    public WaitAction(Unit unit, Path path)
    {
      actor = unit;
      movePath = path;
      if( (null != path) && (path.getPathLength() > 0) )
      {
        // Store the destination for later.
        waitLoc = new XYCoord(path.getEnd().x, path.getEnd().y);
      }
      else
        waitLoc = null;
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
    public String toString()
    {
      return String.format("[Move %s to %s]", actor.toStringWithLocation(), waitLoc);
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.WAIT;
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
    public String toString()
    {
      return String.format("[Load %s into %s]", passenger.toStringWithLocation(), transport.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.LOAD;
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
    public String toString()
    {
      return String.format("[Unload from %s]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.UNLOAD;
    }
  } // ~UnloadAction

  // ===========  UnitJoinAction  =================================
  // A unit join action will combine a unit into a damaged unit to restore its HP. Any overflow HP is converted back into funds.
  public static class UnitJoinAction implements GameAction
  {
    private Unit donor;
    Path movePath;
    private XYCoord pathEnd = null;
    private Unit recipient;

    public UnitJoinAction(GameMap gameMap, Unit actor, Path path)
    {
      donor = actor;
      movePath = path;
      if( (null != movePath) && (movePath.getPathLength() > 0 ))
      {
        pathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        if( (null != gameMap) && gameMap.isLocationValid(pathEnd) )
        {
          recipient = gameMap.getLocation(pathEnd).getResident();
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // UNITJOIN actions consist of
      //   MOVE
      //   JOIN
      GameEventQueue unitJoinEvents = new GameEventQueue();

      // Validate input
      boolean isValid = true;
      isValid &= (null != donor) && !donor.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
      if( isValid )
      {
        pathEnd = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
        isValid &= gameMap.isLocationValid(pathEnd);

        if( isValid )
        {
          // Find the unit we want to join.
          recipient = gameMap.getLocation(pathEnd).getResident();
          isValid &= (null != recipient) && (recipient.getHP() < recipient.model.maxHP);
        }
      }

      // Create events.
      if( isValid )
      {
        // Move to the recipient, if we don't get blocked.
        if( Utils.enqueueMoveEvent(gameMap, donor, movePath, unitJoinEvents) )
        {
          // Combine forces.
          unitJoinEvents.add(new UnitJoinEvent(donor, recipient));
        }
      }
      return unitJoinEvents;
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
    public String toString()
    {
      return String.format("[Join %s into %s]", donor.toStringWithLocation(), recipient.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.JOIN;
    }
  } // ~UnitJoinAction

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
          // If we should be blocked, don't resupply anything.
          if( !Utils.enqueueMoveEvent(map, unitActor, movePath, eventSequence) )
            isValid = false; // isValid is used to signal pre-emption here rather than a malformed action.
                             // Strange control flow stems from ResupplyAction's dual purpose. 
        }
      }

      if( isValid )
      {
        // Get the adjacent map locations.
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, supplyLocation, 1);

        // For each location, see if there is a friendly unit to re-supply.
        for( XYCoord loc : locations )
        {
          Unit other = map.getLocation(loc).getResident();
          if( other != null && other != unitActor && other.CO == unitActor.CO && !other.isFullySupplied() )
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
    public String toString()
    {
      return String.format("[Resupply units adjacent to %s with %s]", myLocation(), unitActor.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.RESUPPLY;
    }
  } // ~ResupplyAction

  // ===========  RepairUnitAction  ===============================
  public static class RepairUnitAction implements GameAction
  {
    private Path movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    private XYCoord repairCoord;
    Unit benefactor;
    Unit beneficiary;

    public RepairUnitAction(Unit actor, Path path, Unit target)
    {
      benefactor = actor;
      beneficiary = target;
      movePath = path;
      if( benefactor != null && null != beneficiary )
      {
        startCoord = new XYCoord(actor.x, actor.y);
        repairCoord = new XYCoord(target.x, target.y);
      }
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Repair actions consist of
      //   MOVE
      //   HEAL
      //   RESUPPLY
      GameEventQueue repairEvents = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != repairCoord) &&
          gameMap.isLocationValid(startCoord) && gameMap.isLocationValid(repairCoord) )
      {
        isValid &= benefactor != null && !benefactor.isTurnOver;
        isValid &= isValid && null != beneficiary && !benefactor.CO.isEnemy(beneficiary.CO);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, benefactor, movePath, repairEvents) )
        {
          // No surprises in the fog.
          repairEvents.add(new HealUnitEvent(beneficiary, 1, benefactor.CO)); // As this is a unit action, there's no usecase to vary this yet
          repairEvents.add(new ResupplyEvent(beneficiary));
        }
      }
      return repairEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return moveCoord;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return repairCoord;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and heal %s]",
          benefactor.toStringWithLocation(), moveCoord, beneficiary.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.REPAIR_UNIT;
    }
  } // ~RepairUnitAction

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
    public String toString()
    {
      return String.format("[Perform CO Ability %s]", myAbility);
    }

    @Override
    public UnitActionType getType()
    {
      return null;
    }
  } // ~AbilityAction
  
  // ===========  UnitTransformAction  =================================
  /** Effectively a WAIT, but the unit ends up as a different unit at the end of it. */
  public static class TransformAction extends WaitAction
  {
    private UnitActionType.Transform type;
    Unit actor;

    public TransformAction(Unit unit, Path path, UnitActionType.Transform pType)
    {
      super(unit, path);
      type = pType;
      actor = unit;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue transformEvents = super.getEvents(gameMap);
      
      if( transformEvents.size() > 0 ) // if we successfully made a move action
      {
        GameEvent moveEvent = transformEvents.peek();
        if (moveEvent.getEndPoint().equals(getMoveLocation())) // make sure we shouldn't be pre-empted
        {
          transformEvents.add(new UnitTransformEvent(actor, type.destinationType));
        }
      }
      return transformEvents;
    }
    
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and transform to %s]", actor.toStringWithLocation(), getMoveLocation(), type.destinationType);
    }

    @Override
    public UnitActionType getType()
    {
      return type;
    }
  } // ~TransformAction
  
  // ===========  ExplodeAction  =================================
  /** Effectively a WAIT, but the unit explodes at the end of it. */
  public static class ExplodeAction extends WaitAction
  {
    private UnitActionType.Explode type;
    Unit actor;

    public ExplodeAction(Unit unit, Path path, UnitActionType.Explode pType)
    {
      super(unit, path);
      type = pType;
      actor = unit;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue explodeEvents = super.getEvents(gameMap);
      
      if( explodeEvents.size() > 0 ) // if we successfully made a move action
      {
        GameEvent moveEvent = explodeEvents.peek();
        if (moveEvent.getEndPoint().equals(getMoveLocation())) // make sure we shouldn't be pre-empted
        {
          explodeEvents.add(new UnitDieEvent(actor)); // If you explode, you die

          HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
          for (XYCoord coord : Utils.findLocationsInRange(gameMap, getMoveLocation(), type.range))
          {
            Unit victim = gameMap.getLocation(coord).getResident();
            if (null != victim && victim != actor) // Since you're already dead when you explode, you can't get hurt in the explosion
            {
              victims.add(victim);
            }
          }

          explodeEvents.addFirst(new MassDamageEvent(victims, type.damage, false));
          if( actor.CO.units.size() == 1 )
          {
            // CO is out of units. Too bad.
            explodeEvents.add(new CommanderDefeatEvent(actor.CO));
          }
        }
      }
      return explodeEvents;
    }
    
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and explode]", actor.toStringWithLocation(), getMoveLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return type;
    }
  } // ~TransformAction

  // ===========  UnitDeleteAction  =================================
  /** Removes the unit. Only allows deletion in place */
  public static class UnitDeleteAction implements GameAction
  {
    final Unit actor;
    final XYCoord destination;
    
    public UnitDeleteAction(Unit unit)
    {
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      eventSequence.add(new UnitDieEvent(actor));
      // The unit died; check if the Commander is defeated.
      if( actor.CO.units.size() == 1 )
      {
        // CO is out of units. Too bad.
        eventSequence.add(new CommanderDefeatEvent(actor.CO));
      }
      return eventSequence;
    }

    @Override
    public String toString()
    {
      return String.format("[Delete %s in place]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return UnitActionType.DELETE;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return destination;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return destination;
    }
  } // ~UnitDeleteAction
}