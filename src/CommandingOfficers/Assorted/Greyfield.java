package CommandingOfficers.Assorted;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GameScenario;
import Engine.Path;
import Engine.UnitActionType;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

public class Greyfield extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Greyfield");
      infoPages.add(new InfoPage(
          "Stealths are replaced with Seaplanes, which are Stealths with +1 move that cost 15k, but can't hide." +
          "Can build Seaplanes from Carriers" +
          "Naval units, copters, and Seaplanes gain +10% firepower and +30% defense.\n" +
          "xxxXX\n" +
          "Supply Chain (3): Restore ammo to units\n" +
          "High Command (5): Resupply all units"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Greyfield(rules);
    }
  }

  public Greyfield(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels.values() )
    {
      if ( um.type == UnitEnum.STEALTH || um.type == UnitEnum.STEALTH_HIDE)
      {
        um.possibleActions.clear();
        for( UnitActionType action : UnitActionType.COMBAT_VEHICLE_ACTIONS )
        {
          um.possibleActions.add(action);
        }
        um.name = "Seaplane";
        um.hidden = false;
        um.moneyCostAdjustment = -9000;
        um.movePower += 1;
        um.modifyDamageRatio(10);
        um.modifyDefenseRatio(30);
      }
      if ( um.type == UnitEnum.CARRIER)
        um.possibleActions.add(new BuildSeaplane(this));
      
      if( um.chassis == ChassisEnum.SUBMERGED || um.chassis == ChassisEnum.SHIP ||
          um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(10);
        um.modifyDefenseRatio(30);
      }
    }

    addCommanderAbility(new Resupply(this, "Supply Chain", 3, true, false));
    addCommanderAbility(new Resupply(this, "High Command", 5, true, true));
  }
  
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Resupply extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private boolean ammo, fuel;

    Resupply(Commander commander, String name, int cost, boolean ammo, boolean fuel)
    {
      super(commander, name, cost);
      this.ammo = ammo;
      this.fuel = fuel;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for (Unit unit : myCommander.units)
      {
        if( fuel )
          unit.fuel = unit.model.maxFuel;
        if( ammo )
          for( Weapon w : unit.weapons )
            w.reload();
      }
    }
  }
  
  
  
  
  public static class BuildSeaplane extends UnitActionType
  {
    private static final long serialVersionUID = 1L;
    final Commander payer;
    final UnitModel seaplane;
    public BuildSeaplane(Commander owner)
    {
      payer = owner;
      seaplane = owner.getUnitModel(UnitEnum.STEALTH);
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( moveLocation.equals(actor.x, actor.y) && actor.hasCargoSpace(seaplane.type) && payer.money > seaplane.getCost() )
      {
        return new GameActionSet(new BuildSeaplaneAction(this, actor), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return "BUILD";
    }
  }

  public static class BuildSeaplaneAction implements GameAction
  {
    final BuildSeaplane type;
    final Unit actor;
    final XYCoord destination;
    public BuildSeaplaneAction(BuildSeaplane owner, Unit unit)
    {
      type = owner;
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      eventSequence.add(new BuildSeaplaneEvent(type.payer, actor, type.seaplane));
      return eventSequence;
    }

    @Override
    public String toString()
    {
      return String.format("[Build seaplane with %s in place]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionType getType()
    {
      return type;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return destination;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return destination;
    }
  } // ~BuildSeaplaneAction

  public static class BuildSeaplaneEvent implements GameEvent
  {
    private final Commander myCommander;
    private Unit builder;
    private final Unit myNewUnit;

    public BuildSeaplaneEvent(Commander commander, Unit unit, UnitModel model)
    {
      myCommander = commander;
      builder = unit;

      // TODO: Consider breaking the fiscal part into its own event.
      if( model.getCost() <= commander.money )
      {
        myNewUnit = new Unit(myCommander, model);
      }
      else
      {
        System.out.println("WARNING! Attempting to build unit with insufficient funds.");
        myNewUnit = null;
      }
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public void sendToListener(GameEventListener listener)
    {
      if( null != myNewUnit )
      {
        listener.receiveCreateUnitEvent(myNewUnit);
      }
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      if( null != myNewUnit )
      {
        myCommander.money -= myNewUnit.model.getCost();
        myCommander.units.add(myNewUnit);
        builder.heldUnits.add(myNewUnit);
        builder.isTurnOver = true;
      }
      else
      {
        System.out.println("Warning! Attempting to build unit with insufficient funds.");
      }
    }

    @Override
    public XYCoord getStartPoint()
    {
      return new XYCoord(builder.x, builder.y);
    }

    @Override
    public XYCoord getEndPoint()
    {
      return new XYCoord(builder.x, builder.y);
    }
  }
}

