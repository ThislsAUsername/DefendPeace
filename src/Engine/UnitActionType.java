package Engine;

import java.io.Serializable;
import java.util.ArrayList;

import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;

public interface UnitActionType extends Serializable
{
  public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor);
  public String name();

  public static final UnitActionType ATTACK = new Attack();
  public static final UnitActionType UNLOAD = new Unload();
  public static final UnitActionType CAPTURE = new Capture();
  public static final UnitActionType RESUPPLY = new Resupply();
  public static final UnitActionType WAIT = new Wait();
  public static final UnitActionType LOAD = new Load();
  public static final UnitActionType JOIN = new Join();

  public static final UnitActionType[] FOOTSOLDIER_ACTIONS =    { ATTACK, CAPTURE,  WAIT, LOAD, JOIN };
  public static final UnitActionType[] COMBAT_VEHICLE_ACTIONS = { ATTACK,           WAIT, LOAD, JOIN };
  public static final UnitActionType[] TRANSPORT_ACTIONS =      { UNLOAD,           WAIT, LOAD, JOIN };
  public static final UnitActionType[] APC_ACTIONS =            { UNLOAD, RESUPPLY, WAIT, LOAD, JOIN };

  /**
   * Base type to standardize indexing into HashMaps
   */
  abstract static class BaseUnitActionType implements UnitActionType
  {
    @Override
    public int hashCode()
    {
      return name().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if( this == obj )
        return true;
      if( obj == null )
        return false;
      if( getClass() != obj.getClass() )
        return false;
      UnitActionType other = (UnitActionType) obj;
      if( !name().contentEquals(other.name()) )
        return false;
      return true;
    }
  }

  public static class Attack extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
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
  }

  public static class Capture extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
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
  }

  public static class Wait extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
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
  }

  public static class Load extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      Unit resident = map.getLocation(moveLocation).getResident();
      if( resident != null )
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
  }

  public static class Join extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      Unit resident = map.getLocation(moveLocation).getResident();
      if( resident != null )
      {
        if( (resident.model.type == actor.model.type) && (resident.getHP() < resident.model.maxHP) )
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
  }

  public static class Unload extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
      {
        if( actor.heldUnits.size() > 0 )
        {
          ArrayList<GameAction> unloadActions = new ArrayList<GameAction>();

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
  }

  public static class Resupply extends BaseUnitActionType
  {
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
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
  }

  /**
   * Effectively a wait, but the unit ends up as a different unit at the end of it.
   * This action type requires a parameter (the unit to transform into), and thus
   * cannot be represented as a static global constant.
   */
  public static class Transform extends BaseUnitActionType
  {
    public final UnitModel destinationType;
    
    public Transform(UnitModel type)
    {
      destinationType = type;
    }
    
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new GameAction.TransformAction(actor, movePath, this), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return String.format("~%s", destinationType);
    }
  }
}
