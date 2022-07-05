package Units;

import java.util.ArrayList;

import Engine.FloodFillFunctor;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.GamePath.PathNode;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.FloodFillFunctor.BasicMoveFillFunctor;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.MassDamageEvent;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.Environment.Weathers;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;
import Units.MoveTypes.MoveType;

public class KaijuWarsKaiju
{
  /*
   * Kaiju abilities I do not plan to implement at this time

   * Alpha
   *  - Tail Whip (Kill any adjacent unit that isn't on the PATH; not implementing PATH mechanics)
   * Hell Turkey
   *  - Fire trail (Place a crisis on any tile you move over; not implementing PATH mechanics)
   * Duggemundr
   *  - Heat Beam (Kill a unit at 1-2 range and plop down crises in radius 1; not implementing crises)
   * Big Donk
   *  - Wily (-1 damage taken while on forest, +1 move while on forest, move into adjacent forest on building kill; too much jank)
   * Flying Hubcap
   *  - Hologram (After movement, spawn a hologram within 3 tiles, and swap with it half the time; too much jank)
   */

  private static final int KAIJU_COST = 0;
  private static final double STAR_VALUE = 10.0;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MAX_AMMO = -1;
  private static final int VISION_RANGE = 2;
  private static final UnitActionFactory[] KAIJU_ACTIONS = { new KaijuCrushFactory(), UnitActionFactory.DELETE };

  private static final MoveType KAIJU_MOVE = new FootKaiju();

  public static class KaijuUnitModel extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    // How do we do kaiju resurrection?

    public KaijuUnitModel(String pName, long pRole, int pMovePower)
    {
      super(pName, pRole, KAIJU_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, pMovePower, KAIJU_MOVE, KAIJU_ACTIONS, new WeaponModel[0], STAR_VALUE);

      resistsKaiju = true;
      isKaiju      = true;
    }
  }

  public static class Alphazaurus extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | SURFACE_TO_AIR | TROOP | LAND;
    private static final int MOVE_POWER = 2;

    public Alphazaurus()
    {
      super("Alphazaurus", ROLE, MOVE_POWER);
    }
  }
  
  // Movement/basic action stuffs

  public static class FootKaiju extends MoveType
  {
    private static final long serialVersionUID = 1L;

    public FootKaiju()
    {
      super();
      moveCosts.get(Weathers.CLEAR).setAllMovementCosts(1);
      moveCosts.get(Weathers.RAIN).setAllMovementCosts(1);
      moveCosts.get(Weathers.SNOW).setAllMovementCosts(1);
      moveCosts.get(Weathers.SANDSTORM).setAllMovementCosts(1);
    }
    public FootKaiju(FootKaiju other)
    {
      super(other);
    }

    @Override
    public FloodFillFunctor getUnitMoveFunctor(Unit mover, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      return new KaijuMoveFillFunctor(mover, this, includeOccupied, canTravelThroughEnemies);
    }

    @Override
    public MoveType clone()
    {
      return new FootKaiju(this);
    }
  }
  public static class KaijuMoveFillFunctor extends BasicMoveFillFunctor
  {
    public KaijuMoveFillFunctor(Unit mover, MoveType propulsion, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      super(mover, propulsion, includeOccupied, canTravelThroughEnemies);
    }

    @Override
    public int getTransitionCost(GameMap map, XYCoord from, XYCoord to)
    {
      // if we're past the edges of the map
      if( !map.isLocationValid(to) )
        return MoveType.IMPASSABLE;

      int cost = findMoveCost(from, to, map);

      // Cannot path through: Kaiju, buildings
      final MapLocation fromLocation = map.getLocation(from);
      if( !canTravelThroughEnemies
          && fromLocation.isCaptureable()
          && null != unit
          && unit.CO.isEnemy(fromLocation.getOwner()) )
        return MoveType.IMPASSABLE;

      if( null == unit )
        return cost;

      final Unit fromResident = fromLocation.getResident();
      if( null != fromResident && fromResident.CO.isEnemy(unit.CO) )
      {
        final KaijuWarsUnitModel fromResidentType = (KaijuWarsUnitModel) fromResident.model;
        if( !canTravelThroughEnemies && null != fromResidentType )
        {
          if( fromResidentType.isKaiju )
            cost = MoveType.IMPASSABLE;
        }
      }

      final Unit toResident = map.getLocation(to).getResident();
      if( null != toResident && toResident.CO.isEnemy(unit.CO) )
      {
        final KaijuWarsUnitModel toResidentType = (KaijuWarsUnitModel) toResident.model;
        if( !canTravelThroughEnemies && null != toResidentType )
        {
          if( unit.model.isLandUnit()
              && toResidentType.slowsLand )
            cost += 1;
          if( unit.model.isAirUnit()
              && toResidentType.slowsAir )
            cost += 1;
        }
      }

      return cost;
    }

    @Override
    public boolean canEnd(GameMap map, XYCoord end)
    {
      return true;
    }
  } // ~KaijuMoveFillFunctor


  public static class KaijuCrushFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      {
        return new GameActionSet(new KaijuCrushAction(actor, movePath, this), false);
      }
    }

    @Override
    public String name(Unit actor)
    {
      return "CRUSH";
    }
  } //~Factory

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
      // WAIT actions consist of
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
          if( location.isCaptureable() )
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

}
