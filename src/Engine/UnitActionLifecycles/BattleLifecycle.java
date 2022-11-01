package Engine.UnitActionLifecycles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.UnitDieEvent;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.WeaponModel;

public abstract class BattleLifecycle
{
  public static class BattleFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
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
            if( wpn.loaded(actor) && (!moved || wpn.canFireAfterMoving) )
            {
              ArrayList<XYCoord> locations = Utils.findTargetsInRange(map, actor.CO, moveLocation, wpn);

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
    public String name()
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
    private Path movePath;
    private XYCoord moveCoord = null;
    private XYCoord attackLocation = null;
    private Unit attacker;
    private Unit defender;

    public BattleAction(GameMap gameMap, Unit actor, Path path, int targetX, int targetY)
    {
      this(gameMap, actor, path, new XYCoord(targetX, targetY));
    }

    public BattleAction(GameMap gameMap, Unit actor, Path path, XYCoord atkLoc)
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
      isValid &= null == gameMap.getResident(moveCoord) || gameMap.getResident(moveCoord) == attacker;
      isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      if( isValid )
      {
        attackRange = Math.abs(moveCoord.xCoord - attackLocation.xCoord) + Math.abs(moveCoord.yCoord - attackLocation.yCoord);

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
          BattleEvent event = new BattleEvent(attacker, defender, movePath, gameMap);
          attackEvents.add(event);

          if( event.attackerDies() )
          {
            Utils.enqueueDeathEvent(event.getAttacker(), attackEvents);

            // Since the attacker died, see if he has any friends left.
            if( attacker.CO.units.size() == 1 )
            {
              // CO is out of units. Too bad.
              attackEvents.add(new CommanderDefeatEvent(event.getAttacker().CO));
            }
          }
          if( event.defenderDies() )
          {
            Utils.enqueueDeathEvent(event.getDefender(), attackEvents);

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
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      BattleSummary summary = CombatEngine.simulateBattleResults(attacker, defender, map,
                              moveCoord.xCoord, moveCoord.yCoord);

      // output any damage done, with the color of the one dealing the damage
      if( summary.attackerHealthLoss > 0 )
        output.add(new DamagePopup(movePath.getWaypoint(0).GetCoordinates(), defender.CO.myColor, (int) (summary.attackerHealthLoss*10) + "%"));
      if( summary.defenderHealthLoss > 0 )
        // grab the two most significant digits and convert to %
        output.add(new DamagePopup(attackLocation, attacker.CO.myColor, (int) (summary.defenderHealthLoss*10) + "%"));

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
    private Path movePath;
    private XYCoord moveCoord = null;
    private XYCoord attackLocation = null;
    private Unit attacker;
    private Location target;

    public DemolitionAction(GameMap gameMap, Unit actor, Path path, XYCoord atkLoc)
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
        attackRange = Math.abs(moveCoord.xCoord - attackLocation.xCoord) + Math.abs(moveCoord.yCoord - attackLocation.yCoord);

        boolean moved = attacker.x != moveCoord.xCoord || attacker.y != moveCoord.yCoord;
        isValid &= (null != target) && attacker.canAttack(target, attackRange, moved);
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

            // Check if destroying this property will cause someone's defeat.
            if( Utils.willLoseFromLossOf(gameMap, target) )
            {
              // Someone is losing their big, comfy chair.
              CommanderDefeatEvent defeat = new CommanderDefeatEvent(target.getOwner());
              attackEvents.add(defeat);
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
        output.add(new DamagePopup(attackLocation, attacker.CO.myColor, (int) (result.calculateDamage()*10) + "%"));

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

    public BattleEvent(Unit attacker, Unit defender, Path path, MapMaster map)
    {
      boolean attackerMoved = path.getPathLength() > 1;
      // Calculate the result of the battle immediately. This will allow us to plan the animation.
      battleInfo = CombatEngine.calculateBattleResults(attacker, defender, map, attackerMoved, path.getEnd().x, path.getEnd().y);
      defenderCoords = new XYCoord(defender.x, defender.y);
    }

    public Unit getAttacker()
    {
      return battleInfo.attacker;
    }
    public boolean attackerDies()
    {
      return (int) ((battleInfo.attacker.getPreciseHP() - battleInfo.attackerHealthLoss) * 10) <= 0;
    }

    public Unit getDefender()
    {
      return battleInfo.defender;
    }
    public boolean defenderDies()
    {
      final double preciseHP = battleInfo.defender.getPreciseHP();
      final double healthLoss = battleInfo.defenderHealthLoss;
      final double finalHP = (preciseHP - healthLoss) * 10;
      return (int) finalHP <= 0;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return mapView.buildBattleAnimation(battleInfo);
    }

    @Override
    public void sendToListener(GameEventListener listener)
    {
      listener.receiveBattleEvent(battleInfo);
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      // Apply the battle results that we calculated previously.
      Unit attacker = battleInfo.attacker;
      Unit defender = battleInfo.defender;
      attacker.fire(battleInfo.attackerWeapon); // expend ammo
      defender.damageHP(battleInfo.defenderHealthLoss);

      // Handle counter-attack if relevant.
      if( battleInfo.attackerHealthLoss > 0 )
      {
        defender.fire(battleInfo.defenderWeapon);
        attacker.damageHP(battleInfo.attackerHealthLoss);
      }
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
    private final Location target;

    public DemolitionEvent(Unit attacker, Location target, Path path, MapMaster map)
    {
      this.target = target;
      // Calculate the result of the battle immediately. This will allow us to plan the animation.
      result = CombatEngine.calculateTerrainDamage(attacker, path, target, map);
      percentDamage = (int) (10 * result.calculateDamage());
    }

    public Unit getAttacker()
    {
      return result.attacker.body;
    }

    public Location getDefender()
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
    public void sendToListener(GameEventListener listener)
    {
      listener.receiveDemolitionEvent(result.attacker.body, target.getCoordinates());
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      // Apply the battle results that we calculated previously.
      Unit attacker = result.attacker.body;
      attacker.fire(result.attacker.gun); // expend ammo
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
