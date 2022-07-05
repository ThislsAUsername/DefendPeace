package Units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.MassDamageEvent;
import Engine.GamePath.PathNode;
import Engine.StateTrackers.StateTracker;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.KaijuWarsKaiju.KaijuStateTracker;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;

public class KaijuActions
{
  public static class KaijuCrushFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      return new GameActionSet(new KaijuCrushAction(actor, movePath, this), false);
    }

    @Override
    public String name(Unit actor)
    {
      return "CRUSH";
    }
  } //~Factory

  /** List of things Kaiju break that aren't capturable */
  public static final List<TerrainType> INERT_CRUSHABLES = Arrays.asList(TerrainType.BRIDGE, TerrainType.BUNKER, TerrainType.PILLAR, TerrainType.METEOR);

  public static class KaijuCrushAction extends GameAction
  {
    private final KaijuCrushFactory type;
    private final GamePath movePath;
    private final XYCoord waitLoc;
    private final Unit actor;

    public KaijuCrushAction(Unit unit, GamePath path, KaijuCrushFactory pType)
    {
      type = pType;
      actor = unit;
      movePath = path;
      if( (null != path) && (path.getPathLength() > 0) )
      {
        // Store the destination for later.
        waitLoc = movePath.getEndCoord();
      }
      else
        waitLoc = null;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // CRUSH actions consist of
      //   sequence:
      //     [Terrain destruction]
      //     [Mass damage]
      //     [Death]
      //   MOVE
      GameEventQueue crushEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);

      // Generate events.
      if( isValid )
      {
        boolean actorDies = false;
        Utils.enqueueMoveEvent(gameMap, actor, movePath, crushEvents);
        // Remove the move event so it can go last; this allows us to clear out stuff we step on first
        GameEvent moveEvent = crushEvents.pop();

        // For taking counter damage
        ArrayList<Unit> walkingKaiju = new ArrayList<>();
        walkingKaiju.add(actor);
        int totalCounter = 0;

        // movePath should be updated by the above, so we should be good to go
        for( PathNode node : movePath.getWaypoints() )
        {
          MapLocation location = gameMap.getLocation(node.GetCoordinates());
          if( location.isCaptureable()
              || INERT_CRUSHABLES.contains(location.getEnvironment().terrainType) )
          {
            Environment oldEnvirons = location.getEnvironment();
            Environment newEnvirons = Environment.getTile(oldEnvirons.terrainType.getBaseTerrain(), oldEnvirons.weatherType);
            crushEvents.add(new MapChangeEvent(location.getCoordinates(), newEnvirons));

            if( Utils.willLoseFromLossOf(gameMap, location) )
            {
              crushEvents.add(new ArmyDefeatEvent(location.getOwner().army));
            }
          }
          Unit victim = location.getResident();
          if( null != victim && actor != victim )
          {
            KaijuWarsUnitModel kjum = (KaijuWarsUnitModel) victim.model;
            int counter = kjum.kaijuCounter;
            int stompDamage = victim.getHP();
            if( kjum.isKaiju )
            {
              // When stomping another kaiju, at least one of us dies
              counter += victim.getHP();
              stompDamage = actor.getHP() - totalCounter;
            }
            else
            {
              Utils.enqueueDeathEvent(victim, crushEvents);
              counter += KaijuWarsWeapons.getCounterBoost(victim, gameMap, location.getEnvironment().terrainType);
            }
            final boolean isLethal = true;

            ArrayList<Unit> stompable = new ArrayList<>();
            stompable.add(victim);
            crushEvents.add(new MassDamageEvent(actor.CO, stompable, stompDamage, isLethal));
            if( stompDamage >= victim.getHP() )
              Utils.enqueueDeathEvent(victim, crushEvents);

            crushEvents.add(new MassDamageEvent(victim.CO, walkingKaiju, counter, isLethal));
            totalCounter += counter;
            if(totalCounter >= actor.getHP())
            {
              // Kaiju has to die at his starting position, since the move event happens at the end
              Utils.enqueueDeathEvent(actor, crushEvents);
              actorDies = true;
              break;
            }
          }
        }

        if( !actorDies )
          crushEvents.add(moveEvent);
      }
      return crushEvents;
    }

    @Override
    public Unit getActor()
    {
      return actor;
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
      return String.format("[Kaiju-move %s to %s]", actor.toStringWithLocation(), waitLoc);
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~KaijuCrushAction


  /**
   * Destroy both terrain and any unit on this spot, assuming there's no Kaiju-resistor
   * <p>Designed for Kaiju ability actions
   */
  public static void enqueueKaijuStrikeEvents(MapMaster gameMap, Unit kaiju, XYCoord xyc, GameEventQueue events)
  {
    if( enqueueKaijuKillEvents(gameMap, kaiju, xyc, events) )
      return; // Kaiju resistance cancels all events

    MapLocation location = gameMap.getLocation(xyc);

    if( location.isCaptureable()
        || INERT_CRUSHABLES.contains(location.getEnvironment().terrainType) )
    {
      Environment oldEnvirons = location.getEnvironment();
      Environment newEnvirons = Environment.getTile(oldEnvirons.terrainType.getBaseTerrain(), oldEnvirons.weatherType);
      events.add(new MapChangeEvent(location.getCoordinates(), newEnvirons));

      if( Utils.willLoseFromLossOf(gameMap, location) )
      {
        events.add(new ArmyDefeatEvent(location.getOwner().army));
      }
    }
  }
  /**
   * Destroy a non-Kaiju-resistor on this spot
   * @return Whether there was a Kaiju-resistor
   */
  public static boolean enqueueKaijuKillEvents(MapMaster gameMap, Unit kaiju, XYCoord xyc, GameEventQueue events)
  {
    MapLocation location = gameMap.getLocation(xyc);

    Unit victim = location.getResident();
    if( null != victim )
    {
      KaijuWarsUnitModel kjum = (KaijuWarsUnitModel) victim.model;
      if( kjum.resistsKaiju )
        return true; // Kaiju resistance cancels all events
      else
      {
        ArrayList<Unit> stompable = new ArrayList<>();
        stompable.add(victim);
        events.add(new MassDamageEvent(kaiju.CO, stompable, victim.getHP(), true));
        Utils.enqueueDeathEvent(victim, events);
      }
    }

    return false;
  }

  /** Superclass for Kaiju attack ability actions */
  public abstract static class KaijuAttackAction extends GameAction
  {
    final UnitActionFactory type;
    final Unit actor;
    final XYCoord target;

    public KaijuAttackAction(UnitActionFactory pType, Unit unit, XYCoord target)
    {
      type = pType;
      actor = unit;
      this.target = target;
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public String toString()
    {
      return String.format("[%s %s on %s]", actor.toStringWithLocation(), type, target);
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return new XYCoord(actor);
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return target;
    }
  } // ~KaijuAttackAction


  public static class AlphaTsunamiFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, AlphaTsunamiFactory.class);

      // Act while stationary in water
      if( canAct && movePath.getEndCoord().equals(actCoord) && map.getEnvironment(actCoord).terrainType.isWater() )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
          actions.add(new AlphaTsunamiAction(this, actor, xyc));
        return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "TSUNAMI";
    }
  } //~Factory

  public static class AlphaTsunamiAction extends KaijuAttackAction
  {
    public AlphaTsunamiAction(AlphaTsunamiFactory pType, Unit unit, XYCoord target)
    {
      super(pType, unit, target);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      enqueueKaijuStrikeEvents(gameMap, actor, target, eventSequence);
      KaijuStateTracker kaijuTracker = StateTracker.instance(gameMap.game, KaijuStateTracker.class);
      kaijuTracker.abilityUsedShort(actor, AlphaTsunamiFactory.class);
      return eventSequence;
    }
  } // ~AlphaTsunamiAction

  public static class AlphaKickFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, AlphaKickFactory.class);

      // Act while stationary
      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
        {
          Unit resident = map.getResident(xyc);
          if( null != resident && resident.model.isLandUnit() )
            actions.add(new AlphaKickAction(this, actor, xyc));
        }
        if( actions.size() > 0 )
          return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "KICK";
    }
  } //~Factory

  public static class AlphaKickAction extends KaijuAttackAction
  {
    public AlphaKickAction(AlphaKickFactory pType, Unit unit, XYCoord target)
    {
      super(pType, unit, target);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      enqueueKaijuKillEvents(gameMap, actor, target, eventSequence);
      KaijuStateTracker kaijuTracker = StateTracker.instance(gameMap.game, KaijuStateTracker.class);
      kaijuTracker.abilityUsedShort(actor, AlphaKickFactory.class);
      return eventSequence;
    }
  } // ~AlphaKickAction

  public static class AlphaBreathFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, AlphaBreathFactory.class);

      // Act while stationary
      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
          actions.add(new AlphaBreathAction(this, actor, xyc));
        return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "BREATH";
    }
  } //~Factory

  public static class AlphaBreathAction extends KaijuAttackAction
  {
    public AlphaBreathAction(AlphaBreathFactory pType, Unit unit, XYCoord target)
    {
      super(pType, unit, target);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      enqueueKaijuStrikeEvents(gameMap, actor, target, eventSequence);
      // Double the target coordinates and subtract the start coord to get start + offset*2
      XYCoord secondTile = new XYCoord(target.xCoord * 2 - actor.x, target.yCoord * 2 - actor.y);
      enqueueKaijuStrikeEvents(gameMap, actor, secondTile, eventSequence);
      KaijuStateTracker kaijuTracker = StateTracker.instance(gameMap.game, KaijuStateTracker.class);
      kaijuTracker.abilityUsedShort(actor, AlphaBreathFactory.class);
      return eventSequence;
    }
  } // ~AlphaBreathAction


} //~KaijuActions
