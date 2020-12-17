package Engine.UnitActionLifecycles;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.UnitDieEvent;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

/** Removes the unit. Only allows deletion in place */
public abstract class DeleteLifecycle
{
  public static class DeleteFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;

    public DeleteFactory()
    {
      shouldConfirm = true;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( moveLocation.equals(actor.x, actor.y) )
      {
        return new GameActionSet(new DeleteAction(actor), true); // We don't really need a target, but I want a confirm dialogue
      }
      return null;
    }

    @Override
    public String name()
    {
      return "DELETE";
    }

    /**
     * From Serializable interface
     * @return The statically-defined object to use for this action type.
     */
    private Object readResolve()
    {
      return DELETE;
    }
  } // ~factory

  public static class DeleteAction extends GameAction
  {
    final Unit actor;
    final XYCoord destination;

    public DeleteAction(Unit unit)
    {
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      eventSequence.add(new UnitDieEvent(actor));
      // The unit died; check if the Commander is defeated.
      if( actor.CO.units.size() == 1 )
      {
        // CO is out of units. Too bad.
        eventSequence.add(new CommanderDefeatEvent(actor.CO));
      }
      return eventSequence;
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public String toString()
    {
      return String.format("[Delete %s in place]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return UnitActionFactory.DELETE;
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
  } // ~UnitDeleteAction

  // No event, as UnitDieEvents are held in common with multiple activities
}
