package Engine;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.CommanderAbilityEvent;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.GameEvents.TeleportEvent;
import Engine.GameEvents.UnitDieEvent;
import Engine.GameEvents.TeleportEvent.AnimationStyle;
import Engine.GameEvents.TeleportEvent.CollisionOutcome;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

/**
 * Provides an interface for all in-game actions.
 */
public abstract class GameAction
{
  /**
   * Returns a GameEventQueue with the events that make up this action. If the action
   * was constructed incorrectly, this should return an empty GameEventQueue.
   */
  public abstract GameEventQueue getEvents(MapMaster map);
  public abstract XYCoord getMoveLocation();
  public abstract XYCoord getTargetLocation();
  public abstract UnitActionFactory getType();

  public Collection<DamagePopup> getDamagePopups(GameMap map)
  {
    return new ArrayList<DamagePopup>();
  }

  // ==========================================================
  //   Concrete Action type classes.
  // ==========================================================

  // ===========  UnitProductionAction  ==============================
  public static class UnitProductionAction extends GameAction
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
    public UnitActionFactory getType()
    {
      return null;
    }
  } // ~UnitProductionAction

  // ===========  AbilityAction  =================================
  public static class AbilityAction extends GameAction
  {
    private CommanderAbility myAbility;

    public AbilityAction(CommanderAbility ability)
    {
      // ABILITY actions consist of
      //   ABILITY
      myAbility = ability;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEventQueue abilityEvents = new GameEventQueue();

      // Validity check
      boolean isValid = null != myAbility;
      isValid &= myAbility.myCommander.getReadyAbilities().contains(myAbility);
      if( !isValid ) return abilityEvents;

      // Create an event for the ability itself, and then for each resulting event.
      abilityEvents.add(new CommanderAbilityEvent(myAbility));
      abilityEvents.addAll(myAbility.getEvents(map));
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
    public UnitActionFactory getType()
    {
      return null;
    }
  } // ~AbilityAction

  // ===========  TeleportAction  =================================
  /**
   * Moves a unit directly to the destination without traversing intermediate steps.
   * No validation is performed on the final destination, but affordances are provided for
   * conflict resolution (e.g. existing units can swap places, die, or simply be removed).
   * If a unit is teleported into non-traversable terrain, it will die; teleport carefully.
   */
  public static class TeleportAction extends GameAction
  {
    private Unit unit;
    private XYCoord unitStart;
    private XYCoord unitDestination;
    private Unit obstacle;
    private AnimationStyle animationStyle;
    private CollisionOutcome collisionOutcome;

    public TeleportAction(Unit u, XYCoord dest)
    {
      this(u, dest, TeleportEvent.AnimationStyle.BLINK, TeleportEvent.CollisionOutcome.KILL);
    }

    public TeleportAction(Unit u, XYCoord dest, CollisionOutcome crashResult)
    {
      this(u, dest, TeleportEvent.AnimationStyle.BLINK, crashResult);
    }

    public TeleportAction(Unit u, XYCoord dest, AnimationStyle animStyle, CollisionOutcome crashResult)
    {
      unit = u;
      unitStart = new XYCoord(unit.x, unit.y);
      unitDestination = dest;
      animationStyle = animStyle;
      collisionOutcome = crashResult;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Teleport actions consist of
      // [TELEPORT]   (if the two units swap places)
      // [MASSDAMAGE] (if the other unit is squashed or killed by swapping onto bad terrain)
      // [DEATH]      (if the other unit is squashed or killed by swapping onto bad terrain)
      // [DEFEAT]     (if the other unit's death causes defeat)
      // [TELEPORT]   (to move this unit to its destination)
      // [MASSDAMAGE] (if this unit can't survive at the destination)
      // [DEATH]      (if this unit can't survive at the destination)
      // [DEFEAT]     (if the acting unit dies and is the last one)

      GameEventQueue subEvents = new GameEventQueue();

      // Validate input.
      boolean isValid = true;
      isValid &= null != unit && !unit.isTurnOver;
      isValid &= (null != gameMap && null != unitDestination) && gameMap.isLocationValid(unitDestination);

      if( !isValid ) return subEvents;

      // Put our guy where he belongs.
      subEvents.add(new TeleportEvent(gameMap, unit, unitDestination, animationStyle));

      // Figure out if something's in the way, and what to do with it.
      obstacle = gameMap.getLocation(unitDestination).getResident();
      boolean obstacleDies = false;
      if( null != obstacle )
      {
        switch(collisionOutcome)
        {
          case KILL:
            obstacleDies = true;
            break;
          case SWAP:
            // Move him to where our guy started. If he can't live there, he dies.
            if( gameMap.isLocationValid(unitStart) )
            {
              subEvents.add(new TeleportEvent(gameMap, obstacle, unitStart, animationStyle));
              if( !obstacle.model.propulsion.canTraverse(gameMap.getEnvironment(unitStart)) )
              {
                obstacleDies = true;
              }
            }
            else obstacleDies = true;
            break;
        }
      }

      if( obstacleDies )
      {
        ArrayList<Unit> ary = new ArrayList<Unit>();
        ary.add(obstacle);
        boolean fatal = true;
        MassDamageEvent mde = new MassDamageEvent(ary, obstacle.getHP()+1, fatal);
        UnitDieEvent ude = new UnitDieEvent(obstacle);
        subEvents.add(mde);
        subEvents.add(ude);

        // Poor sap died; Check if his CO lost the game.
        if( obstacle.CO.units.size() == 1 )
        {
          CommanderDefeatEvent cde = new CommanderDefeatEvent(obstacle.CO);
          subEvents.add(cde);
        }
      }

      // If our guy can't survive there, end him.
      if( !unit.model.propulsion.canTraverse(gameMap.getEnvironment(unitDestination)) )
      {
        ArrayList<Unit> ary = new ArrayList<Unit>();
        ary.add(unit);
        boolean fatal = true;
        MassDamageEvent mde = new MassDamageEvent(ary, unit.getHP()+1, fatal);
        UnitDieEvent ude = new UnitDieEvent(unit);
        subEvents.add(mde);
        subEvents.add(ude);

        // Our unit died; check if we are defeated.
        if( unit.CO.units.size() == 1 )
        {
          // CO is out of units. Too bad.
          CommanderDefeatEvent cde = new CommanderDefeatEvent(unit.CO);
          subEvents.add(cde);
        }
      }
      return subEvents;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return unitDestination;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return unitDestination;
    }

    @Override
    public UnitActionFactory getType()
    {
      return null;
    }
  } // ~TeleportAction
}