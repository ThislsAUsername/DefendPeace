package Units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.MassDamageEvent;
import Engine.GamePath.PathNode;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.KaijuWarsKaiju.HellTurkey;
import Units.KaijuWarsKaiju.HellTurkeyLand;
import Units.KaijuWarsKaiju.KaijuStateTracker;
import Units.KaijuWarsKaiju.KaijuUnitModel;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;
import Units.KaijuWarsUnits.Radar;

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
  private static boolean isCrushable(MapLocation location)
  {
    return location.isCaptureable()
        || INERT_CRUSHABLES.contains(location.getEnvironment().terrainType);
  }

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
      //      - [Heal for Big Donk]
      //     [Mass damage]
      //      - [Transform for Hell Turkey]
      //     [Death]
      //   MOVE
      GameEventQueue crushEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != actor && !actor.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
      isValid &= (actor.model instanceof KaijuUnitModel);

      // Generate events.
      if( isValid )
      {
        Utils.enqueueMoveEvent(gameMap, actor, movePath, crushEvents);
        // Remove and discard the move event; we'll be making iterative ones
        crushEvents.clear();

        // Tracks the Kaiju's predicted state as it progresses through the crush events
        UnitContext kaijuState = new UnitContext(gameMap, actor);
        final int startingMP = kaijuState.calculateMovePower();

        // movePath should be updated by the above, so we should be good to go
        for( PathNode node : movePath.getWaypoints() )
        {
          if( kaijuState.movePower < 1 )
            break; // Go only until we run out of move

          enqueueSingleTileEvents(gameMap, startingMP, kaijuState, node, crushEvents);
        } // ~node loop
      }
      return crushEvents;
    }
    public static void enqueueSingleTileEvents(MapMaster gameMap, int startingMP, UnitContext kaijuState, PathNode node, GameEventQueue crushEvents)
    {
      KaijuUnitModel stomperType = (KaijuUnitModel) kaijuState.model;
      final XYCoord destCoord = node.GetCoordinates();
      MapLocation location = gameMap.getLocation(destCoord);

      Unit victim = location.getResident();
      if( null != victim && kaijuState.unit != victim )
      {
        KaijuWarsUnitModel victimType = (KaijuWarsUnitModel) victim.model;
        int counter = 0;
        int stompDamage = victim.getHP();
        if( victimType.isKaiju )
        {
          // When stomping another kaiju, at least one of us dies
          counter += victim.getHP();
          stompDamage = kaijuState.getHP();
        }
        else
        {
          Utils.enqueueDeathEvent(victim, crushEvents);

          // Apply relevant counter damage and slows if the victim is an enemy
          boolean canCounter = victim.CO.isEnemy(kaijuState.CO);

          // Deep Tunnels negate counters, but radar negates it back
          if( canCounter && stomperType.hasDeepTunnelSkill )
            canCounter &= KaijuWarsKaiju.isEnemyRadarScanning(gameMap, destCoord, kaijuState.CO);

          if( canCounter && stomperType.hasRamSkill
              // Ram activates automatically, at range 1 vs valid targets
              && 1 == destCoord.getDistance(kaijuState.unit) && !victimType.resistsKaiju )
          {
            // Apply Ram - you lose its bonus move, but kill the target for free
            canCounter = false;
            --kaijuState.movePower;
          }

          if( canCounter )
          {
            counter = victimType.kaijuCounter;
            counter += KaijuWarsWeapons.getCounterBoost(victim, gameMap, location.getEnvironment().terrainType);
            if( kaijuState.model.isLandUnit()
                && victimType.slowsLand )
              --kaijuState.movePower;
            if( kaijuState.model.isAirUnit()
                && victimType.slowsAir )
              --kaijuState.movePower;
          }
        }
        final boolean isLethal = true;

        ArrayList<Unit> stompable = new ArrayList<>();
        stompable.add(victim);
        crushEvents.add(new MassDamageEvent(kaijuState.CO, stompable, stompDamage, isLethal));
        kaijuState.damageHP(counter);
        if( stompDamage >= victim.getHP() )
          Utils.enqueueDeathEvent(victim, crushEvents);

        // If we're Hell Turkey and we'll drop below the landing threshold, land.
        if( kaijuState.model instanceof HellTurkey &&
            KaijuWarsKaiju.BIRD_LAND_HP >= kaijuState.getHP() )
        {
          HellTurkeyLand devolveToType = ((HellTurkey) kaijuState.model).turkeyLand;
          crushEvents.add(new TransformLifecycle.TransformEvent(kaijuState.unit, devolveToType));
          kaijuState.model = devolveToType;
        }

        crushEvents.add(new MassDamageEvent(victim.CO, Arrays.asList(kaijuState.unit), counter, isLethal));
        // If there's enough counter damage to kill us, die
        if( kaijuState.getHP() < 1 )
        {
          // Kaiju dies at his current position on the path
          Utils.enqueueDeathEvent(kaijuState.unit, kaijuState.coord, true, crushEvents);
          kaijuState.movePower = 0;
        }
        else // Recalculate remaining movement so taking damage can drain movement
        {
          int moveSpent = startingMP - kaijuState.movePower;
          kaijuState.calculateMovePower();
          kaijuState.movePower -= moveSpent;
        }
      }

      // Add property damage and movement if I can reach
      final int distance = kaijuState.coord.getDistance(destCoord);
      if( kaijuState.movePower >= distance )
      {
        if( isCrushable(location) )
        {
          Environment oldEnvirons = location.getEnvironment();
          Environment newEnvirons = Environment.getTile(oldEnvirons.terrainType.getBaseTerrain(), oldEnvirons.weatherType);
          crushEvents.add(new MapChangeEvent(location.getCoordinates(), newEnvirons));
          if( stomperType.regenOnBuildingKill )
            crushEvents.add(new HealUnitEvent(kaijuState.unit, 2, null, true));
          if( stomperType.chargeOnBuildingKill && location.isCaptureable() )
            // TODO: This is definitely wrong
            kaijuState.CO.modifyAbilityPower(1);

          if( Utils.willLoseFromLossOf(gameMap, location) )
          {
            crushEvents.add(new ArmyDefeatEvent(location.getOwner().army));
          }
        }

        kaijuState.movePower -= distance;
        GamePath oneTilePath = new GamePath();
        oneTilePath.addWaypoint(kaijuState.coord);
        oneTilePath.addWaypoint(destCoord);
        Utils.enqueueMoveEvent(gameMap, kaijuState.unit, oneTilePath, crushEvents);
        kaijuState.coord = destCoord;
      }
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

    if( isCrushable(location) )
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
    final Class<?> abilityKey;
    final Unit actor;
    final XYCoord target;
    final boolean destroyBuildings;
    boolean longCooldown = false;

    public KaijuAttackAction(UnitActionFactory pType, Class<?> pKey, Unit unit, XYCoord target, boolean destroyBuildings)
    {
      type = pType;
      abilityKey = pKey;
      actor = unit;
      this.target = target;
      this.destroyBuildings = destroyBuildings;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      if( destroyBuildings )
        for( XYCoord xyc : getTargets(gameMap) )
          enqueueKaijuStrikeEvents(gameMap, actor, xyc, eventSequence);
      else
        for( XYCoord xyc : getTargets(gameMap) )
          enqueueKaijuKillEvents(gameMap, actor, xyc, eventSequence);

      KaijuStateTracker kaijuTracker = StateTracker.instance(gameMap.game, KaijuStateTracker.class);
      // Setting the tracker state here feels wrong
      if( longCooldown )
        kaijuTracker.abilityUsedLong(actor, abilityKey);
      else
        kaijuTracker.abilityUsedShort(actor, abilityKey);

      return eventSequence;
    }

    public List<XYCoord> getTargets(GameMap gameMap)
    {
      List<XYCoord> output = new ArrayList<>();
      output.add(target);
      return output;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();
      String value = destroyBuildings ? "WRECK" : "KILL";

      for( XYCoord xyc : getTargets(gameMap) )
      {
        Unit victim = gameMap.getResident(xyc);
        if( null != victim )
        {
          KaijuWarsUnitModel kjum = (KaijuWarsUnitModel) victim.model;
          if( kjum.resistsKaiju )
            continue; // Kaiju resistance cancels all effects

          output.add(new DamagePopup(xyc, actor.CO.myColor, value));
          continue;
        }

        if( !destroyBuildings )
          continue;

        MapLocation location = gameMap.getLocation(xyc);
        if( !isCrushable(location) )
          continue;

        output.add(new DamagePopup(xyc, actor.CO.myColor, value));
      }

      return output;
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

  // Alphazaurus skills

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
    static final boolean HIT_BUILDINGS = true;
    public AlphaTsunamiAction(AlphaTsunamiFactory pType, Unit unit, XYCoord target)
    {
      super(pType, AlphaTsunamiFactory.class, unit, target, HIT_BUILDINGS);
    }
  }

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
    static final boolean HIT_BUILDINGS = false;
    public AlphaKickAction(AlphaKickFactory pType, Unit unit, XYCoord target)
    {
      super(pType, AlphaKickFactory.class, unit, target, HIT_BUILDINGS);
    }
  }

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
    static final boolean HIT_BUILDINGS = true;
    public AlphaBreathAction(AlphaBreathFactory pType, Unit unit, XYCoord target)
    {
      super(pType, AlphaBreathFactory.class, unit, target, HIT_BUILDINGS);
    }

    @Override
    public List<XYCoord> getTargets(GameMap gameMap)
    {
      // Double the target coordinates and subtract the start coord to get start + offset*2
      XYCoord secondTile = new XYCoord(target.xCoord * 2 - actor.x, target.yCoord * 2 - actor.y);

      List<XYCoord> output = new ArrayList<>();
      output.add(target);
      output.add(secondTile);
      return output;
    }
  }

  // Hell Turkey skills

  public static class BirdResurrectFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    public final UnitModel targetType;
    public BirdResurrectFactory(UnitModel targetType)
    {
      this.targetType = targetType;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      return new GameActionSet(new BirdResurrectAction(this, actor, actCoord), false);
    }

    @Override
    public String name(Unit actor)
    {
      return "RESURRECT";
    }
  } //~Factory
  public static class BirdResurrectAction extends KaijuAttackAction
  {
    BirdResurrectFactory type; // Shadows the superclass's
    static final boolean HIT_BUILDINGS = true;
    /** @param target The unit's location */
    public BirdResurrectAction(BirdResurrectFactory pType, Unit unit, XYCoord target)
    {
      super(pType, BirdResurrectFactory.class, unit, target, HIT_BUILDINGS);
      type = pType;
      longCooldown = true;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue events = new GameEventQueue();
      for( XYCoord xyc : Utils.findLocationsInRange(gameMap, target, 1, 1) )
        enqueueKaijuStrikeEvents(gameMap, actor, xyc, events);
      events.add(new TransformLifecycle.TransformEvent(actor, type.targetType));
      KaijuStateTracker kaijuTracker = StateTracker.instance(gameMap.game, KaijuStateTracker.class);
      // Setting the tracker state here feels wrong
      kaijuTracker.abilityUsedLong(actor, BirdResurrectFactory.class);
      // Hell Turkey revives at 6 HP (originally 5+1 from Swoop), and I don't feel like throwing another event in the pipeline
      actor.health = 60;
      return events;
    }
  } // ~BirdResurrectAction

  public static class BirdSwoopFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, BirdSwoopFactory.class);

      // Act while stationary
      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
        {
          Unit resident = map.getResident(xyc);
          if( null != resident && resident.model.isLandUnit() )
            actions.add(new BirdSwoopAction(this, actor, xyc));
        }
        if( actions.size() > 0 )
          return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "SWOOP";
    }
  } //~Factory
  public static class BirdSwoopAction extends KaijuAttackAction
  {
    static final boolean HIT_BUILDINGS = false;
    public BirdSwoopAction(BirdSwoopFactory pType, Unit unit, XYCoord target)
    {
      super(pType, BirdSwoopFactory.class, unit, target, HIT_BUILDINGS);
    }
  }

  public static class BirdWindForceFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, BirdWindForceFactory.class);

      // Act while stationary
      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
        {
          Unit resident = map.getResident(xyc);
          if( null != resident && resident.model.isAirUnit() )
            actions.add(new BirdWindForceAction(this, actor, xyc));
        }
        if( actions.size() > 0 )
          return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "WIND FORCE";
    }
  } //~Factory
  public static class BirdWindForceAction extends KaijuAttackAction
  {
    static final boolean HIT_BUILDINGS = false;
    public BirdWindForceAction(BirdWindForceFactory pType, Unit unit, XYCoord target)
    {
      super(pType, BirdWindForceFactory.class, unit, target, HIT_BUILDINGS);
    }
  }

  public static class BirdEruptionFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, BirdEruptionFactory.class);

      // Act while stationary
      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        return new GameActionSet(new EruptionAction(this, actor, actCoord), false);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "ERUPTION";
    }
  } //~Factory
  public static class EruptionAction extends KaijuAttackAction
  {
    static final boolean HIT_BUILDINGS = true;
    public EruptionAction(BirdEruptionFactory pType, Unit unit, XYCoord target)
    {
      super(pType, BirdEruptionFactory.class, unit, target, HIT_BUILDINGS);
      longCooldown = true;
    }

    @Override
    public List<XYCoord> getTargets(GameMap gameMap)
    {
      return Utils.findLocationsInRange(gameMap, target, 1, 1);
    }
  }

  // Big Donk skills

  public static class DonkPunchFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, DonkPunchFactory.class);

      // Act while stationary, target ground
      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
        {
          Unit resident = map.getResident(xyc);
          if( null != resident && resident.model.isLandUnit() )
            actions.add(new DonkPunchAction(this, actor, xyc));
        }
        if( actions.size() > 0 )
          return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "PUNCH";
    }
  } //~Factory
  public static class DonkPunchAction extends KaijuAttackAction
  {
    static final boolean HIT_BUILDINGS = true;
    public DonkPunchAction(DonkPunchFactory pType, Unit unit, XYCoord target)
    {
      super(pType, DonkPunchFactory.class, unit, target, HIT_BUILDINGS);
    }

    @Override
    public List<XYCoord> getTargets(GameMap gameMap)
    {
      // Double the target coordinates and subtract the start coord to get start + offset*2
      XYCoord secondTile = new XYCoord(target.xCoord * 2 - actor.x, target.yCoord * 2 - actor.y);

      List<XYCoord> output = new ArrayList<>();
      output.add(target);
      output.add(secondTile);
      return output;
    }
  }

  // Should be fine to just reuse Wind Force, since Kaiju don't share abilities
  public static class DonkClimbFactory extends BirdWindForceFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public String name(Unit actor)
    {
      return "CLIMB";
    }
  } //~Factory

  // UFO skills

  public static class UFOBeamFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, UFOBeamFactory.class);

      if( canAct && movePath.getEndCoord().equals(actCoord) )
      {
        ArrayList<GameAction> actions = new ArrayList<>();
        for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, 1, 1) )
        {
          Unit resident = map.getResident(xyc);
          if( null != resident && resident.CO.isEnemy(actor.CO) )
            actions.add(new UFOBeamAction(this, actor, xyc));
        }
        if( actions.size() > 0 )
          return new GameActionSet(actions, true);
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "BEAM GUN";
    }
  } //~Factory
  public static class UFOBeamAction extends KaijuAttackAction
  {
    static final boolean HIT_BUILDINGS = true;
    public UFOBeamAction(UFOBeamFactory pType, Unit unit, XYCoord target)
    {
      super(pType, UFOBeamFactory.class, unit, target, HIT_BUILDINGS);
      longCooldown = true;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue events = new GameEventQueue();
      enqueueKaijuKillEvents(gameMap, actor, target, events);
      events.add(new MassDamageEvent(actor.CO, Arrays.asList(actor), 1, false));

      KaijuStateTracker kaijuTracker = StateTracker.instance(gameMap.game, KaijuStateTracker.class);
      // Setting the tracker state here feels wrong
      kaijuTracker.abilityUsedShort(actor, UFOBeamFactory.class);
      return events;
    }
  } // ~UFOBeamAction

  public static class UFOEMPFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      final XYCoord actCoord = new XYCoord(actor);
      if(!movePath.getEndCoord().equals(actCoord))
        return null;

      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      boolean canAct = kaijuTracker.isReady(actor, UFOEMPFactory.class);
      // Indicates whether we caught a radar
      boolean canAoE = kaijuTracker.isReady(actor, KaijuWarsKaiju.UFO.class);

      if( canAct )
      {
        if( canAoE )
          return new GameActionSet(new EMPAction(this, actor, actCoord), false);
        else
        {
          ArrayList<GameAction> radarHits = new ArrayList<>();
          for( XYCoord xyc : Utils.findLocationsInRange(map, actCoord, Radar.PIERCING_VISION) )
          {
            Unit resident = map.getResident(xyc);
            if( null != resident && resident.CO.isEnemy(actor.CO)
                && (resident.model instanceof Radar) )
            {
              radarHits.add(new EMPAction(this, actor, xyc));
            }
          }
          if( radarHits.size() > 0 )
            return new GameActionSet(radarHits, true);
          else
            System.out.println("ERROR: Found a radar to disable EMP, but then couldn't find it to target.");
        }
      }

      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "EMP";
    }
  } //~Factory
  public static class EMPAction extends KaijuAttackAction
  {
    static final boolean HIT_BUILDINGS = false;
    public EMPAction(UFOEMPFactory pType, Unit unit, XYCoord target)
    {
      super(pType, UFOEMPFactory.class, unit, target, HIT_BUILDINGS);
    }

    @Override
    public List<XYCoord> getTargets(GameMap gameMap)
    {
      // If targeting myself, AoE
      if( target.equals(new XYCoord(actor)) )
        return Utils.findLocationsInRange(gameMap, target, 1, 1);
      // Otherwise, just wreck the target
      List<XYCoord> output = new ArrayList<>();
      output.add(target);
      return output;
    }
  }

} //~KaijuActions
