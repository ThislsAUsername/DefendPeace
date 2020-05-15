package Engine;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameEvents.CommanderAbilityEvent;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.Location;
import Terrain.MapMaster;
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
  public abstract UnitActionFactory getType();

  // ==========================================================
  //   Concrete Action type classes.
  // ==========================================================

  // ===========  AttackAction  ===============================

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
    public UnitActionFactory getType()
    {
      return null;
    }
  } // ~UnitProductionAction

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
    public UnitActionFactory getType()
    {
      return null;
    }
  } // ~AbilityAction
  
}