package Engine.UnitActionLifecycles;

import java.util.ArrayList;
import java.util.HashSet;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.UnitDieEvent;
import Terrain.GameMap;
import Terrain.MapMaster;
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
              attackOptions.add(new BattleAction(map, actor, movePath, loc));
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

  public static class BattleAction implements GameAction
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
      return String.format("[Attack %s with %s after moving to %s]", defender.toStringWithLocation(),
          attacker.toStringWithLocation(), moveCoord);
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.ATTACK;
    }
  } // ~Action

  /**
   * BattleEvent handles a single battle event between two units. The outcome is
   * calculated as soon as the event is created; the result is applied when
   * performEvent is called. The results can be seen via the provided functions.
   */
  public static class BattleEvent implements GameEvent
  {
    private final BattleSummary battleInfo;
    private final XYCoord defenderCoords;

    public BattleEvent(Unit attacker, Unit defender, int attackerX, int attackerY, MapMaster map)
    {
      // Calculate the result of the battle immediately. This will allow us to plan the animation.
      battleInfo = CombatEngine.calculateBattleResults(attacker, defender, map, attackerX, attackerY);
      defenderCoords = new XYCoord(defender.x, defender.y);
    }

    public Unit getAttacker()
    {
      return battleInfo.attacker;
    }
    public boolean attackerDies()
    {
      return battleInfo.attacker.getPreciseHealth() <= battleInfo.attackerHealthLoss;
    }

    public Unit getDefender()
    {
      return battleInfo.defender;
    }
    public boolean defenderDies()
    {
      return battleInfo.defender.getPreciseHealth() <= battleInfo.defenderHealthLoss;
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
      defender.damageHP(battleInfo.defenderHPLoss);

      // Handle counter-attack if relevant.
      if( battleInfo.attackerHPLoss > 0 )
      {
        defender.fire(battleInfo.attackerWeapon);
        attacker.damageHP(battleInfo.attackerHPLoss);
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
  } // ~Event
}
