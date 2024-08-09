package Engine.UnitActionLifecycles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext.CalcType;
import Engine.Combat.CombatEngine;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import UI.InputOptionsController;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitContext;
import Units.WeaponModel;

public abstract class BattleLifecycle
{
  public static class BattleFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    // Provide an invalid FIRE option on selecting an action when:
    // Weapon could attack, but nothing's in range
    // Weapon is out of ammo
    @Override
    public GameActionSet getGUIActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      GameActionSet realActions = getPossibleActions(map, movePath, actor, ignoreResident);
      if( null == realActions )
      {
        boolean moved = !movePath.getEndCoord().equals(actor.x, actor.y);
        boolean insertFake = false;
        for( WeaponModel wpn : actor.model.weapons )
        {
          if( wpn.loaded(actor) && wpn.rangeMax() == 1 )
            continue;
          insertFake |= ( !moved || wpn.canFireAfterMoving() );
        } // ~Weapon loop
        if( insertFake )
        {
          GameActionSet fakeAction = new GameActionSet(new BattleAction(map, actor, movePath, new XYCoord(actor)), true);
          fakeAction.isInvalidChoice = true;
          return fakeAction;
        }
      }
      return realActions;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        // Evaluate attack options.
        {
          boolean moved = !moveLocation.equals(actor.x, actor.y);
          HashSet<XYCoord> allWeaponTargets = new HashSet<XYCoord>();
          for( WeaponModel wpn : actor.model.weapons )
          {
            // Evaluate this weapon for targets if it has ammo, and if either the weapon
            // is mobile or we don't care if it's mobile (because we aren't moving).
            if( wpn.loaded(actor) && (!moved || wpn.canFireAfterMoving()) )
            {
              UnitContext uc = new UnitContext(map, actor, wpn, movePath, moveLocation);
              ArrayList<XYCoord> locations = Utils.findTargetsInRange(map, uc);

              allWeaponTargets.addAll(locations);
            }
          } // ~Weapon loop

          // Only add this action set if we actually have a target
          if( !allWeaponTargets.isEmpty() )
          {
            ArrayList<GameAction> attackOptions = new ArrayList<GameAction>();
            for( XYCoord loc : allWeaponTargets )
            {
              if( null != map.getLocation(loc).getResident() )
                attackOptions.add(new BattleAction(map, actor, movePath, loc));
              else
                attackOptions.add(new DemolitionAction(map, actor, movePath, loc));
            }
            // Bundle our attack options into an action set
            return new GameActionSet(attackOptions);
          }
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "ATTACK";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return ATTACK;
    }
  } // ~Factory

  public static class BattleAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord moveCoord = null;
    private XYCoord attackLocation = null;
    private Unit attacker;
    private Unit defender;

    public BattleAction(GameMap gameMap, Unit actor, GamePath path, int targetX, int targetY)
    {
      this(gameMap, actor, path, new XYCoord(targetX, targetY));
    }

    public BattleAction(GameMap gameMap, Unit actor, GamePath path, XYCoord atkLoc)
    {
      movePath = path;
      attacker = actor;
      attackLocation = atkLoc;
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = movePath.getEndCoord();
        if( (null != atkLoc) && (null != gameMap) && gameMap.isLocationValid(atkLoc) )
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
      //   [DEATH]  If the newly-deceased unit is a loaded transport.
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
        attackRange = Math.abs(moveCoord.x - attackLocation.x) + Math.abs(moveCoord.y - attackLocation.y);

        boolean moved = attacker.x != moveCoord.x || attacker.y != moveCoord.y;
        isValid &= (gameMap.getLocation(attackLocation).getResident() == defender);
        isValid &= (null != defender) && attacker.canAttack(gameMap, defender.model, attackRange, moved);
        isValid &= (null != defender) && attacker.CO.isEnemy(defender.CO);
      }

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, attacker, movePath, attackEvents) )
        {
          // No surprises in the fog. Resolve combat.
          BattleEvent event = new BattleEvent(attacker, defender, movePath, gameMap);
          attackEvents.add(event);

          if( event.attackerDies() )
          {
            Utils.enqueueDeathEvent(event.getAttacker(), moveCoord, true, attackEvents);
          }
          if( event.defenderDies() )
          {
            Utils.enqueueDeathEvent(event.getDefender(), attackEvents);
          }
        }
      }
      return attackEvents;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      // This feels like an uggo hack, but this would be kind of out of place as a parameter
      CalcType calcType = CalcType.values()[InputOptionsController.damagePreviewTypeOption.getSelectedObject().ordinal()];
      BattleSummary summary = CombatEngine.simulateBattleResults(attacker, defender, map, movePath, calcType);

      int attackerHealthLoss = summary.attacker.getPreciseHealthDamage();
      int defenderHealthLoss = summary.defender.getPreciseHealthDamage();
      // output any damage done, with the color of the one dealing the damage
      if( attackerHealthLoss > 0 )
        output.add(new DamagePopup(movePath.getWaypoint(0).GetCoordinates(), defender.CO.myColor, attackerHealthLoss + "%"));
      if( defenderHealthLoss > 0 )
        output.add(new DamagePopup(attackLocation, attacker.CO.myColor, defenderHealthLoss + "%"));

      return output;
    }

    @Override
    public Unit getActor()
    {
      return attacker;
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
      return String.format("[Attack %s with %s after moving to %s]", defender.toStringWithLocation(),
          attacker.toStringWithLocation(), moveCoord);
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.ATTACK;
    }
  } // ~BattleAction

  public static class DemolitionAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord moveCoord = null;
    private XYCoord attackLocation = null;
    private Unit attacker;
    private MapLocation target;

    public DemolitionAction(GameMap gameMap, Unit actor, GamePath path, XYCoord atkLoc)
    {
      movePath = path;
      attacker = actor;
      attackLocation = atkLoc;
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = movePath.getEndCoord();
        if( (null != atkLoc) && (null != gameMap) && gameMap.isLocationValid(atkLoc) )
        {
          target = gameMap.getLocation(atkLoc);
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // ATTACK actions consist of
      //   MOVE
      //   BATTLE
      //   [MAP CHANGE]
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
        attackRange = Math.abs(moveCoord.x - attackLocation.x) + Math.abs(moveCoord.y - attackLocation.y);

        boolean moved = attacker.x != moveCoord.x || attacker.y != moveCoord.y;
        isValid &= (null != target) && attacker.canAttack(gameMap, target, attackRange, moved);
      }

      if( isValid )
      {
        target = gameMap.getLocation(attackLocation); // Re-assign with the real location instance
        if( Utils.enqueueMoveEvent(gameMap, attacker, movePath, attackEvents) )
        {
          // No surprises in the fog. Resolve combat.
          DemolitionEvent event = new DemolitionEvent(attacker, target, movePath, gameMap);
          attackEvents.add(event);

          if( event.demolitionFinishes() )
          {
            Environment oldEnvirons = target.getEnvironment();
            Environment newEnvirons = Environment.getTile(oldEnvirons.terrainType.getBaseTerrain(), oldEnvirons.weatherType);
            attackEvents.add(new MapChangeEvent(target.getCoordinates(), newEnvirons));

            if( Utils.willLoseFromLossOf(gameMap, target) )
            {
              attackEvents.add(new ArmyDefeatEvent(target.getOwner().army));
            }
          }
        }
      }
      return attackEvents;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      StrikeParams result = CombatEngine.calculateTerrainDamage(attacker, movePath, target, map);

      // output any damage done, with the color of the one dealing the damage
      if( result.calculateDamage() > 0 )
        // grab the two most significant digits and convert to %
        output.add(new DamagePopup(attackLocation, attacker.CO.myColor, result.calculateDamage() + "%"));

      return output;
    }

    @Override
    public Unit getActor()
    {
      return attacker;
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
      return String.format("[Attack %s with %s after moving to %s]", target.toStringWithLocation(),
          attacker.toStringWithLocation(), moveCoord);
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.ATTACK;
    }
  } // ~DemolitionAction

  /**
   * BattleEvent handles a single battle event between two units. The outcome is
   * calculated as soon as the event is created; the result is applied when
   * performEvent is called. The results can be seen via the provided functions.
   */
  public static class BattleEvent implements GameEvent
  {
    private final BattleSummary battleInfo;
    private final XYCoord defenderCoords;

    public BattleEvent(Unit attacker, Unit defender, GamePath path, MapMaster map)
    {
      UnitContext attackerContext = new UnitContext(attacker);
      attackerContext.setPath(path);
      UnitContext defenderContext = new UnitContext(defender);
      // Calculate the result of the battle immediately. This will allow us to plan the animation.
      battleInfo = CombatEngine.calculateBattleResults(attackerContext, defenderContext, map);
      defenderCoords = new XYCoord(defender.x, defender.y);
    }

    public Unit getAttacker()
    {
      return battleInfo.attacker.unit;
    }
    public boolean attackerDies()
    {
      return battleInfo.attacker.after.getHealth() <= 0;
    }

    public Unit getDefender()
    {
      return battleInfo.defender.unit;
    }
    public boolean defenderDies()
    {
      return battleInfo.defender.after.getHealth() <= 0;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return mapView.buildBattleAnimation(battleInfo);
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return listener.receiveBattleEvent(battleInfo);
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      // Apply the battle results that we calculated previously.
      battleInfo.attacker.unit.copyUnitState(battleInfo.attacker.after);
      battleInfo.defender.unit.copyUnitState(battleInfo.defender.after);
    }

    @Override
    public XYCoord getStartPoint()
    {
      return defenderCoords;
    }

    @Override
    public XYCoord getEndPoint()
    {
      return defenderCoords;
    }
  } // ~BattleEvent

  public static class DemolitionEvent implements GameEvent
  {
    private final StrikeParams result;
    private final int percentDamage;
    private final MapLocation target;

    public DemolitionEvent(Unit attacker, MapLocation target, GamePath path, MapMaster map)
    {
      this.target = target;
      // Calculate the result of the battle immediately. This will allow us to plan the animation.
      result = CombatEngine.calculateTerrainDamage(attacker, path, target, map);
      percentDamage = result.calculateDamage();
    }

    public Unit getAttacker()
    {
      return result.attacker.unit;
    }

    public MapLocation getDefender()
    {
      return target;
    }
    public boolean demolitionFinishes()
    {
      return target.durability - percentDamage <= 0;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return mapView.buildDemolitionAnimation(result, getEndPoint(), percentDamage);
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return listener.receiveDemolitionEvent(result.attacker.unit, target.getCoordinates());
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      // Apply the battle results that we calculated previously.
      Unit attacker = result.attacker.unit;
      attacker.fire(result.attacker.weapon); // expend ammo
      target.durability -= percentDamage;
    }

    @Override
    public XYCoord getStartPoint()
    {
      return target.getCoordinates();
    }

    @Override
    public XYCoord getEndPoint()
    {
      return target.getCoordinates();
    }
  } // ~DemolitionEvent

}
