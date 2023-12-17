package Engine.UnitActionLifecycles;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public abstract class JoinLifecycle
{
  public static class JoinFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      Unit resident = map.getLocation(moveLocation).getResident();
      if( resident != null )
      {
        int recipientCapacity = new UnitContext(resident).calculateCargoCapacity();
        int cargoSize = resident.heldUnits.size() + actor.heldUnits.size();
        if( resident.model.equals(actor.model) && resident != actor && resident.isHurt() && cargoSize <= recipientCapacity )
        {
          return new GameActionSet(new JoinAction(map, actor, movePath), false);
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
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

  // A unit join action will combine a unit into a damaged unit to restore its HP. Any overflow HP is converted back into funds.
  public static class JoinAction extends GameAction
  {
    private Unit donor;
    GamePath movePath;
    private XYCoord pathEnd = null;
    private Unit recipient;

    public JoinAction(GameMap gameMap, Unit actor, GamePath path)
    {
      donor = actor;
      movePath = path;
      if( (null != movePath) && (movePath.getPathLength() > 0) )
      {
        pathEnd = movePath.getEndCoord();
        if( (null != gameMap) && gameMap.isLocationValid(pathEnd) )
        {
          recipient = gameMap.getLocation(pathEnd).getResident();
        }
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // UNITJOIN actions consist of
      //   MOVE
      //   JOIN
      GameEventQueue unitJoinEvents = new GameEventQueue();

      // Validate input
      boolean isValid = true;
      isValid &= (null != donor) && !donor.isTurnOver;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);
      isValid &= (null != gameMap);
      if( isValid )
      {
        pathEnd = movePath.getEndCoord();
        isValid &= gameMap.isLocationValid(pathEnd);
      }
      if( isValid )
      {
        // Find the unit we want to join.
        recipient = gameMap.getLocation(pathEnd).getResident();
        isValid &= (null != recipient) && recipient.isHurt();
      }
      if( isValid )
      {
        int recipientCapacity = new UnitContext(recipient).calculateCargoCapacity();
        int cargoSize = recipient.heldUnits.size() + donor.heldUnits.size();
        isValid &= cargoSize <= recipientCapacity;
      }

      // Create events.
      if( isValid )
      {
        // Move to the recipient, if we don't get blocked.
        if( Utils.enqueueBoardEvent(gameMap, donor, movePath, unitJoinEvents) )
        {
          // Combine forces.
          unitJoinEvents.add(new JoinEvent(donor, recipient));
        }
      }
      return unitJoinEvents;
    }

    @Override
    public Unit getActor()
    {
      return donor;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return pathEnd;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return pathEnd;
    }

    @Override
    public String toString()
    {
      return String.format("[Join %s into %s]", donor.toStringWithLocation(), recipient.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.JOIN;
    }
  } // ~UnitJoinAction

  public static class JoinEvent implements GameEvent
  {
    public final Unit unitDonor;
    public final Unit unitRecipient;

    public JoinEvent(Unit donor, Unit recipient)
    {
      unitDonor = donor;
      unitRecipient = recipient;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return mapView.buildUnitJoinAnimation();
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return listener.receiveUnitJoinEvent(this);
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      if( null != unitRecipient && unitRecipient.isHurt() )
      {
        // Crunch the numbers we need up front.
        int donorHealth = unitDonor.getHealth();
        int neededHealth = UnitModel.MAXIMUM_HEALTH - unitRecipient.getHealth();
        int extraHealth = donorHealth - neededHealth;
        if( extraHealth < 0 )
          extraHealth = 0;

        // Add the individuals' resources together
        unitRecipient.alterHealth(donorHealth);
        unitRecipient.ammo      = Math.min(unitRecipient.model.maxAmmo     , unitRecipient.ammo + unitDonor.ammo);
        unitRecipient.fuel      = Math.min(unitRecipient.model.maxFuel     , unitRecipient.fuel + unitDonor.fuel);
        unitRecipient.materials = Math.min(unitRecipient.model.maxMaterials, unitRecipient.materials + unitDonor.materials);

        // If we had extra HP, add that as income.
        int costPerHealth = unitDonor.getCost() / UnitModel.MAXIMUM_HEALTH; // This truncation is cart-accurate to DS; even with discount skills, the "real" price of a unit never has tens/ones
        unitDonor.CO.army.money += (extraHealth * costPerHealth);

        // Reconcile cargo units.
        unitRecipient.heldUnits.addAll(unitDonor.heldUnits);

        // Remove the donor unit.
        gameMap.removeUnit(unitDonor);
        unitDonor.CO.units.remove(unitDonor);

        // End the turn of the recipient.
        unitRecipient.isTurnOver = true;
      }
      else
      {
        System.out.println("WARNING! Cannot join " + unitDonor.model.name + " with " + unitRecipient.model.name);
      }
    }

    @Override
    public XYCoord getStartPoint()
    {
      return new XYCoord(unitDonor.x, unitDonor.y);
    }

    @Override
    public XYCoord getEndPoint()
    {
      return new XYCoord(unitRecipient.x, unitRecipient.y);
    }
  }

}
