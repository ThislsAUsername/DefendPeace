package Engine.UnitActionLifecycles;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

public abstract class UnitProduceLifecycle
{
  public static class UnitProduceFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    final UnitModel typeToBuild;

    public UnitProduceFactory(UnitModel type)
    {
      typeToBuild = type;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( moveLocation.equals(actor.x, actor.y) && actor.hasCargoSpace(typeToBuild.role)
          && actor.CO.army.money >= actor.CO.getCost(typeToBuild)
          && actor.materials > 0 )
      {
        return new GameActionSet(new UnitProduceAction(this, actor), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      String cost = "???";
      if( actor != null )
        cost = "" + actor.CO.getCost(typeToBuild);
      return String.format("BUILD %s (%s)", typeToBuild.toString(), cost);
    }
  } //~factory

  public static class UnitProduceAction extends GameAction
  {
    final UnitProduceFactory type;
    final Unit actor;
    final XYCoord destination;

    public UnitProduceAction(UnitProduceFactory pType, Unit unit)
    {
      type = pType;
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      eventSequence.add(new UnitProduceEvent(actor.CO, actor, type.typeToBuild));
      return eventSequence;
    }

    @Override
    public String toString()
    {
      return String.format("[Build " + type.typeToBuild.name + " with %s in place]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }

    @Override
    public Unit getActor()
    {
      return actor;
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
  } // ~UnitProduceAction

  public static class UnitProduceEvent implements GameEvent
  {
    private final Commander myCommander;
    private Unit builder;
    private final Unit myNewUnit;
    private final int cost;

    public UnitProduceEvent(Commander commander, Unit unit, UnitModel model)
    {
      myCommander = commander;
      builder = unit;

      // TODO: Consider breaking the fiscal part into its own event.
      cost = commander.getCost(model);
      if( cost <= commander.army.money && builder.materials > 0 )
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
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      if( null != myNewUnit )
      {
        return listener.receiveCreateUnitEvent(myNewUnit);
      }
      return null;
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      if( null != myNewUnit )
      {
        myCommander.army.money -= cost;
        builder.materials -= 1;
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
  } // ~UnitProduceEvent

}
