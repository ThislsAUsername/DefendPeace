package Units;

import java.util.ArrayList;
import java.util.Vector;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameAction.ActionType;
import Engine.GameActionSet;
import Engine.Path;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ResupplyEvent;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;
import Units.Weapons.WeaponModel;

public class Unit
{
  public Vector<Unit> heldUnits;
  public UnitModel model;
  public int x;
  public int y;
  public int fuel;
  private int captureProgress;
  private Location captureTarget;
  public Commander CO;
  public boolean isTurnOver;
  public boolean isStunned;
  private double HP;
  public ArrayList<Weapon> weapons;
  private ArrayList<GameAction> turnInitActions;

  public Unit(Commander co, UnitModel um)
  {
    CO = co;
    model = um;
    fuel = model.maxFuel;
    isTurnOver = true;
    HP = model.maxHP;
    captureProgress = 0;
    captureTarget = null;
    if( model.weaponModels != null )
    {
      weapons = new ArrayList<Weapon>();
      for( WeaponModel weapType : model.weaponModels )
      {
        weapons.add(new Weapon(weapType));
      }
    }
    else
    {
      // Just make sure we don't crash if we try to iterate on this.
      weapons = new ArrayList<Weapon>();
    }
    if( model.holdingCapacity > 0 )
      heldUnits = new Vector<Unit>(model.holdingCapacity);

    turnInitActions = model.getTurnInitActions(this);
  }

  /**
   * Ready this unit for the next turn. Any actions it performs as part of
   * initialization will be returned in a GameEventQueue.
   * @param map
   * @param events
   */
  public GameEventQueue initTurn(MapMaster map)
  {
    // Make a queue to return any init events.
    GameEventQueue events = new GameEventQueue();

    Location locus = map.getLocation(x, y);

    // Only perform turn initialization for the unit if it is on the map.
    //   Units that are e.g. in a transport don't burn fuel, etc.
    if( null != locus )
    {
      if( isStunned )
      {
        isTurnOver = true;
        isStunned = false;
      }
      else
        isTurnOver = false;
      fuel -= model.idleFuelBurn;
      if( captureTarget != null && captureTarget.getResident() != this )
      {
        captureTarget = null;
        captureProgress = 0;
      }

      // If the unit is not at max health, and is on a repair tile, heal it.
      if( model.canRepairOn(locus) && locus.getOwner() == CO )
      {
        // Resupply is free; whether or not we can repair, go ahead and add the resupply event.
        if( !isFullySupplied() )
        {
          events.add(new ResupplyEvent(this));
        }

        if( HP < model.maxHP )
        {
          int neededHP = Math.min(model.maxHP - getHP(), 2); // will be 0, 1, 2
          double proportionalCost = model.getCost() / model.maxHP;
          if( CO.money >= neededHP * proportionalCost )
          {
            CO.money -= neededHP * proportionalCost;
            alterHP(2);
          }
          else if( CO.money >= proportionalCost )
          {
            // case will only be used if neededHP is 2
            CO.money -= proportionalCost;
            alterHP(1);
          }
        }
      }

      // Collect any turn-initialization actions for this unit.
      for( GameAction ga : turnInitActions )
      {
        events.addAll(ga.getEvents(map));
      }
    } // ~If location is valid.

    return events;
  }

  /**
   * @return whether or not this unit can attack the given unit type at the
   * specified range, accounting for the possibility of moving first.
   */
  public boolean canAttack(UnitModel targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( weapons == null )
      return false;

    boolean canHit = false;
    for( Weapon weapon : weapons )
    {
      if( afterMoving && !weapon.model.canFireAfterMoving )
      {
        // If we are planning to move first, and the weapon
        // can't shoot after moving, then move along.
        continue;
      }
      if( weapon.getDamage(targetType, range) > 0 )
      {
        canHit = true;
        break;
      }
    }
    return canHit;
  }

  /**
   * Select the weapon owned by this unit that can inflict the
   * most damage against the chosen target
   * @param target
   * @param range
   * @param afterMoving
   * @return The best weapon for that target, or null if no usable weapon exists.
   */
  public Weapon chooseWeapon(UnitModel targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( weapons == null )
      return null;

    Weapon chosenWeapon = null;
    double maxDamage = 0;
    for( Weapon weapon : weapons )
    {
      // If the weapon isn't mobile, we cannot fire if we moved.
      if( afterMoving && !weapon.model.canFireAfterMoving )
      {
        continue;
      }
      double currentDamage = weapon.getDamage(targetType, range);
      if( weapon.getDamage(targetType, range) > maxDamage )
      {
        chosenWeapon = weapon;
        maxDamage = currentDamage;
      }
    }
    return chosenWeapon;
  }

  public int getHP()
  {
    return (int) Math.ceil(HP);
  }
  public double getPreciseHP()
  {
    return HP;
  }

  public void damageHP(double damage)
  {
    HP -= damage;
    if( HP < 0 )
    {
      HP = 0;
    }
  }
  public double alterHP(int change)
  {
    double before = HP;
    // Change the unit's health, but don't grant more
    // than 10 HP, and don't drop HP to zero.
    HP = Math.max(1, Math.min(10, getHP() + change));
    return HP - before;
  }

  public void capture(Location target)
  {
    if( !target.isCaptureable() )
    {
      System.out.println("ERROR! Attempting to capture an uncapturable Location!");
      return;
    }
    if( !CO.isEnemy(target.getOwner()) )
    {
      System.out.println("WARNING! Attempting to capture an allied property!");
      return;
    }

    if( target != captureTarget )
    {
      captureTarget = target;
      captureProgress = 0;
    }
    captureProgress += getHP();
    if( captureProgress >= 20 )
    {
      target.setOwner(CO);
      captureProgress = 0;
      target = null;
    }
  }

  public void stopCapturing()
  {
    captureTarget = null;
    captureProgress = 0;
  }

  public int getCaptureProgress()
  {
    return captureProgress;
  }
  public XYCoord getCaptureTargetCoords()
  {
    XYCoord target = null;
    if( null != captureTarget )
    {
      target = captureTarget.getCoordinates();
    }
    return target;
  }

  /** Compiles and returns a list of all actions this unit could perform on map after moving along movePath. */
  public ArrayList<GameActionSet> getPossibleActions(GameMap map, Path movePath)
  {
    XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
    ArrayList<GameActionSet> actionSet = new ArrayList<GameActionSet>();
    for( ActionType at : model.possibleActions)
    {
      if( map.isLocationEmpty(this, moveLocation) )
      {
        switch (at)
        {
          case ATTACK:
          // Evaluate attack options.
          {
            boolean moved = !moveLocation.equals(x, y);
            ArrayList<GameAction> attackOptions = new ArrayList<GameAction>();
            for( Weapon wpn : weapons )
            {
              // Evaluate this weapon for targets if it has ammo, and if either the weapon
              // is mobile or we don't care if it's mobile (because we aren't moving).
              if( wpn.ammo > 0 && (!moved || wpn.model.canFireAfterMoving) )
              {
                ArrayList<XYCoord> locations = Utils.findTargetsInRange(map, CO, moveLocation, wpn);

                for( XYCoord loc : locations )
                {
                  attackOptions.add(new GameAction.AttackAction(map, this, movePath, loc));
                }
              }
            } // ~Weapon loop

            // Only add this action set if we actually have a target
            if( !attackOptions.isEmpty() )
            {
              // Bundle our attack options into an action set and add it to our return collection.
              actionSet.add(new GameActionSet(attackOptions));
            }
          } // ~attack options
            break;
          case CAPTURE:
            if( CO.isEnemy(map.getLocation(moveLocation).getOwner()) && map.getLocation(moveLocation).isCaptureable() )
            {
              actionSet.add(new GameActionSet(new GameAction.CaptureAction(map, this, movePath), false));
            }
            break;
          case WAIT:
            actionSet.add(new GameActionSet(new GameAction.WaitAction(this, movePath), false));
            break;
          case LOAD:
          case JOIN:
            // We only get to here if there is no unit at the end of the move path, which means there is
            //   no transport in this space to board and no friendly unit to reinforce. LOAD/JOIN actions are handled down below.
            break;
          case UNLOAD:
            if( heldUnits.size() > 0 )
            {
              ArrayList<GameAction> unloadActions = new ArrayList<GameAction>();

              // TODO: This could get messy real quick for transports with more cargo space. Figure out a
              //       better way to handle this case.
              for( Unit cargo : heldUnits )
              {
                ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, this, moveLocation, cargo);
                for( XYCoord loc : dropoffLocations )
                {
                  unloadActions.add(new GameAction.UnloadAction(this, movePath, cargo, loc));
                }
              }

              if( !unloadActions.isEmpty() )
              {
                actionSet.add(new GameActionSet(unloadActions));
              }
            }
            break;
          case RESUPPLY:
            // Search for a unit in resupply range.
            ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, 1);

            // For each location, see if there is a friendly unit to re-supply.
            for( XYCoord loc : locations )
            {
              // If there's a friendly unit there who isn't us, we can resupply them.
              Unit other = map.getLocation(loc).getResident();
              if( other != null && other.CO == CO && other != this && !other.isFullySupplied() )
              {
                // We found at least one unit we can resupply. Since resupply actions aren't
                // targeted, we can just add our action and break here.
                actionSet.add(new GameActionSet(new GameAction.ResupplyAction(this, movePath), false));
                break;
              }
            }
            break;
          default:
            System.out
                .println("getPossibleActions: Invalid action in model's possibleActions: " + at);
        }
      }
      else
      {
        // There is another unit in the tile at the end of movePath. We are either LOADing a transport or JOINing an ally.
        if( at == ActionType.LOAD )
        {
          if(map.getLocation(moveLocation).getResident().hasCargoSpace(model.type))
          {
            actionSet.add(new GameActionSet(new GameAction.LoadAction(map, this, movePath), false));
          }
        }
        else if( at == ActionType.JOIN )
        {
          Unit resident = map.getLocation(moveLocation).getResident();
          if( (resident.model.type == model.type) && (resident.getHP() < resident.model.maxHP) )
          {
            actionSet.add(new GameActionSet(new GameAction.UnitJoinAction(map, this, movePath), false));
          }
        }
        else
        {
          System.out.println("getPossibleActions: Invalid action in unit's possibleActions: " + at);
        }
      }
    }

    return actionSet;
  }

  public boolean hasCargoSpace(UnitEnum type)
  {
    return (model.holdingCapacity > 0 && heldUnits.size() < model.holdingCapacity && model.holdables.contains(type));
  }

  /** Grant this unit full fuel and ammunition */
  public void resupply()
  {
    fuel = model.maxFuel;
    if( null != weapons )
    {
      for( Weapon wpn : weapons )
      {
        wpn.reload();
      }
    }
  }

  /** Returns true if resupply would have zero effect on this unit. */
  public boolean isFullySupplied()
  {
    boolean isFull = (model.maxFuel == fuel);
    if( isFull )
    {
      // Check weapon ammo.
      for( Weapon w : weapons )
      {
        isFull &= (w.model.maxAmmo == w.ammo);
      }
    }
    return isFull;
  }

  @Override
  public String toString()
  {
    return model.toString();
  }

  public String toStringWithLocation()
  {
    return String.format("%s at %s", model, new XYCoord(x, y));
  }
}
