package Engine;

import java.io.Serializable;
import java.util.ArrayList;

import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

public abstract class UnitActionType implements Serializable
{
  private static final long serialVersionUID = 1L;

  public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
  {
    return getPossibleActions(map, movePath, actor, false);
  }
  public abstract GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident);
  public abstract String name();
  public boolean shouldConfirm = false;

  public static final UnitActionType ATTACK = new Attack();
  public static final UnitActionType UNLOAD = new Unload();
  public static final UnitActionType CAPTURE = new Capture();
  public static final UnitActionType RESUPPLY = new Resupply();
  public static final UnitActionType REPAIR_UNIT = new RepairUnit();
  public static final UnitActionType WAIT = new Wait();
  public static final UnitActionType DELETE = new Delete();
  public static final UnitActionType LOAD = new Load();
  public static final UnitActionType JOIN = new Join();

  public static final UnitActionType[] FOOTSOLDIER_ACTIONS =      { ATTACK, CAPTURE,  WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionType[] COMBAT_VEHICLE_ACTIONS =   { ATTACK,           WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionType[] COMBAT_TRANSPORT_ACTIONS = { ATTACK, UNLOAD,   WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionType[] TRANSPORT_ACTIONS =        { UNLOAD,           WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionType[] APC_ACTIONS =              { UNLOAD, RESUPPLY, WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionType[] BASIC_ACTIONS =            {                   WAIT, DELETE, LOAD, JOIN };

  public static class Attack extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        // Evaluate attack options.
        {
          boolean moved = !moveLocation.equals(actor.x, actor.y);
          ArrayList<GameAction> attackOptions = new ArrayList<GameAction>();
          for( Weapon wpn : actor.weapons )
          {
            // Evaluate this weapon for targets if it has ammo, and if either the weapon
            // is mobile or we don't care if it's mobile (because we aren't moving).
            if( wpn.ammo > 0 && (!moved || wpn.model.canFireAfterMoving) )
            {
              ArrayList<XYCoord> locations = Utils.findTargetsInRange(map, actor.CO, moveLocation, wpn);

              for( XYCoord loc : locations )
              {
                attackOptions.add(new GameAction.AttackAction(map, actor, movePath, loc));
              }
            }
          } // ~Weapon loop

          // Only add this action set if we actually have a target
          if( !attackOptions.isEmpty() )
          {
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
  }

  public static class Capture extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        if( actor.CO.isEnemy(map.getLocation(moveLocation).getOwner()) && map.getLocation(moveLocation).isCaptureable() )
        {
          return new GameActionSet(new GameAction.CaptureAction(map, actor, movePath), false);
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "CAPTURE";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return CAPTURE;
    }
  }

  public static class Wait extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new GameAction.WaitAction(actor, movePath), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return "WAIT";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return WAIT;
    }
  }

  public static class Load extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      Unit resident = map.getLocation(moveLocation).getResident();
      if( !ignoreResident && resident != null )
      {
        if( resident.hasCargoSpace(actor.model.type) )
        {
          return new GameActionSet(new GameAction.LoadAction(map, actor, movePath), false);
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "LOAD";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return LOAD;
    }
  }

  public static class Join extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      Unit resident = map.getLocation(moveLocation).getResident();
      if( !ignoreResident && resident != null )
      {
        if( (resident.model.type == actor.model.type) && resident != actor && (resident.getHP() < resident.model.maxHP) )
        {
          return new GameActionSet(new GameAction.UnitJoinAction(map, actor, movePath), false);
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "JOIN";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return JOIN;
    }
  }

  public static class Unload extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        if( actor.heldUnits.size() > 0 )
        {
          ArrayList<GameAction> unloadActions = new ArrayList<GameAction>();

          // TODO: Consider using ignoreResident for dropoff points as well
          for( Unit cargo : actor.heldUnits )
          {
            ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, actor, moveLocation, cargo);
            for( XYCoord loc : dropoffLocations )
            {
              unloadActions.add(new GameAction.UnloadAction(actor, movePath, cargo, loc));
            }
          }

          if( !unloadActions.isEmpty() )
          {
            return new GameActionSet(unloadActions);
          }
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "UNLOAD";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return UNLOAD;
    }
  }

  public static class Resupply extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        // Search for a unit in resupply range.
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, 1);

        // For each location, see if there is a friendly unit to re-supply.
        for( XYCoord loc : locations )
        {
          // If there's a friendly unit there who isn't us, we can resupply them.
          Unit other = map.getLocation(loc).getResident();
          if( other != null && other.CO == actor.CO && other != actor && !other.isFullySupplied() )
          {
            // We found at least one unit we can resupply. Since resupply actions aren't
            // targeted, we can just add our action and break here.
            return new GameActionSet(new GameAction.ResupplyAction(actor, movePath), false);
          }
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "RESUPPLY";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return RESUPPLY;
    }
  }

  public static class RepairUnit extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        ArrayList<GameAction> repairOptions = new ArrayList<GameAction>();
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, 1, 1);

        // For each location, see if there is a friendly unit to repair.
        for( XYCoord loc : locations )
        {
          // If there's a friendly unit there who isn't us, we can repair them.
          Unit other = map.getLocation(loc).getResident();
          if( other != null && !actor.CO.isEnemy(other.CO) && other != actor &&
              (!other.isFullySupplied() || other.getPreciseHP() < other.model.maxHP) )
          {
            repairOptions.add(new GameAction.RepairUnitAction(actor, movePath, other));
          }
        }

        // Only add this action set if we actually have a target
        if( !repairOptions.isEmpty() )
        {
          // Bundle our attack options into an action set
          return new GameActionSet(repairOptions);
        }
      }
      return null;
    }

    @Override
    public String name()
    {
      return "REPAIR";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return REPAIR_UNIT;
    }
  }

  /**
   * Effectively a wait, but the unit ends up as a different unit at the end of it.
   * This action type requires a parameter (the unit to transform into), and thus
   * cannot be represented as a static global constant.
   */
  public static class Transform extends UnitActionType
  {
    private static final long serialVersionUID = 1L;
    public final UnitEnum destinationType;
    public final String name;
    
    public Transform(UnitEnum type, String displayName)
    {
      destinationType = type;
      name = displayName;
    }
    
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new GameAction.TransformAction(actor, movePath, this), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return name;
    }
  }

  /**
   * Effectively a wait, but the unit dies and deals damage to everything nearby
   * This action type requires parameters, and thus
   * cannot be represented as a static global constant.
   */
  public static class Explode extends UnitActionType
  {
    private static final long serialVersionUID = 1L;
    public final int damage, range;
    
    public Explode(int pDamage, int pRange)
    {
      damage = pDamage;
      range = pRange;
      shouldConfirm = true;
    }
    
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new GameAction.ExplodeAction(actor, movePath, this), true); // We don't really need a target, but I want a confirm dialogue
      }
      return null;
    }

    @Override
    public String name()
    {
      return "EXPLODE";
    }
  }

  public static class Delete extends UnitActionType
  {
    private static final long serialVersionUID = 1L;

    public Delete()
    {
      shouldConfirm = true;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( moveLocation.equals(actor.x, actor.y) )
      {
        return new GameActionSet(new GameAction.UnitDeleteAction(actor), true); // We don't really need a target, but I want a confirm dialogue
      }
      return null;
    }

    @Override
    public String name()
    {
      return "DELETE";
    }
  }
}
